package services;

import entities.MdpHash;
import entities.Utilisateur;
import tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurCrud implements IUtilisateurCrud {

    private final Connection cnx;

    public UtilisateurCrud() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ==============================
    // AJOUTER UTILISATEUR
    // ==============================
    @Override
    public void ajouter(Utilisateur u) {
        // ✅ ZIDNA L'IDENTIFIANT HOUWN
        String sql = "INSERT INTO user (cin, tel, nom, prenom, email, password, roles, identifiant) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, u.getCin());
            pst.setString(2, u.getTel());
            pst.setString(3, u.getNom());
            pst.setString(4, u.getPrenom());
            pst.setString(5, u.getEmail());

            // HASH PASSWORD
            pst.setString(6, MdpHash.hashPassword(u.getPassword()));

            // DEFAULT ROLE
            pst.setString(7, u.getRoles() != null ? u.getRoles() : "[\"ROLE_PATIENT\"]");
            
            // L'IDENTIFIANT
            pst.setString(8, u.getIdentifiant());

            pst.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec identifiant : " + u.getIdentifiant());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // ==============================
    // AFFICHER TOUS
    // ==============================
    @Override
    public List<Utilisateur> afficher() {

        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (PreparedStatement pst = cnx.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(mapUtilisateur(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ==============================
    // MODIFIER UTILISATEUR
    // ==============================
    @Override
    public void modifier(Utilisateur u) {

        String sql = """
        UPDATE user
        SET cin = ?,
            tel = ?,
            nom = ?,
            prenom = ?,
            email = ?,
            roles = ?
        WHERE id = ?
    """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, u.getCin());
            pst.setString(2, u.getTel());
            pst.setString(3, u.getNom());
            pst.setString(4, u.getPrenom());
            pst.setString(5, u.getEmail());
            pst.setString(6, u.getRoles());
            pst.setInt(7, u.getId());

            pst.executeUpdate();
            System.out.println("✅ Utilisateur modifié (UPDATE)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //==============================LPatient +++++++++++++++++++++++++++++
    @Override
    public void modifierI(Utilisateur u) {

        String sql = """
        UPDATE user 
        SET cin=?,
            tel=?,
            nom=?,
            prenom=?,
            email=?,
            roles=?,
            yearsExperience=?,
            highestProfit=?,
            budget=?
        WHERE id=?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, u.getCin());
            pst.setString(2, u.getTel());
            pst.setString(3, u.getNom());
            pst.setString(4, u.getPrenom());
            pst.setString(5, u.getEmail());
            pst.setString(6, u.getRoles());

            pst.setString(7, u.getYearsExperience());
            pst.setString(8, u.getHighestProfit());
            pst.setString(9, u.getBudget());

            pst.setInt(10, u.getId());

            pst.executeUpdate();
            System.out.println("Utilisateur modifié (Patient)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // CHANGER MOT DE PASSE
    // ==============================
    public void changerMotDePasse(int id, String newPassword) {

        String sql = "UPDATE user SET password=? WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, MdpHash.hashPassword(newPassword));
            pst.setInt(2, id);
            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // SUPPRIMER UTILISATEUR
    // ==============================
    @Override
    public void supprimer(int id) {

        String sql = "DELETE FROM user WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Utilisateur supprimé");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // LOGIN SECURISE
    // ==============================
    public Utilisateur login(String email, String password) {
        // ✅ BADELNA REQUETE BECH TA9BIL EMAIL WELA IDENTIFIANT
        String sql = "SELECT * FROM user WHERE email = ? OR identifiant = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, email.trim());
            pst.setString(2, email.trim()); // N7ottouh martin khater tnajem tkoun identifiant

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String storedHash = rs.getString("password");
                String inputHash = MdpHash.hashPassword(password);

                if (storedHash.equals(inputHash)) {
                    return mapUtilisateur(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    // ==============================
    // FIND BY EMAIL
    // ==============================
    public Utilisateur findByEmail(String email) {

        String sql = "SELECT * FROM user WHERE email=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapUtilisateur(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ==============================
    // VERIFIER EXISTENCE
    // ==============================
    public boolean utilisateurExiste(int cin, String email) {

        String sql = "SELECT COUNT(*) FROM user WHERE cin=? OR email=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, cin);
            pst.setString(2, email);

            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==============================
    // MAPPER UTILISATEUR
    // ==============================
    private Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();

        u.setId(rs.getInt("id"));
        u.setCin(rs.getInt("cin"));
        u.setTel(rs.getString("tel"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRoles(rs.getString("roles"));

        u.setIdentifiant(rs.getString("identifiant"));

        u.setFaceImage(rs.getString("face_image"));

        u.setYearsExperience(rs.getString("yearsExperience"));
        u.setHighestProfit(rs.getString("highestProfit"));
        u.setBudget(rs.getString("budget"));
        u.setActive(rs.getInt("active"));

        return u;
    }

    public void resetPasswordByEmail(String email, String hashedPassword) {
        String sql = "UPDATE user SET password=? WHERE email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, hashedPassword);
            pst.setString(2, email);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean updatePasswordByEmail(String email, String hashedPassword) {

        String sql = "UPDATE user SET password=? WHERE email=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, hashedPassword);
            pst.setString(2, email);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // ==============================
// 🔒 BLOCK USER
// ==============================
    public void blockUser(int id) {

        String sql = "UPDATE user SET active = 0 WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("⛔ Utilisateur bloqué");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // ==============================
// 🔓 UNBLOCK USER
// ==============================
    public void unblockUser(int id) {

        String sql = "UPDATE user SET active = 1 WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Utilisateur débloqué");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateFaceImage(int id, String imagePath) {
        String sql = "UPDATE user SET face_image = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, imagePath);
            pst.setInt(2, id);
            pst.executeUpdate();
            System.out.println("✅ Image faciale enregistrée dans la base");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
