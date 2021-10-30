package Database;

import java.sql.*;

public class DB {
    Connection conn;

    public DB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/MVD?autoReconnect=true&useSSL=false&&allowPublicKeyRetrieval=true",
                "root", "P4password!");
        this.conn = con;
    }

    public Connection getCon() {
        return conn;
    }

}