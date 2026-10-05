$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location $root
if (Test-Path (Join-Path $root 'config.local.ps1')) { . (Join-Path $root 'config.local.ps1') }
python (Join-Path $PSScriptRoot 'commerce-smoke-test.py')
if ($LASTEXITCODE -ne 0) { throw 'Commerce test failed. See target/commerce-server.log and target/commerce-results.json.' }
