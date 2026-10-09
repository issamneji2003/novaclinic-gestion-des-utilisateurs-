package controllers;

import entities.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import services.FaceService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import javafx.scene.image.Image;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;







public class FaceAuthController {

    // IDs m'sync-i m3a el FXML mte3ek
    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private FaceService faceService = new FaceService();
    private Utilisateur currentUser;
    private boolean verified = false;








    // 1. Initialisation des données
    public void initData(Utilisateur u) {
        this.currentUser = u;
        if (u != null) {
            System.out.println("👤 Admin en attente: " + u.getNom());
            System.out.println("🖼️ Path image: " + u.getFaceImage());
        }
        startCamera();
    }

    // 2. Getter pour l'AuthentificationController
    public boolean isVerified() {
        return verified;
    }

    // 3. Lancement de la Caméra
    private void startCamera() {
        this.capture = new VideoCapture(0);
        if (this.capture.isOpened()) {
            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(() -> {
                Mat frame = new Mat();
                if (this.capture.read(frame)) {
                    Image imageToShow = mat2Image(frame);
                    Platform.runLater(() -> {
                        if (cameraView != null) {
                            cameraView.setImage(imageToShow);
                        }
                    });
                }
            }, 0, 33, TimeUnit.MILLISECONDS);
        } else {
            Platform.runLater(() -> statusLabel.setText("❌ Erreur: Caméra introuvable"));
        }
    }

    // 4. Vérification du visage
    @FXML
    private void verifyFace() {

        if (currentUser == null || currentUser.getFaceImage() == null) {
            statusLabel.setText("❌ Erreur: Pas d'image de référence");
            return;
        }

        // ✅ FIX FINAL : read from disk (dev-safe)
        String projectPath = System.getProperty("user.dir");
        String facePath = currentUser.getFaceImage();
        
        // nna7iw et'tekrar mte3 src/main/resources kan mawjouda fel base
        if (facePath.startsWith("src/main/resources/")) {
            facePath = facePath.replace("src/main/resources/", "");
        }

        File imageFile = new File(projectPath + "/src/main/resources/" + facePath);
        System.out.println("DISK TEST = " + imageFile.getAbsolutePath());

        if (!imageFile.exists()) {
            statusLabel.setText("❌ Image introuvable (disk)");
            return;
        }

        Mat currentFrame = new Mat();
        if (this.capture.read(currentFrame)) {

            if (faceService.compareFaces(imageFile.getAbsolutePath(), currentFrame)) {
                verified = true;
                statusLabel.setText("✅ Accès Autorisé !");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

                new Thread(() -> {
                    try { Thread.sleep(1200); } catch (Exception ignored) {}
                    Platform.runLater(this::closePopup);
                }).start();

            } else {
                statusLabel.setText("❌ Visage non reconnu");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    // 5. Fermeture du popup et arrêt caméra
    @FXML
    private void closePopup() {
        stopCamera();
        if (cameraView != null && cameraView.getScene() != null) {
            cameraView.getScene().getWindow().hide();
        }
    }

    private void stopCamera() {
        if (this.timer != null && !this.timer.isShutdown()) {
            this.timer.shutdown();
        }
        if (this.capture != null && this.capture.isOpened()) {
            this.capture.release();
        }
    }

    // 6. Conversion OpenCV -> JavaFX
    private Image mat2Image(Mat frame) {
        Mat rgb = new Mat();
        Imgproc.cvtColor(frame, rgb, Imgproc.COLOR_BGR2RGB);

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", rgb, buffer);

        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}