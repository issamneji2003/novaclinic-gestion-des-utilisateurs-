package test;

import entities.Utilisateur;
import services.UtilisateurCrud;
import tools.MyConnection;

public class MainClass {

    public static void main(String[] args) {

        // Test Singleton
        MyConnection mc1 = MyConnection.getInstance();
        MyConnection mc2 = MyConnection.getInstance();
        System.out.println(mc1.hashCode() + " - " + mc2.hashCode());

        // Test ajout utilisateur
        Utilisateur u = new Utilisateur();

        u.setCin(1505878);
        u.setTel("5297719");
        u.setNom("talbi");
        u.setPrenom("isaam");
        u.setEmail("ISSAM@esprit.tn");
        u.setPassword("123456");
        u.setRoles("[\"ROLE_PATIENT\"]");

        UtilisateurCrud crud = new UtilisateurCrud();


    }
}
