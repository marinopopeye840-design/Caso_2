
package ni.edu.uam.caso_2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource("pedidos-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());

        PedidoController controller = fxmlLoader.getController();

        stage.setTitle("Procesamiento de Pedidos");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {

            event.consume();

            controller.cerrar(() -> {

                stage.setOnCloseRequest(null);
                stage.close();
                Platform.exit();

            });
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
