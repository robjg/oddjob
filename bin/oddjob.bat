@echo off

set _ODDJOB_HOME=%ODDJOB_HOME%

if "%_ODDJOB_HOME%" == "" set _ODDJOB_HOME=%~dp0..

set _ODDJOB_JAR="%_ODDJOB_HOME%\run-oddjob.jar"

if not exist %_ODDJOB_JAR% goto failNoOddjob

if "%JAVA_HOME%" == "" goto noJavaHome
set _JAVA_CMD=%JAVA_HOME%/bin/javaw.exe
goto haveJavaCmd

:noJavaHome
set _JAVA_CMD=javaw

:haveJavaCmd

set _ARGS=
if "%1" == "" goto noMoreArgs
set _ARGS=%1
shift
:moreArgs
if "%1" == "" goto noMoreArgs
set _ARGS=%_ARGS% %1
shift
goto moreArgs

:noMoreArgs
goto runOddjob

:failNoOddjob
echo "run-oddjob.jar can not be found at %_ODDJOB_JAR%."

fail:
exit /b 1

:runOddjob

start "Oddjob" /b "%_JAVA_CMD%" -jar %_ODDJOB_JAR% %_ARGS%

