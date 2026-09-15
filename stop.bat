@echo off
echo Stopping JGCB Server...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
    echo Server stopped (PID: %%a)
    goto :end
)
echo No running server found on port 8080
:end
pause
