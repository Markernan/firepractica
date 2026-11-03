@echo off
rem Doble clic: muestra el SHA-1 y el hash de clave de Facebook de esta PC.
powershell -ExecutionPolicy Bypass -File "%~dp0obtener-huellas.ps1"
pause
