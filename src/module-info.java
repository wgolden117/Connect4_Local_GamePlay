module Connect4_Local_GamePlay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.media;

    opens ui;
    opens core;
    opens logic;
}