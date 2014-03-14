@echo off
:: get the directory in which the script reside
set DIR=%~dp0

if not defined STDERRLOG (set STDERRLOG="%DIR%webdriver.log")

java -cp "%DIR%binary\selenium-server-standalone-2.37.0.jar;%DIR%binary\js.jar;%DIR%binary\commons-io-2.2\commons-io-2.2.jar" ^
    org.mozilla.javascript.tools.shell.Main -f "%DIR%launcher-common.js" "%DIR%webdriver-launcher.js" "%DIR%/" %* ^
    2>%STDERRLOG%