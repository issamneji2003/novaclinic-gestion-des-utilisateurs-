package services;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Random;

public class CaptchaService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 5;
    private static String currentCode;

    public static String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        }
        currentCode = sb.toString();
        return currentCode;
    }

    public static Canvas generateImage() {
        Canvas canvas = new Canvas(200, 60);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Random r = new Random();

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, 200, 60);

        // Noise
        for (int i = 0; i < 20; i++) {
            gc.setStroke(Color.GRAY);
            gc.strokeLine(
                    r.nextInt(200), r.nextInt(60),
                    r.nextInt(200), r.nextInt(60)
            );
        }

        gc.setFont(Font.font("Arial", 28));
        gc.setFill(Color.DARKBLUE);

        for (int i = 0; i < currentCode.length(); i++) {
            gc.fillText(
                    String.valueOf(currentCode.charAt(i)),
                    20 + i * 30,
                    40 + r.nextInt(5)
            );
        }

        return canvas;
    }

    public static boolean verify(String input) {
        return input != null && input.equalsIgnoreCase(currentCode);
    }
}