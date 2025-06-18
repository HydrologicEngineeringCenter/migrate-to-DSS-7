@echo off
setlocal

REM Set APP_HOME to parent of the script location
set "SCRIPT_DIR=%~dp0"
set "APP_HOME=%SCRIPT_DIR%\.."
pushd "%APP_HOME%" >nul
for %%I in (.) do set "APP_HOME=%%~fI"
popd

REM Set native and lib folders
set "NATIVES_FOLDER=%APP_HOME%\natives"
set "LIB_FOLDER=%APP_HOME%\lib"

REM <<< Replace this line with a specific Java path before distribution >>>
set "JAVA_EXE=__REPLACE_WITH_JAVA_PATH__"
if "%JAVA_EXE%"=="__REPLACE_WITH_JAVA_PATH__" (
    set "JAVA_EXE=%NATIVES_FOLDER%\jre\bin\java.exe"
)

REM Set Java lib path and program class
set "JAVA_LIB_PATH=%NATIVES_FOLDER%;%NATIVES_FOLDER%\javaHeclib"
set "JAVA_LIB_OPT=-Djava.library.path=%JAVA_LIB_PATH%"
set "PROG=api.MigrateToDss7"

REM Execute silently
"%JAVA_EXE%" %JAVA_LIB_OPT% -classpath "%LIB_FOLDER%\*" %PROG% %*
endlocal & exit /b %errorlevel%