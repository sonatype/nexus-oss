@rem .
@rem Copyright (c) 2009-2010 Sonatype, Inc. All rights reserved.
@rem .

@if "%WRAPPER_DEBUG%" == "" @echo off

if "%OS%"=="Windows_NT" goto begin
echo Unsupported Windows version: %OS%
pause
goto :eof

:begin
setlocal enableextensions

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

:pickwrapper
set WRAPPER_EXE=%DIRNAME%wrapper.exe
if exist "%WRAPPER_EXE%" goto pickconfig
echo Missing wrapper executable: %WRAPPER_EXE%
pause
goto end

:pickconfig
set WRAPPER_CONF=%~f1
if not "%WRAPPER_CONF%" == "" goto execute
set WRAPPER_CONF=%DIRNAME%..\..\jsw\conf\wrapper.conf
if exist "%WRAPPER_CONF%" goto execute
echo Missing wrapper config: %WRAPPER_CONF%
pause
goto end

:execute
"%WRAPPER_EXE%" -p "%WRAPPER_CONF%"
goto end

:end
endlocal

if not errorlevel 1 goto finish
pause

:finish
cmd /C exit /B %ERRORLEVEL%
