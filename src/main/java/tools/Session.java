package tools;

import entities.Utilisateur;

public class Session {

    private static Utilisateur currentUser;

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
