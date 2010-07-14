@echo off
rem -------------------------------------------------------------------------
rem IronJacamar Script for Windows
rem -------------------------------------------------------------------------

rem $Id: $

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..
if "x%IRON_JACAMAR_HOME%" == "x" (
  set "IRON_JACAMAR_HOME=%CD%"
)
popd

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=run.bat"
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

rem Setup IronJacamar specific properties

rem Setup the java endorsed dirs
set IRON_JACAMAR_ENDORSED_DIRS=%IRON_JACAMAR_HOME%\lib\endorsed

if "x%JAVA_OPTS%" == "x" (
  set "JAVA_OPTS=-Xmx512m"
) else (
  set "JAVA_OPTS=-Xmx512m %JAVA_OPTS%"
)

echo ===============================================================================
echo.
echo   IronJacamar
echo.
echo   IRON_JACAMAR_HOME: %IRON_JACAMAR_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% ^
   -Djava.endorsed.dirs="%IRON_JACAMAR_ENDORSED_DIRS%" ^
   -Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl ^
   -Dlog4j.defaultInitOverride=true ^
   -jar ironjacamar-sjc.jar %*

if ERRORLEVEL 10 goto RESTART

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
