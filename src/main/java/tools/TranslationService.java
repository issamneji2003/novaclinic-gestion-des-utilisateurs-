package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static String translate(String text, String targetLang) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            String url =
                    "https://translate.googleapis.com/translate_a/single"
                            + "?client=gtx"
                            + "&sl=auto"
                            + "&tl=" + targetLang
                            + "&dt=t"
                            + "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body().split("\"")[1];

        } catch (Exception e) {
            return text;
        }
    }
}