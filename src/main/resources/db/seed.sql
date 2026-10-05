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
