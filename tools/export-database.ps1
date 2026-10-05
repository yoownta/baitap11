$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$schema = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\schema.sql')
$seed = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\seed.sql')
$demo = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\demo-users.sql')
$header = @'
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
'@
$commerce = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\commerce.sql')
$trigger = Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\order-trigger.sql')
$output = $header + [Environment]::NewLine + $schema + [Environment]::NewLine + $seed + [Environment]::NewLine + $demo + [Environment]::NewLine + $commerce + [Environment]::NewLine + $trigger + [Environment]::NewLine + 'COMMIT TRANSACTION;' + [Environment]::NewLine
[IO.File]::WriteAllText((Join-Path $root 'database.sql'), $output, (New-Object Text.UTF8Encoding($false)))
Write-Host 'database.sql generated from the application SQL resources.'
