package controllers;

import entities.Utilisateur;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.GoogleAuthService;
import services.UtilisateurCrud;
import services.LanguageService;
import services.TranslationService;
import tools.SceneManager;
import tools.Session;

public class AuthentificationController {

    @FXML private TextField tfemail;
    @FXML private PasswordField tfmdp;
    @FXML private Button btn_auth;
    @FXML private Button btnGoogle;          // ← NOUVEAU bouton Google
    @FXML private Label lblMessage;
    @FXML private Hyperlink linkForgot, linkRegister;
    @FXML private Label titleLabel, subtitleLabel;

    private final UtilisateurCrud utilisateurCrud = new UtilisateurCrud();
    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    // ================= INIT =================
    @FXML
    private void initialize() {
        String lang = LanguageService.getLang();

        titleLabel.setText(TranslationService.translate("Authentification", lang));
        subtitleLabel.setText(TranslationService.translate("Content de te revoir !", lang));
        tfemail.setPromptText(TranslationService.translate("Email ou Identifiant", lang));
        tfmdp.setPromptText(TranslationService.translate("Mot de passe", lang));
        btn_auth.setText(TranslationService.translate("Se connecter", lang));
        linkForgot.setText(TranslationService.translate("Mot de passe oublié ?", lang));
        linkRegister.setText(TranslationService.translate("Retour à l'inscription", lang));

        lblMessage.setText("");
        linkForgot.setVisible(true);
    }

    // ================= LOGIN CLASSIQUE =================
    @FXML
    private void login() {
        String email = tfemail.getText().trim();
        String password = tfmdp.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Veuillez remplir tous les champs");
            return;
        }

        Utilisateur user = utilisateurCrud.login(email, password);

        if (user == null) {
            attempts++;
            if (attempts < MAX_ATTEMPTS) {
                lblMessage.setText("Email ou mot de passe incorrect (" + attempts + "/3)");
            } else {
                lblMessage.setText("Trop de tentatives. Mot de passe oublié ?");
                btn_auth.setDisable(true);
                linkForgot.setVisible(true);
            }
            return;
        }

        if (user.getActive() == 0) {
            lblMessage.setText("Compte bloqué");
            return;
        }

        attempts = 0;
        btn_auth.setDisable(false);
        linkForgot.setVisible(true);
        showCaptchaPopup(user);
    }

    // ================= GOOGLE SIGN-IN =================
    @FXML
    private void loginWithGoogle() {
        // Désactiver le bouton pendant l'auth
        btnGoogle.setDisable(true);
        lblMessage.setText("Ouverture de Google...");

        // Lancer dans un thread séparé pour ne pas bloquer l'UI JavaFX
        Task<String> googleTask = new Task<>() {
            @Override
            protected String call() {
                // Cette méthode ouvre le navigateur et attend le callback
                return GoogleAuthService.signInWithGoogle();
            }
        };

        googleTask.setOnSucceeded(event -> {
            // On est de retour sur le JavaFX thread
            String email = googleTask.getValue();
            Platform.runLater(() -> {
                btnGoogle.setDisable(false);

                if (email == null) {
                    lblMessage.setText("❌ Connexion Google annulée ou échouée.");
                    return;
                }

                // Chercher l'email dans la base de données
                Utilisateur user = utilisateurCrud.findByEmail(email);

                if (user == null) {
                    // Email Google non trouvé dans la DB
                    lblMessage.setText("❌ Compte Google non enregistré dans NovaClinic.\n" +
                            "Veuillez d'abord vous inscrire avec : " + email);
                    return;
                }

                if (user.getActive() == 0) {
                    lblMessage.setText("⛔ Compte bloqué. Contactez l'administrateur.");
                    return;
                }

                // ✅ Email trouvé → login direct (pas besoin de mot de passe)
                System.out.println("✅ Google Login OK pour : " + email + " | Rôle : " + user.getRoles());
                lblMessage.setText("");
                finishLogin(user);
            });
        });

        googleTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                btnGoogle.setDisable(false);
                lblMessage.setText("❌ Erreur lors de la connexion Google.");
                googleTask.getException().printStackTrace();
            });
        });

        // Démarrer le thread
        Thread thread = new Thread(googleTask);
        thread.setDaemon(true);
        thread.start();
    }

    // ================= CAPTCHA / FACE =================
    private void showCaptchaPopup(Utilisateur user) {
        try {
            if (user.isAdmin()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/face_auth_popup.fxml"));
                Parent root = loader.load();
                FaceAuthController controller = loader.getController();
                controller.initData(user);

                Stage stage = new Stage();
                stage.setTitle("Vérification Faciale Admin");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

                if (controller.isVerified()) {
                    finishLogin(user);
                } else {
                    lblMessage.setText("Échec de la reconnaissance faciale.");
                }
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/captcha_popup.fxml"));
                Parent root = loader.load();
                CaptchaPopupController controller = loader.getController();
                controller.setOnSuccess(() -> finishLogin(user));

                Stage stage = new Stage();
                stage.setTitle("Vérification CAPTCHA");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors de la vérification.");
        }
    }

    // ================= FINAL LOGIN - REDIRECTION PAR RÔLE =================
    private void finishLogin(Utilisateur user) {
        Session.setCurrentUser(user);

        if (user.isAdmin()) {
            SceneManager.switchScene("AdminDashboard.fxml");
        } else if (user.isMedecin()) {
            SceneManager.switchScene("MedecinDashboard.fxml");
        } else {
            // ROLE_PATIENT
            SceneManager.switchScene("PatientDashboard.fxml");
        }
    }

    // ================= NAV =================
    @FXML
    private void forgotPassword() {
        SceneManager.switchScene("ResetMdp.fxml");
    }

    @FXML
    private void goToInscription() {
        SceneManager.switchScene("inscription.fxml");
    }
}