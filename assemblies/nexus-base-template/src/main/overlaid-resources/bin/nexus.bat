@echo off
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

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%

SET KARAF_TITLE=Sonatype Nexus

goto BEGIN

:USAGE
    echo "%PROGNAME% { console | start | stop | restart | status }"
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:RUN
    SET SHIFT=false
    if "%1" == "console" goto :EXECUTE_CONSOLE
    if "%1" == "start" goto :EXECUTE_START
    if "%1" == "stop" goto :EXECUTE_STOP
    if "%1" == "restart" goto :EXECUTE_RESTART
    if "%1" == "status" goto :EXECUTE_STATUS
    goto :USAGE

:EXECUTE_CONSOLE
    shift
    "%DIRNAME%karaf.bat" %1 %2 %3 %4 %5 %6 %7 %8
    goto :EOF

:EXECUTE_START
    shift
    "%DIRNAME%start.bat" %1 %2 %3 %4 %5 %6 %7 %8
    goto :EOF

:EXECUTE_STOP
    shift
    "%DIRNAME%stop.bat" %1 %2 %3 %4 %5 %6 %7 %8
    goto :EOF

:EXECUTE_RESTART
    shift
    call "%DIRNAME%stop.bat" 2>NUL
    "%DIRNAME%start.bat" %1 %2 %3 %4 %5 %6 %7 %8
    goto :EOF

:EXECUTE_STATUS
    shift
    "%DIRNAME%status.bat" %1 %2 %3 %4 %5 %6 %7 %8
    goto :EOF

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE

