package test;

import javafx.application.Application;
import javafx.stage.Stage;
import tools.SceneManager;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setStage(stage);

        SceneManager.switchScene("inscription.fxml");


        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
