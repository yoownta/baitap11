param(
    [int]$Port = 8082,
    [string]$TomcatHome = 'D:\Web\apache-tomcat-11.0.25',
    [switch]$SkipBuild,
    [switch]$SkipLocalConfig,
    [switch]$TestRuntime
)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$localConfig = Join-Path $PSScriptRoot 'config.local.ps1'
if (!$SkipLocalConfig -and (Test-Path -LiteralPath $localConfig)) { . $localConfig }
foreach ($key in @('KTQT_DB_URL','KTQT_DB_USER','KTQT_DB_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($key))) {
        throw "Missing $key. Copy config.example.ps1 to config.local.ps1 and configure it."
    }
}
if ($Port -lt 1024 -or $Port -gt 65535) { throw 'Port must be between 1024 and 65535.' }
if (!(Test-Path -LiteralPath (Join-Path $TomcatHome 'bin\catalina.bat'))) { throw 'TomcatHome is invalid.' }
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$maven = if ($mavenCommand) { $mavenCommand.Source } else { 'D:\Web\apache-maven-3.9.16\bin\mvn.cmd' }
if (!$SkipBuild) {
    $repository = Join-Path $env:USERPROFILE '.m2\repository'
    & $maven "-Dmaven.repo.local=$repository" -DforkCount=0 package
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed. The old WAR has not been deployed.' }
}
$war = Join-Path $PSScriptRoot 'target\ktqt03.war'
if (!(Test-Path -LiteralPath $war)) { throw 'Missing target/ktqt03.war. Build the project first.' }
# Use an isolated Catalina base; never overwrite other projects in the shared Tomcat.
$runtime = Join-Path $PSScriptRoot $(if ($TestRuntime) { '.runtime-test' } else { '.runtime' })
foreach ($folder in @('conf','logs','temp','webapps','work')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $runtime $folder) | Out-Null
}
$conf = Join-Path $runtime 'conf'
if (!(Test-Path -LiteralPath (Join-Path $conf 'web.xml'))) {
    Copy-Item -Path (Join-Path $TomcatHome 'conf\*') -Destination $conf -Recurse -Force
}
$serverXml = Join-Path $conf 'server.xml'
[xml]$server = Get-Content -Raw -LiteralPath (Join-Path $TomcatHome 'conf\server.xml')
$server.Server.SetAttribute('port','-1')
$connector = $server.SelectSingleNode('//Connector[@protocol="HTTP/1.1"]')
if (!$connector) { throw 'Cannot find the HTTP connector in Tomcat server.xml.' }
$connector.SetAttribute('port', [string]$Port)
$connector.SetAttribute('address', '127.0.0.1')
$server.Save($serverXml)
Copy-Item -LiteralPath $war -Destination (Join-Path $runtime 'webapps\ktqt03.war') -Force
$env:CATALINA_HOME = $TomcatHome
$env:CATALINA_BASE = $runtime
$env:CATALINA_TMPDIR = Join-Path $runtime 'temp'
if ([string]::IsNullOrWhiteSpace($env:CATALINA_OPTS)) { $env:CATALINA_OPTS = '-Xms64m -Xmx384m -XX:+UseSerialGC' }
Write-Host "Open http://localhost:$Port/ktqt03/home"
Write-Host 'Stop with Ctrl+C. Database initializes once; existing data is preserved.'
& (Join-Path $TomcatHome 'bin\catalina.bat') run
