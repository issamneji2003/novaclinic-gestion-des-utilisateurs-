package controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import services.CaptchaService;

public class CaptchaPopupController {

    @FXML
    private StackPane captchaPane;

    @FXML
    private TextField tfCaptcha;

    @FXML
    private Label lblError;


    private Runnable onSuccess;

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        CaptchaService.generateCode();
        Canvas img = CaptchaService.generateImage();
        captchaPane.getChildren().setAll(img);
        tfCaptcha.clear();
        lblError.setText("");
    }

    @FXML
    private void validate() {
        if (CaptchaService.verify(tfCaptcha.getText())) {
            if (onSuccess != null) {
                onSuccess.run();
            }
            ((Stage) captchaPane.getScene().getWindow()).close();
        } else {
            lblError.setText("Code incorrect");
            refresh();
        }
    }
}