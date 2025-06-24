@echo off
"C:\Program Files\Java\jdk-17\bin\java.exe" -Dprism.order=sw ^
--module-path "C:\Users\weron\Documents\Libraries\javafx-sdk-21.0.7\lib;C:\Users\weron\Dropbox\Personal Projects\Connect4\Connect4_Local_GamePlay\out\production\Connect4_Local_GamePlay" ^
--add-modules javafx.controls,javafx.fxml,Connect4_Local_GamePlay ^
-m Connect4_Local_GamePlay/ui.Main
pause
