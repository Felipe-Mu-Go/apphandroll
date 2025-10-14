@echo off
setlocal enabledelayedexpansion
set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%scripts\prepare-wrapper.ps1"
if errorlevel 1 exit /b %errorlevel%
call "%SCRIPT_DIR%gradlew.bat" %*
