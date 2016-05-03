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
if "x%IRONJACAMAR_HOME%" == "x" (
  set "IRONJACAMAR_HOME=%CD%"
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
set IRONJACAMAR_ENDORSED_DIRS=%IRONJACAMAR_HOME%\lib\endorsed

echo ===============================================================================
echo.
echo   IronJacamar
echo.
echo   IRONJACAMAR_HOME: %IRONJACAMAR_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% ^
   -Djava.endorsed.dirs="%IRONJACAMAR_ENDORSED_DIRS%" ^
   -Djava.util.logging.manager=org.jboss.logmanager.LogManager ^
   -Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl ^
   -Dlog4j.defaultInitOverride=true ^
   -jar ..\lib\ironjacamar-sjc.jar %*

if ERRORLEVEL 10 goto RESTART

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
