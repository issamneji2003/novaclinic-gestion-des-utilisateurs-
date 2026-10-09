package services;

public class LanguageService {

    private static String currentLang = "fr"; // default

    public static void setLang(String lang) {
        currentLang = lang;
    }

    public static String getLang() {
        return currentLang;
    }
}