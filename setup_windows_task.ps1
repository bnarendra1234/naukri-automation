# PowerShell Script to register daily Task Scheduler for Naukri Automation at 08:30 AM IST
$TaskName = "NaukriDailyJobApply"
$ScriptDir = $PSScriptRoot
$BatPath = Join-Path $ScriptDir "run_daily.bat"

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Registering Windows Task Scheduler for Naukri Bot" -ForegroundColor Green
Write-Host "Scheduled Time: 08:30 AM Daily" -ForegroundColor Yellow
Write-Host "Batch File: $BatPath" -ForegroundColor White
Write-Host "=====================================================" -ForegroundColor Cyan

# Unregister old task if it exists
Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false -ErrorAction SilentlyContinue

# Define action and trigger
$Action = New-ScheduledTaskAction -Execute $BatPath -WorkingDirectory $ScriptDir
$Trigger = New-ScheduledTaskTrigger -Daily -At "08:30AM"
$Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable

# Register task
try {
    Register-ScheduledTask -TaskName $TaskName -Action $Action -Trigger $Trigger -Settings $Settings -Description "Runs Naukri automated job applier and profile freshness booster every day at 8:30 AM IST."
    Write-Host "[SUCCESS] Task '$TaskName' has been registered successfully!" -ForegroundColor Green
    Write-Host "It will automatically run every morning at 08:30 AM." -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to register task: $_" -ForegroundColor Red
    Write-Host "Tip: Run PowerShell as Administrator." -ForegroundColor Yellow
}
