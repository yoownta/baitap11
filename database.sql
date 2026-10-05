-- Generated from src/main/resources/db; rerun tools/export-database.ps1 after editing SQL.
-- Safe to run again: existing rows are preserved. Change BOTH database names if needed.
USE master;
GO
IF DB_ID(N'BAITAP11_24133054') IS NULL CREATE DATABASE BAITAP11_24133054;
GO
USE BAITAP11_24133054;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @lockResult INT;
EXEC @lockResult=sys.sp_getapplock @Resource=N'KTQT03.initialize', @LockMode='Exclusive', @LockOwner='Transaction', @LockTimeout=30000;
IF @lockResult<0 THROW 50001, 'Cannot acquire initialization lock', 1;
-- De 03: preserve all types/nullability from the PDF. Never DROP existing tables.
IF OBJECT_ID(N'dbo.VideoIdSeq', N'SO') IS NULL
    EXEC(N'CREATE SEQUENCE dbo.VideoIdSeq AS BIGINT START WITH 1 INCREMENT BY 1');
IF OBJECT_ID(N'dbo.Category', N'U') IS NULL
CREATE TABLE dbo.Category (
    CategoryId INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Categoryname NVARCHAR(100) NULL,
    Categorycode NVARCHAR(100) NULL,
    Images NVARCHAR(500) NULL,
    Status BIT NULL
);
IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
CREATE TABLE dbo.Users (
    Username NVARCHAR(50) NOT NULL PRIMARY KEY,
    Password NVARCHAR(50) NULL,
    Phone NVARCHAR(15) NULL,
    Fullname NVARCHAR(50) NULL,
    Email NVARCHAR(150) NULL,
    Admin BIT NULL,
    Active BIT NULL,
    Images NVARCHAR(500) NULL
);
IF OBJECT_ID(N'dbo.Videos', N'U') IS NULL
CREATE TABLE dbo.Videos (
    VideoId NVARCHAR(50) NOT NULL CONSTRAINT DF_Videos_Id
        DEFAULT (CONVERT(NVARCHAR(50), NEXT VALUE FOR dbo.VideoIdSeq)) PRIMARY KEY,
    Title NVARCHAR(200) NULL,
    Poster NVARCHAR(50) NULL,
    Views INT NULL CONSTRAINT DF_Videos_Views DEFAULT 0,
    Description NVARCHAR(500) NULL,
    Active BIT NULL,
    CategoryId INT NULL,
    CONSTRAINT FK_Videos_Category FOREIGN KEY(CategoryId) REFERENCES dbo.Category(CategoryId)
);
IF OBJECT_ID(N'dbo.Favorites', N'U') IS NULL
CREATE TABLE dbo.Favorites (
    FavoriteId INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    LikedDate DATE NULL,
    VideoId NVARCHAR(50) NULL,
    Username NVARCHAR(50) NULL,
    CONSTRAINT FK_Favorites_Videos FOREIGN KEY(VideoId) REFERENCES dbo.Videos(VideoId),
    CONSTRAINT FK_Favorites_Users FOREIGN KEY(Username) REFERENCES dbo.Users(Username)
);
IF OBJECT_ID(N'dbo.Shares', N'U') IS NULL
CREATE TABLE dbo.Shares (
    ShareId INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Emails NVARCHAR(50) NULL,
    SharedDate DATE NULL,
    Username NVARCHAR(50) NULL,
    VideoId NVARCHAR(50) NULL,
    CONSTRAINT FK_Shares_Users FOREIGN KEY(Username) REFERENCES dbo.Users(Username),
    CONSTRAINT FK_Shares_Videos FOREIGN KEY(VideoId) REFERENCES dbo.Videos(VideoId)
);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Videos') AND name='IX_Videos_Category')
    CREATE INDEX IX_Videos_Category ON dbo.Videos(CategoryId);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Favorites') AND name='IX_Favorites_Video')
    CREATE INDEX IX_Favorites_Video ON dbo.Favorites(VideoId);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Shares') AND name='IX_Shares_Video')
    CREATE INDEX IX_Shares_Video ON dbo.Shares(VideoId);

-- Advance only when a manually imported numeric ID is ahead of the sequence.
-- Never restart an already advancing sequence back at 1.
DECLARE @max BIGINT=(SELECT MAX(TRY_CONVERT(BIGINT, VideoId)) FROM dbo.Videos);
DECLARE @current BIGINT=(SELECT CONVERT(BIGINT,current_value) FROM sys.sequences WHERE object_id=OBJECT_ID('dbo.VideoIdSeq'));
IF @max IS NOT NULL AND @max >= @current
BEGIN
    IF @max=9223372036854775807 THROW 50002, 'Video ID range exhausted', 1;
    DECLARE @restart NVARCHAR(200)=N'ALTER SEQUENCE dbo.VideoIdSeq RESTART WITH '+CONVERT(NVARCHAR(30),@max+1);
    EXEC(@restart);
END;

-- Seed once only on a completely empty database. Restarts never duplicate/reset rows.
IF NOT EXISTS (SELECT 1 FROM dbo.Category)
AND NOT EXISTS (SELECT 1 FROM dbo.Users)
AND NOT EXISTS (SELECT 1 FROM dbo.Videos)
AND NOT EXISTS (SELECT 1 FROM dbo.Favorites)
AND NOT EXISTS (SELECT 1 FROM dbo.Shares)
BEGIN
    DECLARE @java INT, @web INT, @db INT;
    INSERT dbo.Category(Categoryname,Categorycode,Images,Status)
        VALUES(N'Lập trình Java',N'JAVA',N'/assets/images/java.svg',1);
    SET @java=CONVERT(INT,SCOPE_IDENTITY());
    INSERT dbo.Category(Categoryname,Categorycode,Images,Status)
        VALUES(N'Lập trình Web',N'WEB',N'/assets/images/web.svg',1);
    SET @web=CONVERT(INT,SCOPE_IDENTITY());
    INSERT dbo.Category(Categoryname,Categorycode,Images,Status)
        VALUES(N'Cơ sở dữ liệu',N'DB',N'/assets/images/database.svg',1);
    SET @db=CONVERT(INT,SCOPE_IDENTITY());
    INSERT dbo.Category(Categoryname,Categorycode,Images,Status)
        VALUES(N'Chuyên mục trống',N'EMPTY',NULL,1);

    -- Demo users support relational statistics, not a bypass for OTP registration.
    INSERT dbo.Users(Username,Password,Phone,Fullname,Email,Admin,Active,Images)
    VALUES(N'demo01',NULL,N'0900000001',N'Nguyễn Minh Anh',N'demo01@example.com',0,1,NULL),
          (N'demo02',NULL,N'0900000002',N'Trần Hoàng Nam',N'demo02@example.com',0,1,NULL);

    DECLARE @lessons TABLE(n INT,categoryId INT,title NVARCHAR(200),description NVARCHAR(500),poster NVARCHAR(50));
    INSERT @lessons VALUES
    (1,@java,N'01. Java: biến và kiểu dữ liệu',N'Khai báo biến, kiểu nguyên thủy và chuyển đổi kiểu trong Java.',N'/assets/images/java.svg'),
    (2,@java,N'02. Java: điều kiện và vòng lặp',N'Thực hành if, switch, for và while với các bài tập cơ bản.',N'/assets/images/java.svg'),
    (3,@java,N'03. Java: mảng và chuỗi',N'Thao tác mảng, String và StringBuilder.',N'/assets/images/java.svg'),
    (4,@java,N'04. Java: lớp và đối tượng',N'Xây dựng lớp, constructor và đóng gói thuộc tính.',N'/assets/images/java.svg'),
    (5,@java,N'05. Java: kế thừa và đa hình',N'Áp dụng kế thừa, interface và ghi đè phương thức.',N'/assets/images/java.svg'),
    (6,@java,N'06. Java: collections',N'Sử dụng List, Set và Map để quản lý dữ liệu.',N'/assets/images/java.svg'),
    (7,@java,N'07. Java: xử lý ngoại lệ',N'Try/catch, custom exception và đóng tài nguyên.',N'/assets/images/java.svg'),
    (8,@web,N'01. Web: Servlet và HTTP',N'Xử lý GET, POST và điều hướng bằng Servlet.',N'/assets/images/web.svg'),
    (9,@web,N'02. Web: JSP và JSTL',N'Hiển thị dữ liệu bằng EL, c:forEach và c:out.',N'/assets/images/web.svg'),
    (10,@web,N'03. Web: Session và đăng nhập',N'Lưu phiên đăng nhập, bảo vệ trang quản trị và đăng xuất.',N'/assets/images/web.svg'),
    (11,@web,N'04. Web: JPA và kiến trúc ba lớp',N'Tách Controller, Service và DAO với JPA.',N'/assets/images/web.svg'),
    (12,@web,N'05. Web: SiteMesh và phân trang',N'Dùng decorator và phân trang theo từng category.',N'/assets/images/web.svg'),
    (13,@db,N'01. SQL: khóa chính và khóa ngoại',N'Thiết kế quan hệ Users, Videos, Category, Favorites và Shares.',N'/assets/images/database.svg'),
    (14,@db,N'02. SQL: truy vấn JOIN',N'Kết hợp bảng và tránh đếm trùng trong quan hệ một-nhiều.',N'/assets/images/database.svg'),
    (15,@db,N'03. SQL: GROUP BY và COUNT',N'Đếm video theo category, lượt thích và lượt chia sẻ.',N'/assets/images/database.svg'),
    (16,@db,N'04. SQL: transaction và sequence',N'Tự sinh mã, commit, rollback và đảm bảo toàn vẹn dữ liệu.',N'/assets/images/database.svg');

    DECLARE @n INT=1, @id NVARCHAR(50), @first NVARCHAR(50), @second NVARCHAR(50);
    DECLARE @inserted TABLE(VideoId NVARCHAR(50));
    WHILE @n<=16
    BEGIN
        DELETE FROM @inserted;
        INSERT dbo.Videos(Title,Poster,Views,Description,Active,CategoryId)
        OUTPUT inserted.VideoId INTO @inserted
        SELECT title,poster,n*10,description,1,categoryId FROM @lessons WHERE n=@n;
        SELECT @id=VideoId FROM @inserted;
        IF @n=1 SET @first=@id;
        IF @n=2 SET @second=@id;
        SET @n+=1;
    END;
    INSERT dbo.Favorites(LikedDate,VideoId,Username)
    VALUES(CONVERT(DATE,GETDATE()),@first,N'demo01'),
          (CONVERT(DATE,GETDATE()),@first,N'demo02'),
          (CONVERT(DATE,GETDATE()),@second,N'demo01');
    INSERT dbo.Shares(Emails,SharedDate,Username,VideoId)
    VALUES(N'friend1@example.com',CONVERT(DATE,GETDATE()),N'demo01',@first),
          (N'friend2@example.com',CONVERT(DATE,GETDATE()),N'demo01',@first),
          (N'friend3@example.com',CONVERT(DATE,GETDATE()),N'demo02',@first);
END;

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

-- Dynamic batch so CREATE OR ALTER TRIGGER is the first statement of its batch.
EXEC(N'CREATE OR ALTER TRIGGER dbo.TR_ShopOrders_Status ON dbo.ShopOrders AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF NOT UPDATE(Status) RETURN;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON i.OrderId=d.OrderId
   WHERE d.Status IN (''CANCELLED'',''RETURNED'') AND i.Status<>d.Status)
  THROW 50011, ''Cancelled and returned orders are terminal'', 1;
 INSERT dbo.ShopOrderEvents(OrderId,Status,Actor)
 SELECT i.OrderId,i.Status,CONVERT(NVARCHAR(100),SUSER_SNAME())
 FROM inserted i JOIN deleted d ON i.OrderId=d.OrderId WHERE i.Status<>d.Status;
 UPDATE p SET Stock=p.Stock+r.Quantity
 FROM dbo.ShopProducts p JOIN (
  SELECT oi.ProductId,SUM(oi.Quantity) AS Quantity
  FROM inserted i JOIN deleted d ON i.OrderId=d.OrderId
  JOIN dbo.ShopOrderItems oi ON oi.OrderId=i.OrderId
  WHERE i.Status IN (''CANCELLED'',''RETURNED'') AND d.StockRestored=0 AND i.Status<>d.Status
  GROUP BY oi.ProductId
 ) r ON p.ProductId=r.ProductId;
 UPDATE o SET UpdatedAt=SYSDATETIME(),StockRestored=CASE WHEN i.Status IN (''CANCELLED'',''RETURNED'') THEN 1 ELSE o.StockRestored END
 FROM dbo.ShopOrders o JOIN inserted i ON o.OrderId=i.OrderId
 JOIN deleted d ON d.OrderId=i.OrderId WHERE i.Status<>d.Status;
END');

COMMIT TRANSACTION;
