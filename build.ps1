param([string[]]$Tasks = @('testDebugUnitTest', 'assembleDebug', 'lintDebug'))
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'D:\Android studio\jbr'
$env:GRADLE_USER_HOME = 'D:\Backup Android Studio\.gradle'
$env:ANDROID_HOME = 'D:\CatatCatat-tools\sdk'
$env:ANDROID_USER_HOME = 'D:\CatatCatat-tools\android-home'
$env:TEMP = 'D:\CatatCatat-tools\temp'
$env:TMP = $env:TEMP
$env:JAVA_TOOL_OPTIONS = '-Djava.io.tmpdir=D:/CatatCatat-tools/temp -Duser.home=D:/CatatCatat-tools'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$taskGradle = 'D:\Backup Android Studio\.gradle\wrapper\dists\gradle-8.14-all\c2qonpi39x1mddn7hk5gh9iqj\gradle-8.14\bin\gradle.bat'
if (-not (Test-Path -LiteralPath $taskGradle)) { throw 'Gradle 8.14 belum tersedia di D. Baca README.md.' }
$taskBuildSource = 'D:\CatatCatat-tools\source'
New-Item -ItemType Directory -Force -Path $taskBuildSource | Out-Null
& robocopy.exe $PSScriptRoot $taskBuildSource /E /XD .git .gradle .kotlin .cxx build /XF *.iml /NFL /NDL /NJH /NJS /NP
if ($LASTEXITCODE -ge 8) { throw 'Gagal menyalin sumber ke folder build D.' }
# Sync authored sources exactly so removed resources cannot survive in the build mirror.
$taskSourceMirror = [IO.Path]::GetFullPath((Join-Path $taskBuildSource 'app\src'))
if ($taskSourceMirror -ne 'D:\CatatCatat-tools\source\app\src') { throw 'Jalur sinkronisasi sumber tidak aman.' }
& robocopy.exe (Join-Path $PSScriptRoot 'app\src') $taskSourceMirror /MIR /NFL /NDL /NJH /NJS /NP
if ($LASTEXITCODE -ge 8) { throw 'Gagal menyinkronkan sumber aplikasi di D.' }
Push-Location $taskBuildSource
try {
    & $taskGradle --project-cache-dir 'D:\CatatCatat-tools\project-cache' --console=plain @Tasks
    if ($LASTEXITCODE -ne 0) { throw "Build gagal: $LASTEXITCODE" }
    if (Test-Path -LiteralPath "$taskBuildSource\app\schemas") { Copy-Item -LiteralPath "$taskBuildSource\app\schemas" -Destination "$PSScriptRoot\app" -Recurse -Force }
} finally { Pop-Location }
