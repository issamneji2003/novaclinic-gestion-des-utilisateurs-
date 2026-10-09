package controllers;

import entities.MdpHash;
import entities.Mailing;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import services.LanguageService;
import services.TranslationService;
import services.UtilisateurCrud;
import tools.SceneManager;

public class ResetMdpController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label lblMessage;

    @FXML private Label titleLabel, subtitleLabel;
    @FXML private Button btnReset;
    @FXML private Hyperlink linkBack;

    private final UtilisateurCrud crud = new UtilisateurCrud();

    @FXML
    private void initialize() {
        // ✅ SAME LOGIC AS AUTHENTIFICATION
        String lang = LanguageService.getLang();

        titleLabel.setText(
                TranslationService.translate("Réinitialisation du mot de passe", lang)
        );
        subtitleLabel.setText(
                TranslationService.translate("Veuillez choisir un nouveau mot de passe", lang)
        );

        passwordField.setPromptText(
                TranslationService.translate("Nouveau mot de passe", lang)
        );
        confirmPasswordField.setPromptText(
                TranslationService.translate("Confirmer mot de passe", lang)
        );
        tfEmail.setPromptText(
                TranslationService.translate("Adresse email", lang)
        );

        btnReset.setText(
                TranslationService.translate("Réinitialiser", lang)
        );
        linkBack.setText(
                TranslationService.translate("Retour à la connexion", lang)
        );
    }

    @FXML
    private void resetPassword() {

        String email = tfEmail.getText().trim().toLowerCase();
        String pass1 = passwordField.getText();
        String pass2 = confirmPasswordField.getText();

        if (email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            lblMessage.setText(
                    TranslationService.translate(
                            "Veuillez remplir tous les champs",
                            LanguageService.getLang()
                    )
            );
            return;
        }

        if (!pass1.equals(pass2)) {
            lblMessage.setText(
                    TranslationService.translate(
                            "Les mots de passe ne correspondent pas",
                            LanguageService.getLang()
                    )
            );
            return;
        }

        boolean updated = crud.updatePasswordByEmail(
                email,
                MdpHash.hashPassword(pass1)
        );

        if (!updated) {
            lblMessage.setText(
                    TranslationService.translate(
                            "Email introuvable",
                            LanguageService.getLang()
                    )
            );
            return;
        }

        lblMessage.setStyle("-fx-text-fill:green;");
        lblMessage.setText(
                TranslationService.translate(
                        "Mot de passe modifié avec succès",
                        LanguageService.getLang()
                )
        );

        new Thread(() ->
                Mailing.sendEmail(
                        email,
                        "NovaClinic - Mot de passe modifié",
                        "Votre mot de passe a été modifié avec succès."
                )
        ).start();

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> SceneManager.switchScene("authentification.fxml"));
        pause.play();
    }

    @FXML
    private void backToLogin() {
        SceneManager.switchScene("authentification.fxml");
    }
}