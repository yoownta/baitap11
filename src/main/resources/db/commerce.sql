-- Bai tap 11: additive migration, safe to run repeatedly.
IF OBJECT_ID('dbo.ShopProducts','U') IS NULL
CREATE TABLE dbo.ShopProducts (
 ProductId INT IDENTITY PRIMARY KEY, Title NVARCHAR(200) NOT NULL,
 VideoId NVARCHAR(50) NULL, Price DECIMAL(18,0) NOT NULL CHECK(Price>0),
 Stock INT NOT NULL CHECK(Stock>=0), MaxQuantity INT NOT NULL CHECK(MaxQuantity BETWEEN 1 AND 99),
 Active BIT NOT NULL DEFAULT 1,
 FOREIGN KEY(VideoId) REFERENCES dbo.Videos(VideoId) ON DELETE SET NULL
);
IF OBJECT_ID('dbo.CartItems','U') IS NULL
CREATE TABLE dbo.CartItems (
 Username NVARCHAR(50) NOT NULL REFERENCES dbo.Users(Username),
 ProductId INT NOT NULL REFERENCES dbo.ShopProducts(ProductId),
 Quantity INT NOT NULL CHECK(Quantity BETWEEN 1 AND 99),
 PRIMARY KEY(Username,ProductId)
);
IF OBJECT_ID('dbo.ShopOrders','U') IS NULL
CREATE TABLE dbo.ShopOrders (
 OrderId BIGINT IDENTITY PRIMARY KEY, Username NVARCHAR(50) NOT NULL REFERENCES dbo.Users(Username),
 Recipient NVARCHAR(100) NOT NULL, Phone NVARCHAR(15) NOT NULL, Address NVARCHAR(500) NOT NULL,
 Note NVARCHAR(500) NOT NULL, Total DECIMAL(18,0) NOT NULL CHECK(Total>0),
 PaymentMethod VARCHAR(10) NOT NULL DEFAULT 'COD' CHECK(PaymentMethod='COD'),
 Status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK(Status IN ('NEW','CONFIRMED','PREPARING','SHIPPING','DELIVERING','DELIVERED','CANCELLED','RETURNED')),
 CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(), UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 CheckoutToken VARCHAR(36) NOT NULL UNIQUE,
 StockRestored BIT NOT NULL DEFAULT 0
);
IF OBJECT_ID('dbo.ShopOrderItems','U') IS NULL
CREATE TABLE dbo.ShopOrderItems (
 OrderId BIGINT NOT NULL REFERENCES dbo.ShopOrders(OrderId),
 ProductId INT NOT NULL REFERENCES dbo.ShopProducts(ProductId),
 Title NVARCHAR(200) NOT NULL, Price DECIMAL(18,0) NOT NULL CHECK(Price>0),
 Quantity INT NOT NULL CHECK(Quantity BETWEEN 1 AND 99), PRIMARY KEY(OrderId,ProductId)
);
IF OBJECT_ID('dbo.ShopOrderEvents','U') IS NULL
CREATE TABLE dbo.ShopOrderEvents (
 EventId BIGINT IDENTITY PRIMARY KEY, OrderId BIGINT NOT NULL REFERENCES dbo.ShopOrders(OrderId),
 Status VARCHAR(20) NOT NULL, ChangedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 Actor NVARCHAR(100) NOT NULL
);
IF OBJECT_ID('dbo.VideoMedia','U') IS NULL
CREATE TABLE dbo.VideoMedia (
 VideoId NVARCHAR(50) PRIMARY KEY REFERENCES dbo.Videos(VideoId) ON DELETE CASCADE,
 MediaUrl NVARCHAR(1000) NOT NULL
);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_ShopOrders_UserStatus' AND object_id=OBJECT_ID('dbo.ShopOrders'))
 CREATE INDEX IX_ShopOrders_UserStatus ON dbo.ShopOrders(Username,Status,CreatedAt DESC);
-- Seed three physical study kits only once, unrelated videos stay free to watch.
IF NOT EXISTS(SELECT 1 FROM dbo.ShopProducts)
BEGIN
 INSERT dbo.ShopProducts(Title,VideoId,Price,Stock,MaxQuantity)
 SELECT TOP(3) N'Bộ tài liệu thực hành - '+LEFT(Title,160), VideoId, 99000, 25, 5
 FROM dbo.Videos WHERE Active=1 ORDER BY TRY_CONVERT(INT,VideoId),VideoId;
END;

-- Bind only the matching lessons to bundled media, preserve configured sources.
-- The second bundled file is its own lesson, not substituted for another topic.
IF NOT EXISTS(SELECT 1 FROM dbo.Videos WHERE Title=N'Java: nhập xuất dữ liệu')
 INSERT dbo.Videos(Title,Poster,Views,Description,Active,CategoryId)
 SELECT N'Java: nhập xuất dữ liệu',N'/assets/images/java.svg',0,N'Thực hành nhập xuất dữ liệu trong Java với video nội bộ.',1,CategoryId
 FROM dbo.Category WHERE Categorycode=N'JAVA';
INSERT dbo.VideoMedia(VideoId,MediaUrl)
SELECT v.VideoId, CASE WHEN v.Title LIKE N'%biến%' THEN '/videos/java-01.mp4' ELSE '/videos/java-02.mp4' END
FROM dbo.Videos v WHERE (v.Title LIKE N'%biến%' OR v.Title LIKE N'%nhập%xuất%')
AND NOT EXISTS(SELECT 1 FROM dbo.VideoMedia m WHERE m.VideoId=v.VideoId);
