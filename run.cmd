@echo off
pushd "%~dp0"
javac --release 21 -encoding UTF-8 -d out -sourcepath src src\main\Main.java
if errorlevel 1 goto end
java -cp out main.Main
:end
set "chess_exit=%errorlevel%"
popd
exit /b %chess_exit%
