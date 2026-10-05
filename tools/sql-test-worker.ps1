# Internal loopback integration-test worker. SQL comes only from smoke-test.py.
$ErrorActionPreference='Stop'
[Console]::OutputEncoding=New-Object Text.UTF8Encoding($false)
$m=[regex]::Match($env:KTQT_DB_URL,'jdbc:sqlserver://([^;]+);.*?databaseName=([^;]+)')
$b=New-Object System.Data.SqlClient.SqlConnectionStringBuilder
$b['Data Source']=$m.Groups[1].Value.Replace(':',',')
$b['Initial Catalog']=$m.Groups[2].Value
$b['User ID']=$env:KTQT_DB_USER
$b['Password']=$env:KTQT_DB_PASSWORD
$b['Encrypt']=$true
$b['TrustServerCertificate']=$true
while ($null -ne ($line=[Console]::ReadLine())) {
    $connection=New-Object System.Data.SqlClient.SqlConnection($b.ConnectionString)
    try {
        $connection.Open()
        $command=$connection.CreateCommand()
        $command.CommandText=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($line))
        $value=$command.ExecuteScalar()
        $json=@{ok=$true;value=[string]$value} | ConvertTo-Json -Compress
    } catch {
        $json=@{ok=$false;error=$_.Exception.Message} | ConvertTo-Json -Compress
    } finally { $connection.Dispose() }
    [Console]::WriteLine($json)
}
