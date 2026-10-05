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
