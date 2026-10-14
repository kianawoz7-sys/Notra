param([string]$Serial)
$ErrorActionPreference = 'Stop'
$env:ANDROID_USER_HOME = 'D:\CatatCatat-tools\android-home'
$env:ANDROID_SDK_HOME = 'D:\CatatCatat-tools'
$taskAdb = 'D:\CatatCatat-tools\sdk\platform-tools\adb.exe'
$taskSelector = if ($Serial) { @('-s', $Serial) } else { @('-d') }
& $taskAdb @taskSelector get-state
if ($LASTEXITCODE -ne 0) { throw 'Hubungkan HP lewat USB dan izinkan USB debugging di layar HP.' }
& $taskAdb @taskSelector logcat -d -v threadtime -s 'CatatVoice:V' '*:S' | Tee-Object -FilePath 'D:\CatatCatat-tools\phone-voice.log'
if ($LASTEXITCODE -ne 0) { throw 'Belum dapat membaca log HP.' }
