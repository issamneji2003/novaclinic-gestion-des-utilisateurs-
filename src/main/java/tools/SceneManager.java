package tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    // نربطو الـ Stage الرئيسي
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    // نبدلو Scene عن طريق FXML
    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/" + fxml)
            );
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
