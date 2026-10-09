package services;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class FaceService {

    // ✅ تحميل OpenCV (clean مع Java >= 12)
    static {
        OpenCV.loadLocally();
    }

    private CascadeClassifier faceDetector;

    public FaceService() {
        try {
            String projectPath = System.getProperty("user.dir");

            String xmlPath = projectPath + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "haarcascade_frontalface_default.xml";

            faceDetector = new CascadeClassifier(xmlPath);

            if (faceDetector.empty()) {
                xmlPath = projectPath + File.separator + "target"
                        + File.separator + "classes"
                        + File.separator + "haarcascade_frontalface_default.xml";
                faceDetector = new CascadeClassifier(xmlPath);
            }

            if (faceDetector.empty()) {
                System.out.println("❌ ERROR: Haarcascade introuvable");
            } else {
                System.out.println("✅ Face Detector prêt !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📸 Inscription : capture + save
    public boolean detectAndSave(int userId) {
        VideoCapture capture = new VideoCapture(0);
        Mat frame = new Mat();

        if (!capture.isOpened()) {
            return false;
        }

        capture.read(frame);

        if (frame.empty()) {
            capture.release();
            return false;
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces);

        if (faces.empty()) {
            capture.release();
            return false;
        }

        String relativePath = "faces/admin_" + userId + ".jpg";
        String fullPath = "src/main/resources/" + relativePath;

// save image physically
        Imgcodecs.imwrite(fullPath, frame);

// ❗ خزّن relativePath فقط في DB
// exemple: utilisateur.setFaceImage(relativePath);

        capture.release();

        System.out.println("✅ Visage enregistré : " + relativePath);
        return true;
    }

    // 🔍 Vérification : face vs face
    public boolean compareFaces(String savedImagePath, Mat currentFrame) {

        // 0️⃣ Load image
        Mat savedImage = Imgcodecs.imread(savedImagePath);
        if (savedImage.empty() || currentFrame.empty()) return false;

        // 1️⃣ Grayscale
        Mat graySaved = new Mat();
        Mat grayCurrent = new Mat();
        Imgproc.cvtColor(savedImage, graySaved, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(currentFrame, grayCurrent, Imgproc.COLOR_BGR2GRAY);

        // 2️⃣ Detect faces (MANDATORY)
        MatOfRect facesSaved = new MatOfRect();
        MatOfRect facesLive = new MatOfRect();
        faceDetector.detectMultiScale(graySaved, facesSaved);
        faceDetector.detectMultiScale(grayCurrent, facesLive);

        // ❌ لازم وجه واحد بالضبط
        if (facesSaved.toArray().length != 1 || facesLive.toArray().length != 1) {
            System.out.println("❌ Invalid face count");
            return false;
        }

        // 3️⃣ Crop face only
        Rect rSaved = facesSaved.toArray()[0];
        Rect rLive = facesLive.toArray()[0];

        Mat faceSaved = new Mat(graySaved, rSaved);
        Mat faceLive = new Mat(grayCurrent, rLive);

        Imgproc.resize(faceLive, faceLive, faceSaved.size());

        // 4️⃣ Blur خفيف
        Imgproc.GaussianBlur(faceSaved, faceSaved, new Size(5,5), 0);
        Imgproc.GaussianBlur(faceLive, faceLive, new Size(5,5), 0);

        // 5️⃣ STRUCTURE (edges)
        Mat e1 = new Mat(), e2 = new Mat();
        Imgproc.Canny(faceSaved, e1, 100, 200);
        Imgproc.Canny(faceLive, e2, 100, 200);

        Mat diff = new Mat();
        Core.absdiff(e1, e2, diff);
        double structureScore = Core.mean(diff).val[0];

        System.out.println("STRUCTURE = " + structureScore);

        // ✅ شرط صارم
        return structureScore <4;
    }}