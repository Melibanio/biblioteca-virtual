@echo off
chcp 65001 >nul
cd /d "%~dp0"
setlocal

if not exist lib mkdir lib
if not exist out mkdir out

call :baixar h2-2.2.224.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
call :baixar gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
call :baixar jbcrypt-0.4.jar https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar

echo Compilando...
javac -encoding UTF-8 -cp "lib\*" -sourcepath backend\src\main\java -d out backend\src\main\java\br\com\biblioteca\Main.java
if errorlevel 1 (
  echo Erro na compilacao.
  pause
  exit /b 1
)
copy /y backend\src\main\resources\schema.sql out\schema.sql >nul

echo Iniciando em http://localhost:8080 (Ctrl+C para parar)
java -cp "out;lib\*" br.com.biblioteca.Main
pause
exit /b 0

:baixar
if exist "lib\%1" exit /b 0
echo Baixando %1...
powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -Uri '%2' -OutFile 'lib\%1'"
exit /b 0
