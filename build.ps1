param(
    [string[]]$Tasks = @('testDebugUnitTest', 'assembleDebug', 'lintDebug'),
    [ValidateSet('small', 'base')][string]$VoiceModel = 'small'
)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'D:\Android studio\jbr'
$env:GRADLE_USER_HOME = 'D:\Backup Android Studio\.gradle'
$env:ANDROID_HOME = 'D:\CatatCatat-tools\sdk'
$env:ANDROID_USER_HOME = 'D:\CatatCatat-tools\android-home'
$env:ANDROID_AVD_HOME = 'D:\CatatCatat-tools\avd'
$env:TEMP = 'D:\CatatCatat-tools\temp'
$env:TMP = $env:TEMP
$env:JAVA_TOOL_OPTIONS = '-Djava.io.tmpdir=D:/CatatCatat-tools/temp -Duser.home=D:/CatatCatat-tools'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
foreach ($taskDirectory in @($env:TEMP, $env:ANDROID_USER_HOME)) { New-Item -ItemType Directory -Force -Path $taskDirectory | Out-Null }
if (-not (Test-Path -LiteralPath $env:ANDROID_HOME)) {
    New-Item -ItemType Junction -Path $env:ANDROID_HOME -Target 'D:\Backup Android Studio\Android-SDK\Sdk' | Out-Null
}
$taskKeyBackup = 'D:\Notra-keamanan\debug.keystore'
$taskSigningKey = Join-Path $env:ANDROID_USER_HOME 'debug.keystore'
if (Test-Path -LiteralPath $taskKeyBackup) {
    if (-not (Test-Path -LiteralPath $taskSigningKey)) { Copy-Item -LiteralPath $taskKeyBackup -Destination $taskSigningKey }
    if ((Get-FileHash -LiteralPath $taskKeyBackup).Hash -ne (Get-FileHash -LiteralPath $taskSigningKey).Hash) { throw 'Kunci APK berbeda dari backup Notra. Build dihentikan agar pembaruan tetap kompatibel.' }
}
if (-not (Test-Path -LiteralPath $taskSigningKey)) { throw 'Kunci pembaruan Notra belum tersedia di D. Pulihkan kunci sebelum build.' }
$taskGradle = 'D:\Backup Android Studio\.gradle\wrapper\dists\gradle-8.14-all\c2qonpi39x1mddn7hk5gh9iqj\gradle-8.14\bin\gradle.bat'
if (-not (Test-Path -LiteralPath $taskGradle)) { throw 'Gradle 8.14 belum tersedia di D. Baca README.md.' }
$taskBuildSource = 'D:\CatatCatat-tools\source'
New-Item -ItemType Directory -Force -Path $taskBuildSource | Out-Null
& robocopy.exe $PSScriptRoot $taskBuildSource /E /XD .git .gradle .kotlin .cxx build .codex-remote-attachments /XF *.iml /NFL /NDL /NJH /NJS /NP
if ($LASTEXITCODE -ge 8) { throw 'Gagal menyalin sumber ke folder build D.' }
# Sync authored sources exactly so removed resources cannot survive in the build mirror.
$taskSourceMirror = [IO.Path]::GetFullPath((Join-Path $taskBuildSource 'app\src'))
if ($taskSourceMirror -ne 'D:\CatatCatat-tools\source\app\src') { throw 'Jalur sinkronisasi sumber tidak aman.' }
& robocopy.exe (Join-Path $PSScriptRoot 'app\src') $taskSourceMirror /MIR /NFL /NDL /NJH /NJS /NP
if ($LASTEXITCODE -ge 8) { throw 'Gagal menyinkronkan sumber aplikasi di D.' }
Push-Location $taskBuildSource
try {
    & $taskGradle --project-cache-dir 'D:\CatatCatat-tools\project-cache' --console=plain "-PvoiceModel=$VoiceModel" @Tasks
    if ($LASTEXITCODE -ne 0) { throw "Build gagal: $LASTEXITCODE" }
    if (Test-Path -LiteralPath "$taskBuildSource\app\schemas") { Copy-Item -LiteralPath "$taskBuildSource\app\schemas" -Destination "$PSScriptRoot\app" -Recurse -Force }
    if ($Tasks -contains 'assembleDebug') {
        $taskDist = 'D:\CatatCatat-tools\dist'
        New-Item -ItemType Directory -Force -Path $taskDist | Out-Null
        $taskApkName = if ($VoiceModel -eq 'small') { 'Notra-v1.1.0-Small.apk' } else { 'Notra-v1.1.0-Base-pembanding.apk' }
        Copy-Item -LiteralPath 'D:\CatatCatat-tools\build\app\outputs\apk\debug\app-debug.apk' -Destination (Join-Path $taskDist $taskApkName) -Force
        Write-Output "APK: $taskDist\$taskApkName"
    }
} finally { Pop-Location }
