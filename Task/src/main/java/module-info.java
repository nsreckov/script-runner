module org.example.task {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.fxmisc.richtext;
    requires java.prefs;


    opens org.example.task to javafx.fxml;
    exports org.example.task;
}