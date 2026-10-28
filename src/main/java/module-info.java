module ni.edu.uam.caso_2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.caso_2 to javafx.fxml;
    exports ni.edu.uam.caso_2;
}