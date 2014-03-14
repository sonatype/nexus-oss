@echo off
:: get the directory in which the script reside
set DIR=%~dp0

"%DIR%binary\phantomjs-1.6.0-win32-static\phantomjs.exe" "%DIR%phantomjs-launcher.js" "%DIR%/" %*
