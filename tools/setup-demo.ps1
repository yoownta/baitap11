$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
if (Test-Path (Join-Path $root 'config.local.ps1')) { . (Join-Path $root 'config.local.ps1') }
$match=[regex]::Match($env:KTQT_DB_URL,'jdbc:sqlserver://([^;]+);.*?databaseName=([^;]+)')
if (!$match.Success) { throw 'Configure KTQT_DB_URL in config.local.ps1 first.' }
$builder=New-Object System.Data.SqlClient.SqlConnectionStringBuilder
$builder['Data Source']=$match.Groups[1].Value.Replace(':',',')
$builder['Initial Catalog']=$match.Groups[2].Value
$builder['User ID']=$env:KTQT_DB_USER
$builder['Password']=$env:KTQT_DB_PASSWORD
$builder['Encrypt']=$true
$builder['TrustServerCertificate']=$true
$connection=New-Object System.Data.SqlClient.SqlConnection($builder.ConnectionString)
try {
    $connection.Open()
    $command=$connection.CreateCommand()
    $command.CommandText=@'
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @lockResult INT;
EXEC @lockResult=sys.sp_getapplock @Resource=N'KTQT03.initialize', @LockMode='Exclusive', @LockOwner='Transaction', @LockTimeout=30000;
IF @lockResult<0 THROW 50001, 'Cannot acquire initialization lock', 1;
'@
    $command.CommandText += [Environment]::NewLine + (Get-Content -Raw -Encoding UTF8 (Join-Path $root 'src\main\resources\db\demo-users.sql')) + [Environment]::NewLine + 'COMMIT TRANSACTION;'
    [void]$command.ExecuteNonQuery()
    Write-Host 'Demo accounts are available. Existing accounts were preserved. See README.md.'
} finally { $connection.Dispose() }
