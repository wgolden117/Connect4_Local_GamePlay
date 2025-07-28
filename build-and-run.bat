@echo off
setlocal enabledelayedexpansion

:: === FORCE JAVA 18 ===
set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.2.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "JDK_MODS=%JAVA_HOME%\jmods"
echo Using Java from: %JAVA_HOME%
java -version
echo.

:: === CONFIG ===
set "JAVAFX_LIB=C:\Users\Weronika Golden\Documents\Libraries\javafx-sdk-21.0.7\lib"
set "JAVAFX_BIN=C:\Users\Weronika Golden\Documents\Libraries\javafx-sdk-21.0.7\bin"

:: === Kill any existing instance ===
echo Killing any running Connect4...
taskkill /F /IM Connect4.exe >nul 2>&1
taskkill /F /IM Connect4-1.0.exe >nul 2>&1

:: === Clean old build ===
echo Cleaning previous build...
rmdir /S /Q dist 2>nul
rmdir /S /Q runtime 2>nul
rmdir /S /Q bin 2>nul
del app\Connect4_Local_GamePlay.jar 2>nul

:: === Compile ===
echo Compiling Java files...
mkdir bin
javac --module-path "%JAVAFX_LIB%" ^
  --add-modules javafx.controls,javafx.fxml,javafx.swing ^
  -d bin ^
  src/module-info.java src/core/*.java src/logic/*.java src/ui/*.java src/animations/*.java

:: === Copy resources to bin ===
echo Copying sound files...
xcopy resources\* bin\ /E /I /Y

:: === Create executable JAR ===
echo Creating JAR file...
jar --create ^
 --file=app\Connect4_Local_GamePlay.jar ^
 --main-class=ui.Main ^
 --module-version=1.0 ^
 -C bin .

:: === Create custom Java runtime ===
echo Creating Java runtime image...
jlink ^
  --module-path "%JDK_MODS%;%JAVAFX_LIB%;app" ^
  --add-modules Connect4_Local_GamePlay,javafx.controls,javafx.fxml,javafx.swing ^
  --output runtime

:: === Copy JavaFX native DLLs ===
echo Copying JavaFX native DLLs...
xcopy "%JAVAFX_BIN%\*.dll" runtime\bin\ /Y >nul

:: === Copy runtime into final app folder ===
xcopy runtime dist\Connect4\runtime\ /E /I /Y >nul

:: === Package app-image with jpackage ===
echo Packaging app-image with jpackage...
jpackage ^
  --type app-image ^
  --name Connect4 ^
  --input app ^
  --main-jar Connect4_Local_GamePlay.jar ^
  --main-class ui.Main ^
  --runtime-image runtime ^
  --icon connect4.ico ^
  --dest dist\Connect4App ^
  --java-options "--module-path=app;lib" ^
  --java-options "--add-modules=Connect4_Local_GamePlay,javafx.controls,javafx.fxml,javafx.swing"

:: === Package Windows Installer with jpackage ===
echo Packaging Windows Installer with jpackage...
jpackage ^
  --type exe ^
  --name Connect4Installer ^
  --input app ^
  --main-jar Connect4_Local_GamePlay.jar ^
  --main-class ui.Main ^
  --runtime-image runtime ^
  --icon connect4.ico ^
  --dest dist ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --win-menu-group "Connect4 Game" ^
  --app-version 2.2 ^
  --vendor "Weronika Golden" ^
  --java-options "--module-path=app;lib" ^
  --java-options "--add-modules=Connect4_Local_GamePlay,javafx.controls,javafx.fxml,javafx.swing"

:: === Debug run (JAR) ===
echo.
echo === Trying JAR directly for debug ===
echo (This helps diagnose if the .exe fails to launch)
java --module-path "runtime;app;%JAVAFX_LIB%" ^
  --add-modules Connect4_Local_GamePlay ^
  -m Connect4_Local_GamePlay/ui.Main
pause

:: === Run the new app-image EXE ===
echo Launching Connect4 app-image executable...
start "" "dist\Connect4App\Connect4\Connect4.exe"
echo Done. If the GUI still closes, check error.log
pause


