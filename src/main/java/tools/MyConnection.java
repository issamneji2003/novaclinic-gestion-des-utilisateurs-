package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    public String url = "jdbc:mysql://localhost:3306/users";
    public String login = "root";
    public String password = "";

    public int id;
    public String email;

    Connection cnx;
    public static MyConnection instance;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Connection getCnx() { return cnx; }

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, password);
            System.out.println("Connexion établie!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
