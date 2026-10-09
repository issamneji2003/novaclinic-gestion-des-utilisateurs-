package controllers;

import entities.Utilisateur;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import services.UtilisateurCrud;
import tools.Session;

import services.FaceService;
import services.UtilisateurCrud;
import tools.Session;
import javafx.scene.control.Alert;



import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;

public class AdminDashboard {

    /* ===== UI ===== */
    @FXML private ListView<Utilisateur> lvUsers;
    @FXML private TextField tfSearch, tfCin, tfTel, tfNom, tfPrenom, tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole, cbFilterRole;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnRefresh, btnChangePass, btnLogout;
    @FXML private Canvas canvasInvest, canvasProp;
    @FXML private Label lblInvest, lblProp;
    @FXML private ToggleButton darkToggle;



    private FaceService faceService = new FaceService();
    private UtilisateurCrud utilisateurCrud = new UtilisateurCrud();


    private boolean darkMode = false;

    /* ===== DATA ===== */
    private final UtilisateurCrud crud = new UtilisateurCrud();
    private ObservableList<Utilisateur> allUsers;
    private Utilisateur selectedUser;

    @FXML // <--- Hethi lezem tziidha bech el FXML ychouf el fonction
    private void handleRegisterFace() {
        // 1. Nakhou el ID mte3 el Admin elli connecti tawa
        int adminId = Session.getCurrentUser().getId();

        // 2. N'khadmu el caméra bech t'sajel el taswira fil dossier resources/faces
        boolean success = faceService.detectAndSave(adminId);

        if (success) {
            // 3. Path elli bech n'sajlouh fil Base de données
            String imagePath = "faces/admin_" + adminId + ".jpg";

            // 4. Update el base de données
            utilisateurCrud.updateFaceImage(adminId, imagePath);

            // 5. Update el Session mte3na bech el login ya3ref elli wallat famma taswira
            Session.getCurrentUser().setFaceImage(imagePath);

            showAlert("Succès", "Votre visage a été enregistré avec succès !");
        } else {
            showAlert("Erreur", "Impossible de détecter un visage. Vérifiez votre caméra.");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }




    @FXML
    private void blockUser() {
        if (selectedUser == null) return;
        crud.blockUser(selectedUser.getId());
        show("Bloqué", "Utilisateur bloqué");
        refreshAll();
    }

    @FXML
    private void unblockUser() {
        if (selectedUser == null) return;
        crud.unblockUser(selectedUser.getId());
        show("Débloqué", "Utilisateur débloqué");
        refreshAll();
    }





    /* ================= INITIALIZE ================= */
    @FXML
    public void initialize() {

        // 🔐 GUARD ADMIN SESSION
        Utilisateur admin = Session.getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            redirectToLogin();
            return;
        }

        cbRole.setItems(FXCollections.observableArrayList(
                "ROLE_PATIENT",
                "ROLE_MEDECIN"
        ));

        cbFilterRole.setItems(FXCollections.observableArrayList(
                "ALL",
                "ROLE_PATIENT",
                "ROLE_MEDECIN"
        ));
        cbFilterRole.setValue("ALL");

        loadUsers();
        loadDonutStats();

        lvUsers.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(
                            4,
                            new Label("👤 " + u.getNom().toUpperCase() + " " + u.getPrenom()),
                            new Label("🪪 CIN : " + u.getCin()),
                            new Label("📧 " + u.getEmail()),
                            new Label("🎭 " + u.getRoles())
                    );
                    box.getStyleClass().add("user-card");
                    setGraphic(box);
                }
            }
        });

        lvUsers.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            if (n != null) {
                selectedUser = n;
                fillFields(n);
            }
        });

        tfSearch.textProperty().addListener((o, a, n) -> applyFilters());
        cbFilterRole.setOnAction(e -> applyFilters());

        btnAdd.setOnAction(e -> ajouter());
        btnUpdate.setOnAction(e -> modifier());
        btnDelete.setOnAction(e -> supprimer());
        btnRefresh.setOnAction(e -> refreshAll());
        btnChangePass.setOnAction(e -> changerMotDePasse());
        btnLogout.setOnAction(e -> logout());
        darkToggle.setOnAction(e -> toggleTheme());

        /* ===== FIX PAGE BLANCHE ===== */
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
                darkMode = false;
            }
        });
    }

    /* ================= DARK MODE ================= */
    private void toggleTheme() {
        Scene scene = darkToggle.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                getClass().getResource("/admin.css").toExternalForm()
        );

        if (!darkMode) {
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

        darkMode = !darkMode;
    }

    /* ================= CRUD ================= */
    private void ajouter() {
        try {
            Utilisateur u = new Utilisateur();

            u.setCin(Integer.parseInt(tfCin.getText()));
            u.setTel(tfTel.getText());
            u.setNom(tfNom.getText());
            u.setPrenom(tfPrenom.getText());
            u.setEmail(tfEmail.getText());
            u.setPassword(tfPassword.getText());
            u.setRoles("[\"" + cbRole.getValue() + "\"]");

            crud.ajouter(u);
            show("Succès", "Utilisateur ajouté");
            refreshAll();

        } catch (Exception e) {
            show("Erreur", "Champs invalides");
        }
    }

    private void modifier() {
        if (selectedUser == null) return;

        selectedUser.setCin(Integer.parseInt(tfCin.getText()));
        selectedUser.setTel(tfTel.getText());
        selectedUser.setNom(tfNom.getText());
        selectedUser.setPrenom(tfPrenom.getText());
        selectedUser.setEmail(tfEmail.getText());
        selectedUser.setRoles("[\"" + cbRole.getValue() + "\"]");

        crud.modifier(selectedUser);

        show("Succès", "Utilisateur modifié");
        refreshAll();
    }

    private void supprimer() {
        if (selectedUser == null) return;
        crud.supprimer(selectedUser.getId());
        show("Supprimé", "Utilisateur supprimé");
        refreshAll();
    }

    private void changerMotDePasse() {
        if (selectedUser == null || tfPassword.getText().isBlank()) return;
        crud.changerMotDePasse(selectedUser.getId(), tfPassword.getText());
        tfPassword.clear();
        show("Succès", "Mot de passe changé");
    }

    /* ================= HELPERS ================= */
    private void loadUsers() {
        allUsers = FXCollections.observableArrayList(crud.afficher());
        lvUsers.setItems(allUsers);
    }

    private void refreshAll() {
        loadUsers();
        loadDonutStats();
        tfSearch.clear();
        cbFilterRole.setValue("ALL");
    }

    private void applyFilters() {
        String k = tfSearch.getText().toLowerCase();
        String r = cbFilterRole.getValue();

        lvUsers.setItems(allUsers.filtered(u ->
                (k.isEmpty() || u.getEmail().toLowerCase().contains(k)) &&
                        (r.equals("ALL") || u.hasRole(r))
        ));
    }

    private void fillFields(Utilisateur u) {
        tfCin.setText(String.valueOf(u.getCin()));
        tfTel.setText(u.getTel());
        tfNom.setText(u.getNom());
        tfPrenom.setText(u.getPrenom());
        tfEmail.setText(u.getEmail());
        cbRole.setValue(
                u.isMedecin() ? "ROLE_MEDECIN" : "ROLE_PATIENT"
        );
    }

    @FXML
    public void sortByNom() {
        allUsers.sort((a, b) -> a.getNom().compareToIgnoreCase(b.getNom()));
    }

    /* ================= PDF ================= */
    @FXML
    public void exportPdf() {
        try {
            String path = "C:/Users/hp/Projet3AJava-NovaClinic/users.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            document.add(new Paragraph("NovaClinic - Users List\n\n"));

            for (Utilisateur u : lvUsers.getItems()) {
                document.add(new Paragraph("CIN : " + u.getCin()));
                document.add(new Paragraph("Nom : " + u.getNom()));
                document.add(new Paragraph("Prenom : " + u.getPrenom()));
                document.add(new Paragraph("Email : " + u.getEmail()));
                document.add(new Paragraph("Role : " + u.getRoles()));
                document.add(new Paragraph("-----------------------------"));
            }

            document.close();
            show("PDF", "Export effectué ✔\n" + path);

        } catch (Exception e) {
            e.printStackTrace();
            show("Erreur", "Erreur export PDF");
        }
    }

    /* ================= DONUT ================= */
    private void loadDonutStats() {
        long invest = crud.afficher().stream()
                .filter(Utilisateur::isPatient)
                .count();

        long prop = crud.afficher().stream()
                .filter(Utilisateur::isMedecin)
                .count();

        long total = invest + prop;
        if (total == 0) total = 1;

        drawDonut(canvasInvest, (double) invest / total, "#6d28d9");
        drawDonut(canvasProp, (double) prop / total, "#dc2626");

        lblInvest.setText(String.valueOf(invest));
        lblProp.setText(String.valueOf(prop));
    }

    private void drawDonut(Canvas canvas, double percent, String color) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, 120, 120);

        g.setLineWidth(10);
        g.setStroke(Color.web("#e5e7eb"));
        g.strokeOval(10, 10, 100, 100);

        g.setStroke(Color.web(color));
        g.strokeArc(10, 10, 100, 100, 90, -360 * percent, ArcType.OPEN);
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

    private void show(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/authentification.fxml")
            );
            Stage stage = (Stage) lvUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}