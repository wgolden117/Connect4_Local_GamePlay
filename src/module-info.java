module Connect4_Local_GamePlay {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.media;
    requires java.desktop;

    opens ui to javafx.fxml;
    opens core to javafx.fxml;
    opens logic to javafx.fxml;
    opens animations to javafx.fxml;

    exports ui;
    exports core;
    exports logic;
    exports animations;
}
