module com.example.twodpredictioncompressorgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.twodpredictioncompressorgui to javafx.fxml;
    exports com.example.twodpredictioncompressorgui;
}