package util;

import java.sql.*;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/bordados_db";
    private static final String USER = "root";
    private static final String PASSWORD = "0000"; // cámbiala si es distinta

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método para cerrar Connection, PreparedStatement y ResultSet
    public static void cerrar(Connection con, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) { e.printStackTrace(); }

        try {
            if (ps != null) ps.close();
        } catch (SQLException e) { e.printStackTrace(); }

        try {
            if (con != null) con.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Sobrecarga para cerrar solo Connection y PreparedStatement
    public static void cerrar(Connection con, PreparedStatement ps) {
        cerrar(con, ps, null);
    }

    // Sobrecarga para cerrar solo PreparedStatement
    public static void cerrar(PreparedStatement ps) {
        cerrar(null, ps, null);
    }

}
