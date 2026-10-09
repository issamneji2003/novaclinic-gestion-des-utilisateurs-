package services;

import entities.Utilisateur;
import java.util.List;

public interface IUtilisateurCrud {

    void ajouter(Utilisateur u);

    List<Utilisateur> afficher();

    void modifier(Utilisateur u);

    void modifierI(Utilisateur u);

    void supprimer(int id);

    // 🔑 ADMIN ACTIONS
    void changerMotDePasse(int id, String newPassword);

    void blockUser(int id);

    void unblockUser(int id);
}