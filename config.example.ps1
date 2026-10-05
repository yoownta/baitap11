# Copy to config.local.ps1; never commit local credentials.
$env:KTQT_DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=BAITAP11_24133054;encrypt=true;trustServerCertificate=true'
$env:KTQT_DB_USER = 'YOUR_SQL_USER'
$env:KTQT_DB_PASSWORD = 'YOUR_SQL_PASSWORD'
$env:KTQT_DB_INIT = 'true'
$env:KTQT_SMTP_HOST = 'smtp.gmail.com'
$env:KTQT_SMTP_PORT = '587'
$env:KTQT_SMTP_USER = 'YOUR_SENDER_EMAIL'
$env:KTQT_SMTP_PASSWORD = 'YOUR_SMTP_APP_PASSWORD'
