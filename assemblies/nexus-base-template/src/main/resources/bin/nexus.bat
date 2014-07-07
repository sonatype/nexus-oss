@REM
@REM Sonatype Nexus (TM) Open Source Version
@REM Copyright (c) 2007-2014 Sonatype, Inc.
@REM All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
@REM
@REM This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
@REM which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
@REM
@REM Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
@REM of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
@REM Eclipse Foundation. All other trademarks are the property of their respective owners.
@REM

@if "%WRAPPER_DEBUG%" == "" @echo off

if "%OS%"=="Windows_NT" goto begin
echo Unsupported Windows version: %OS%
pause
goto :eof

:begin
setlocal enableextensions

set DIRNAME=%~dp0%
if "%DIRNAME%" == "" set DIRNAME=.\
set PROGNAME=%~nx0%

for /F %%v in ('echo %1^|findstr "^console$ ^start$ ^stop$ ^restart$ ^status$ ^dump"') do call :exec set COMMAND=%%v

if "%COMMAND%" == "" (
    echo Usage: %PROGNAME% { console : start : stop : restart : status : dump }
    pause
    goto end
) else (
    shift
)

call :%COMMAND%
if errorlevel 1 pause
goto end

:console
%DIRNAME%\karaf
goto :eof

:start
%DIRNAME%\start
goto :eof

:stop
%DIRNAME%\stop
goto :eof

:status
%DIRNAME%\status
goto :eof

:dump
echo Not yet implemented
goto :eof

:restart
call :stop
call :start
goto :eof

:exec
%*
goto :eof

:end
endlocal

:finish
cmd /C exit /B %ERRORLEVEL%
