@echo off
echo Starting JGCB Server...
cd /d %~dp0
start "JGCB" javaw -jar jgcb-1.0.0.jar
echo Server started
pause
