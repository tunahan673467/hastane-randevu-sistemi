@echo off
setlocal
cd /d "%~dp0"
set "JAVACMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVACMD=%JAVA_HOME%\bin\java.exe"
if exist "%LOCALAPPDATA%\Temp\ndp-java-tools\jdk-17.0.21.0.2+1\bin\java.exe" set "JAVACMD=%LOCALAPPDATA%\Temp\ndp-java-tools\jdk-17.0.21.0.2+1\bin\java.exe"
echo Tarayicinizda http://localhost:8080 adresini acin.
"%JAVACMD%" -jar "target\hastane-1.0.0.jar"
pause
