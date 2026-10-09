package controllers;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Duration;
import services.UtilisateurCrud;
import tools.Session;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MedecinDashboard {

    /* ================= UI ================= */

    @FXML private TextField tfCin, tfTel, tfNom, tfPrenom, tfEmail;
    @FXML private Button btnSaveProfile, btnMyProfile, btnLogout;
    @FXML private ListView<Utilisateur> lvPatients;
    @FXML private ToggleButton darkToggle;

    /* ================= DATA ================= */

    private final UtilisateurCrud crud = new UtilisateurCrud();
    private Utilisateur currentUser;
    private boolean darkMode = false;

    private boolean profileDialogOpen = false;
    private boolean investorDialogOpen = false;

    /* ================= INIT ================= */

    @FXML
    public void initialize() {

        currentUser = Session.getCurrentUser();

        btnLogout.setOnAction(e -> logout());
        btnSaveProfile.setOnAction(e -> saveProfile());
        btnMyProfile.setOnAction(e -> openProfileDialog());
        darkToggle.setOnAction(e -> toggleTheme());

        loadProfile();
        loadPatients();

        // ✅ FIX الصفحة البيضا (ضروري)
        Platform.runLater(this::applyInitialTheme);
    }

    /* ================= THEME ================= */

    private void applyInitialTheme() {
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
            darkMode = true;
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("/light.css").toExternalForm()
            );
            darkToggle.setText("OFF");
            darkMode = false;
        }
    }

    private void toggleTheme() {
        applyInitialTheme();
    }

    /* ================= PROFILE ================= */

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

    /* ================= PatientS ================= */

    private void loadPatients() {

        List<Utilisateur> Patients = crud.afficher()
                .stream()
                .filter(Utilisateur::isPatient)
                .toList();

        lvPatients.setItems(FXCollections.observableArrayList(Patients));

        lvPatients.setCellFactory(lv -> new ListCell<>() {

            private final Label lblName = new Label();
            private final Label lblEmail = new Label();
            private final Button btnInfo = new Button("Voir infos");
            private final Button btnWhats = new Button("WhatsApp");

            private final VBox infoBox = new VBox(4);
            private final HBox btnBox = new HBox(8);
            private final HBox row = new HBox(20);

            {
                lblName.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
                lblEmail.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

                btnInfo.setStyle("""
                        -fx-background-color:#2563eb;
                        -fx-text-fill:white;
                        -fx-background-radius:8;
                        """);

                btnWhats.setStyle("""
                        -fx-background-color:#25D366;
                        -fx-text-fill:white;
                        -fx-font-weight:bold;
                        -fx-background-radius:8;
                        """);

                btnInfo.setOnAction(e -> {
                    Utilisateur u = getItem();
                    if (u != null) openInvestorInfoDialog(u);
                });

                btnWhats.setOnAction(e -> {
                    Utilisateur u = getItem();
                    if (u != null) openWhatsApp(u);
                });

                infoBox.getChildren().addAll(lblName, lblEmail);
                btnBox.getChildren().addAll(btnInfo, btnWhats);

                row.getChildren().addAll(infoBox, btnBox);
                row.setStyle("-fx-alignment:center-left; -fx-padding:10;");
            }

            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    lblName.setText(user.getNom() + " " + user.getPrenom());
                    lblEmail.setText(user.getEmail());
                    btnWhats.setDisable(user.getTel() == null || user.getTel().isBlank());
                    setGraphic(row);
                }
            }
        });
    }

    /* ================= WHATSAPP ================= */

    private void openWhatsApp(Utilisateur user) {
        try {
            if (!Desktop.isDesktopSupported()) {
                alert("Desktop non supporté");
                return;
            }

            String tel = user.getTel().replaceAll("\\s+", "");
            if (!tel.startsWith("+")) {
                tel = "+216" + tel;
            }

            String msg = "Bonjour " + user.getNom() +
                    ", je vous contacte via NovaClinic.";

            String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            String url = "https://wa.me/" + tel + "?text=" + encoded;

            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur ouverture WhatsApp");
        }
    }

    /* ================= DIALOGS ================= */

    private void openProfileDialog() {
        if (profileDialogOpen) return;
        profileDialogOpen = true;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Profile Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        VBox box = new VBox(15,
                new Label("Name: " + currentUser.getNom()),
                new Label("Email: " + currentUser.getEmail())
        );
        box.setStyle("-fx-padding:20;");
        dialog.getDialogPane().setContent(box);

        FadeTransition ft = new FadeTransition(Duration.millis(300), dialog.getDialogPane());
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        dialog.setOnHidden(e -> profileDialogOpen = false);
        dialog.showAndWait();
    }

    private void openInvestorInfoDialog(Utilisateur investor) {

        if (investorDialogOpen) return;
        investorDialogOpen = true;

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);

        Label years = new Label("Years Experience: " + safe(investor.getYearsExperience()));
        Label profit = new Label("Highest Profit: " + safe(investor.getHighestProfit()));
        Label budget = new Label("Budget: " + safe(investor.getBudget()));

        Button close = new Button("OK");
        close.setStyle("""
            -fx-background-color:#2563eb;
            -fx-text-fill:white;
            -fx-font-weight:bold;
            -fx-background-radius:8;
            -fx-padding:6 18;
            """);

        // ✅ FIX الحقيقي
        close.setOnAction(e -> popup.close());

        VBox card = new VBox(15, years, profit, budget, close);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:25;
            -fx-background-radius:18;
            -fx-alignment:center;
            """);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.35);");

        Scene scene = new Scene(root);
        scene.setFill(null);
        popup.setScene(scene);

        // ✅ FIX مهم
        popup.setOnHidden(e -> investorDialogOpen = false);

        popup.showAndWait();
    }
    private String safe(String v) {
        return v == null || v.isBlank() ? "Not provided" : v;
    }

    /* ================= LOGOUT ================= */

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