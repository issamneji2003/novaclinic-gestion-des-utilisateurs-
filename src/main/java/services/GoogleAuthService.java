package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private static final String APP_NAME = "NovaClinic";

    public static String signInWithGoogle() {
        try {
            // 1. Transport HTTP sécurisé
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // 2. Charger client_secrets.json depuis resources
            InputStream in = GoogleAuthService.class.getResourceAsStream("/client_secrets.json");
            if (in == null) {
                System.err.println("client_secrets.json introuvable dans resources/");
                return null;
            }

            // 3. Charger les secrets Google
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    jsonFactory,
                    new InputStreamReader(in)
            );

            // 4. Construire le flow OAuth2
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT,
                    jsonFactory,
                    clientSecrets,
                    SCOPES
            )
                    .setAccessType("online")
                    .build();

            // 5. Serveur local port 8888 pour recevoir le callback Google
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            // 6. Ouvrir le navigateur et attendre la réponse
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");

            // 7. Récupérer les infos du compte Google
            Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, jsonFactory, credential)
                    .setApplicationName(APP_NAME)
                    .build();

            Userinfo userInfo = oauth2.userinfo().get().execute();

            System.out.println("Google Sign-In reussi : " + userInfo.getEmail());
            return userInfo.getEmail();

        } catch (Exception e) {
            System.err.println("Erreur Google Sign-In : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}