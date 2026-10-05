USE BAITAP11_24133054;
GO
-- 1. Find an actual order ID, not a guessed ID.
SELECT OrderId,Username,Recipient,Total,PaymentMethod,Status,CreatedAt,UpdatedAt
FROM dbo.ShopOrders ORDER BY OrderId DESC;
GO
-- 2. Change these two values before running this batch.
DECLARE @OrderId BIGINT = 0;
DECLARE @Status VARCHAR(20) = 'CONFIRMED';
-- NEW, CONFIRMED, PREPARING, SHIPPING, DELIVERING, DELIVERED, CANCELLED, RETURNED
IF NOT EXISTS(SELECT 1 FROM dbo.ShopOrders WHERE OrderId=@OrderId)
 THROW 50020, 'Set @OrderId to an existing order first', 1;
UPDATE dbo.ShopOrders SET Status=@Status WHERE OrderId=@OrderId;
SELECT OrderId,Status,UpdatedAt,StockRestored FROM dbo.ShopOrders WHERE OrderId=@OrderId;
SELECT Status,ChangedAt,Actor FROM dbo.ShopOrderEvents WHERE OrderId=@OrderId ORDER BY EventId;
-- Reload /orders, select the matching status filter.
-- CANCELLED/RETURNED restore inventory once and cannot be reopened.
