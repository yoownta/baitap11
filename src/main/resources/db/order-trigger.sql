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
