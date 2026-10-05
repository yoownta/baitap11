-- Public coursework demo accounts. PBKDF2-SHA256: 600000 iterations, 12-byte salt, 24-byte key.
-- Never replace passwords, activate or elevate an account that already exists.
IF NOT EXISTS(SELECT 1 FROM dbo.Users WHERE Username=N'demo_user')
 INSERT dbo.Users(Username,Password,Fullname,Email,Admin,Active)
 VALUES(N'demo_user',N'Y/bf9OkSWGZ0hYbO:RbpRNoxmcQIPs3O4M/QUBMReTlcpcPok',N'Khách hàng Demo',N'demo_user@example.com',0,1);
IF NOT EXISTS(SELECT 1 FROM dbo.Users WHERE Username=N'demo_admin')
 INSERT dbo.Users(Username,Password,Fullname,Email,Admin,Active)
 VALUES(N'demo_admin',N'3Aw8M5hEFjJduGoS:I6aBvSAxieIhv0S2Ucp2SPQ8L4kUqtJ9',N'Quản trị Demo',N'demo_admin@example.com',1,1);
