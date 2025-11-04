module org.angel.curso.javafx.chatapp.appjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.angel.curso.javafx.chatapp.appjavafxchat to javafx.fxml;
    exports org.angel.curso.javafx.chatapp.appjavafxchat;
}