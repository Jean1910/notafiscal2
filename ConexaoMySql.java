package Pck_DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySql {
    private Connection oConn;

    private static final String URL  =
        "jdbc:mysql://localhost:3306/BD_MVC_DAO?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root"; // coloque a senha real

    public Connection getConexao() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            oConn = DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oConn;
    }

    public void desconectar() {
        try {
            if (oConn != null && !oConn.isClosed()) oConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
