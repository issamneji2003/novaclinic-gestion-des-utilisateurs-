package controllers;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.application.Platform;   // ✅
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.UtilisateurCrud;
import tools.Session;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PatientDashboard {

    @FXML private TextField tfCin, tfTel, tfNom, tfPrenom, tfEmail;
    @FXML private Button btnSaveProfile, btnMyProfile, btnLogout;
    @FXML private ListView<Utilisateur> lvMedecins;
    @FXML private ToggleButton darkToggle;

    private final UtilisateurCrud crud = new UtilisateurCrud();
    private Utilisateur currentUser;

    @FXML
    public void initialize() {

        currentUser = Session.getCurrentUser();

        btnLogout.setOnAction(e -> logout());
        btnSaveProfile.setOnAction(e -> saveProfile());
        btnMyProfile.setOnAction(e -> openExtraInfoDialog());

        loadProfile();
        loadMedecins();
        openExtraInfoDialog();

        /* ================= FIX PAGE BLANCHE ================= */
        Platform.runLater(() -> {
            Scene scene = darkToggle.getScene();
            if (scene != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(
                        getClass().getResource("/admin.css").toExternalForm()
                );
                scene.getStylesheets().add(
                        getClass().getResource("/light.css").toExternalForm()
                );
                darkToggle.setText("OFF");
            }
        });
        /* ==================================================== */
    }

    @FXML
    private void toggleTheme() {

        Scene scene = darkToggle.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                getClass().getResource("/admin.css").toExternalForm()
        );

        if (darkToggle.isSelected()) {
            scene.getStylesheets().add(
                    getClass().getResource("/dark.css").toExternalForm()
            );
            darkToggle.setText("ON");
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("/light.css").toExternalForm()
            );
            darkToggle.setText("OFF");
        }
    }

    private void loadProfile() {
        tfCin.setText(String.valueOf(currentUser.getCin()));
        tfTel.setText(currentUser.getTel());
        tfNom.setText(currentUser.getNom());
        tfPrenom.setText(currentUser.getPrenom());
        tfEmail.setText(currentUser.getEmail());
    }

    private void saveProfile() {
        currentUser.setCin(Integer.parseInt(tfCin.getText()));
        currentUser.setTel(tfTel.getText());
        currentUser.setNom(tfNom.getText());
        currentUser.setPrenom(tfPrenom.getText());
        currentUser.setEmail(tfEmail.getText());
        crud.modifier(currentUser);
        alert("Profile updated");
    }

    private void loadMedecins() {

        List<Utilisateur> props = crud.afficher()
                .stream()
                .filter(Utilisateur::isMedecin)
                .toList();

        lvMedecins.setItems(FXCollections.observableArrayList(props));

        lvMedecins.setCellFactory(lv -> new ListCell<>() {

            private final Label lblName = new Label();
            private final Label lblEmail = new Label();
            private final Button btnVoir = new Button("Voir offres");
            private final Button btnWhatsApp = new Button("WhatsApp");
            private final VBox infoBox = new VBox(4);
            private final HBox actionsBox = new HBox(10);
            private final VBox root = new VBox(8);

            {
                lblName.setStyle("-fx-font-weight: bold;");
                btnWhatsApp.getStyleClass().add("whatsapp-button");

                btnVoir.setOnAction(e -> alert("Offres coming soon"));

                btnWhatsApp.setOnAction(e -> {
                    Utilisateur u = getItem();
                    if (u != null && u.getTel() != null && !u.getTel().isBlank()) {
                        openWhatsApp(u);
                    }
                });

                infoBox.getChildren().addAll(lblName, lblEmail);
                actionsBox.getChildren().addAll(btnVoir, btnWhatsApp);
                root.getChildren().addAll(infoBox, actionsBox);
            }

            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    lblName.setText(user.getNom() + " " + user.getPrenom());
                    lblEmail.setText(user.getEmail());
                    btnWhatsApp.setDisable(
                            user.getTel() == null || user.getTel().isBlank()
                    );
                    setGraphic(root);
                }
            }
        });
    }

    private void openWhatsApp(Utilisateur user) {
        try {
            String phone = user.getTel().startsWith("+")
                    ? user.getTel()
                    : "+216" + user.getTel();

            String msg = "Bonjour " + user.getNom()
                    + ", je vous contacte via NovaClinic.";

            String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(
                    new URI("https://wa.me/" + phone + "?text=" + encoded)
            );
        } catch (Exception e) {
            alert("Erreur ouverture WhatsApp");
        }
    }

    private void openExtraInfoDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Additional Information");

        ButtonType saveBtn =
                new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                saveBtn, ButtonType.CANCEL
        );

        TextField tfYears = new TextField(currentUser.getYearsExperience());
        TextField tfProfit = new TextField(currentUser.getHighestProfit());
        TextField tfBudget = new TextField(currentUser.getBudget());

        VBox box = new VBox(15, tfYears, tfProfit, tfBudget);
        box.setStyle("-fx-padding:20;");
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(b -> {
            if (b == saveBtn) {
                currentUser.setYearsExperience(tfYears.getText());
                currentUser.setHighestProfit(tfProfit.getText());
                currentUser.setBudget(tfBudget.getText());
                crud.modifierI(currentUser);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void logout() {
        try {
            Session.clear();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/authentification.fxml")
            );
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}