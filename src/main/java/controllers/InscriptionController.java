package controllers;

import entities.SmsSender;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UtilisateurCrud;
import services.TranslationService;
import services.LanguageService;

public class InscriptionController {

    @FXML private TextField tfcin, tfnum_tel, tfnom, tfprenom, tfemail;
    @FXML private PasswordField tfmdp;
    @FXML private TextField tfshowpassword;
    @FXML private CheckBox show;

    @FXML private RadioButton rbPatient, rbMedecin;
    @FXML private Button btn_inscri;@FXML private Hyperlink hyperlink;@FXML private Label titleLabel, subtitleLabel;
    @FXML private MenuButton btnLang;

    private final UtilisateurCrud utilisateurCrud = new UtilisateurCrud();
    private static final String ADMIN_PHONE = "+21629750279";

    @FXML
    private void initialize() {

        // 🔹 password toggle
        tfshowpassword.setVisible(false);
        show.setOnAction(e -> togglePassword());

        // 🔹 roles
        ToggleGroup group = new ToggleGroup();
        rbPatient.setToggleGroup(group);
        rbMedecin.setToggleGroup(group);
        rbPatient.setSelected(true);

        // 🔹 apply saved language
        translatePage(LanguageService.getLang());
    }

    // ===== 🌍 LANG =====
    @FXML
    private void setFrench() {
        LanguageService.setLang("fr");
        btnLang.setText("🇫🇷 Français");
        translatePage("fr");
    }

    @FXML
    private void setEnglish() {
        LanguageService.setLang("en");
        btnLang.setText("🇬🇧 English");
        translatePage("en");
    }

    @FXML
    private void setArabic() {
        LanguageService.setLang("ar");
        btnLang.setText("🇹🇳 العربية");
        translatePage("ar");
    }

    private void translatePage(String lang) {

        titleLabel.setText(TranslationService.translate("Inscription", lang));
        subtitleLabel.setText(TranslationService.translate("Créez votre compte", lang));

        tfcin.setPromptText(TranslationService.translate("CIN", lang));
        tfnum_tel.setPromptText(TranslationService.translate("Numéro téléphone", lang));
        tfnom.setPromptText(TranslationService.translate("Nom", lang));
        tfprenom.setPromptText(TranslationService.translate("Prénom", lang));
        tfemail.setPromptText(TranslationService.translate("Adresse email", lang));
        tfmdp.setPromptText(TranslationService.translate("Mot de passe", lang));

        show.setText(TranslationService.translate("Voir mot de passe", lang));
        rbPatient.setText(TranslationService.translate("Patient", lang));
        rbMedecin.setText(TranslationService.translate("Medecin", lang));
        btn_inscri.setText(TranslationService.translate("S'inscrire", lang));
        hyperlink.setText(TranslationService.translate("Vous avez déjà un compte ?", lang));
    }

    // ===== SAVE =====
    @FXML
    private void savePerson() {

        String cinStr = tfcin.getText().trim();
        String tel = tfnum_tel.getText().trim();
        String nom = tfnom.getText().trim();
        String prenom = tfprenom.getText().trim();
        String email = tfemail.getText().trim();
        String password = tfmdp.getText();

        if (cinStr.isEmpty() || tel.isEmpty() || nom.isEmpty()
                || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            alert("Veuillez remplir tous les champs");
            return;
        }

        if (!cinStr.matches("^[01][0-9]{7}$")) {
            alert("CIN invalide");
            return;
        }

        if (!tel.matches("^[0-9]{8}$")) {
            alert("Numéro téléphone invalide");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            alert("Email invalide");
            return;
        }

        if (password.length() < 6) {
            alert("Mot de passe minimum 6 caractères");
            return;
        }

        int cin = Integer.parseInt(cinStr);

        if (utilisateurCrud.utilisateurExiste(cin, email)) {
            alert("Utilisateur existe déjà");
            return;
        }

        String roleLabel;
        String role;

        if (rbMedecin.isSelected()) {
            role = "[\"ROLE_MEDECIN\"]";
            roleLabel = "Medecin";
        } else {
            role = "[\"ROLE_PATIENT\"]";
            roleLabel = "Patient";
        }

        Utilisateur u = new Utilisateur();

        u.setCin(cin);
        u.setTel(tel);
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setPassword(password);
        u.setRoles(role);
        u.generateIdentifiant();
        utilisateurCrud.ajouter(u);

         // ✅ ENVOI EMAIL
        String sujet = "Bienvenue chez NovaClinic - Votre Compte";
        String message = "Bonjour " + nom + " " + prenom + ",\n\n" +
                         "Nous sommes ravis de vous compter parmi nous.\n" +
                         "Voici votre IDENTIFIANT UNIQUE pour vous connecter : " + u.getIdentifiant() + "\n\n" +
                         "Vous pouvez utiliser cet identifiant ou votre adresse email pour accéder à votre compte.\n" +
                         "Merci et à très bientôt sur NovaClinic !";
        
        // n3aytou lel fonction mt3 mailing
        entities.Mailing.sendEmail(email, sujet, message);

        String smsContent =
                "NovaClinic - Nouveau compte créé\n"
                        + "CIN: " + cin + "\n"
                        + "Nom: " + nom + " " + prenom + "\n"
                        + "Email: " + email + "\n"
                        + "Tel: " + tel + "\n"
                        + "Role: " + roleLabel;

        SmsSender.sendSms(ADMIN_PHONE, smsContent);

        alert("Inscription réussie");
        redirect("/authentification.fxml");
    }

    @FXML
    private void redirectToAuthPage() {
        redirect("/authentification.fxml");
    }

    private void redirect(String fxml) {
        try {
            Stage stage = (Stage) btn_inscri.getScene().getWindow();
            stage.setScene(new Scene(
                    FXMLLoader.load(getClass().getResource(fxml))
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ THIS WAS MISSING
    private void togglePassword() {
        if (show.isSelected()) {
            tfshowpassword.setText(tfmdp.getText());
            tfshowpassword.setVisible(true);
            tfmdp.setVisible(false);
        } else {
            tfmdp.setText(tfshowpassword.getText());
            tfmdp.setVisible(true);
            tfshowpassword.setVisible(false);
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).show();
    }
}