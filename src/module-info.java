module Connect4_Local_GamePlay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    opens ui;
    opens core;
    opens logic;
}