package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.UtilisateurCrud;

public class StatisticsDashboard {

    @FXML private Canvas canvasInvest, canvasProp;
    @FXML private Label lblInvest, lblProp;

    private final UtilisateurCrud crud = new UtilisateurCrud();

    @FXML
    public void initialize() {
        long invest = crud.afficher().stream()
                .filter(Utilisateur::isPatient)
                .count();

        long prop = crud.afficher().stream()
                .filter(Utilisateur::isMedecin)
                .count();

        long total = invest + prop;

        draw(canvasInvest, total == 0 ? 0 : (double) invest / total, "#3b82f6");
        draw(canvasProp, total == 0 ? 0 : (double) prop / total, "#22c55e");

        lblInvest.setText(String.valueOf(invest));
        lblProp.setText(String.valueOf(prop));
    }

    private void draw(Canvas c, double percent, String color) {
        GraphicsContext g = c.getGraphicsContext2D();
        g.clearRect(0,0,200,200);

        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(18);
        g.strokeOval(20,20,160,160);

        g.setStroke(Color.web(color));
        g.strokeArc(20,20,160,160,90,-360*percent, ArcType.OPEN);
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/AdminDashboard.fxml")
            );
            Stage stage = (Stage) lblInvest.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}