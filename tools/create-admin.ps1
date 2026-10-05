param([string]$Username)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
if (Test-Path (Join-Path $root 'config.local.ps1')) { . (Join-Path $root 'config.local.ps1') }
if ([string]::IsNullOrWhiteSpace($Username)) { $Username=Read-Host 'Activated username to promote to admin' }
if ($Username -notmatch '^[A-Za-z0-9_.-]{3,50}$') { throw 'Invalid username.' }
$match=[regex]::Match($env:KTQT_DB_URL,'jdbc:sqlserver://([^;]+);.*?databaseName=([^;]+)')
if (!$match.Success) { throw 'Database URL must include host:port and databaseName.' }
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
    $command.CommandText='UPDATE dbo.Users SET Admin=1 WHERE Username=@username AND Active=1;'
    [void]$command.Parameters.Add('@username',[System.Data.SqlDbType]::NVarChar,50)
    $command.Parameters['@username'].Value=$Username
    if ($command.ExecuteNonQuery() -ne 1) { throw 'No activated account found. Register and verify OTP first.' }
    Write-Host "Admin enabled for $Username. You can log in now."
} finally { $connection.Dispose() }
