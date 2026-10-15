param([string]$Serial = 'emulator-5556', [ValidateSet('small', 'base')][string]$VoiceModel = 'small', [string]$TestClass = 'id.catatcatat.DeviceIntegrationTest')
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build.ps1" -Tasks @('assembleDebug', 'assembleDebugAndroidTest') -VoiceModel $VoiceModel
$taskAdb = 'D:\CatatCatat-tools\sdk\platform-tools\adb.exe'
& $taskAdb -s $Serial install -r 'D:\CatatCatat-tools\build\app\outputs\apk\debug\app-debug.apk'
if ($LASTEXITCODE -ne 0) { throw 'Instalasi APK gagal.' }
& $taskAdb -s $Serial install -r -t 'D:\CatatCatat-tools\build\app\outputs\apk\androidTest\debug\app-debug-androidTest.apk'
if ($LASTEXITCODE -ne 0) { throw 'Instalasi APK pengujian gagal.' }
$taskResultFile = "D:\CatatCatat-tools\device-tests-$VoiceModel-latest.log"
& $taskAdb -s $Serial shell am instrument -w -r -e class $TestClass id.catatcatat.test/androidx.test.runner.AndroidJUnitRunner | Tee-Object -FilePath $taskResultFile
if ((Get-Content -LiteralPath $taskResultFile -Raw) -notmatch 'OK \(\d+ tests\)') { throw 'Pengujian perangkat gagal. Periksa log di D.' }
