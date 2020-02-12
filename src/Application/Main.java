package Application;

import Application.Accueil.ControllerAccueil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Accueil/Accueil.fxml"));
        Parent root = loader.load();
        ControllerAccueil controller = loader.getController();
        controller.setFieldFomat();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Accueil");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        scene.getWindow().sizeToScene();
        primaryStage.show();
    }
}
