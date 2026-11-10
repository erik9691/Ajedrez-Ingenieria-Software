$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $version = '1.11.4'
    $jar = Join-Path $PSScriptRoot "lib/junit-platform-console-standalone-$version.jar"
    $expectedHash = 'B016EF6B1C3454D6D7C2C88CE081DABF289699686AF6622D6E4E2E1B54B4A2FC'
    New-Item -ItemType Directory -Force lib, .build/main, .build/test | Out-Null
    if (-not (Test-Path -LiteralPath $jar)) {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $client = [Net.WebClient]::new()
        try { $client.DownloadFile("https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/$version/junit-platform-console-standalone-$version.jar", $jar) }
        finally { $client.Dispose() }
    }
    $stream = [IO.File]::OpenRead($jar)
    $sha = [Security.Cryptography.SHA256]::Create()
    try { $actualHash = [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '') }
    finally { $stream.Dispose(); $sha.Dispose() }
    if ($actualHash -ne $expectedHash) { throw 'El archivo de JUnit no coincide con la version esperada.' }
    # Compilar de cero evita ejecutar clases borradas de una corrida anterior.
    Get-ChildItem .build/main,.build/test -Filter *.class -Recurse | Remove-Item
    $mainSources = @(Get-ChildItem src/main -Filter *.java -Recurse | ForEach-Object FullName)
    $testSources = @(Get-ChildItem test -Filter *.java -Recurse | ForEach-Object FullName)
    & javac --release 21 -encoding UTF-8 -d .build/main @mainSources
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion del programa.' }
    & javac --release 21 -encoding UTF-8 -cp ".build/main;$jar" -d .build/test @testSources
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion de las pruebas.' }
    & java -jar $jar execute --class-path '.build/main;.build/test' --scan-class-path --fail-if-no-tests --details summary --reports-dir .build/reports --disable-banner
    if ($LASTEXITCODE -ne 0) { throw 'Hay pruebas fallidas. Ver .build/reports.' }
} finally { Pop-Location }
