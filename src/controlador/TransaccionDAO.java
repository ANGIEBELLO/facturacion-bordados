package controlador;

import util.ConexionBD;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransaccionDAO {

public double obtenerTotalPorTipoCuenta(String tipoCuenta) {
    double total = 0;
    String sql = """
        SELECT SUM(t.monto)
        FROM transaccion t
        JOIN cuenta_contable c ON t.id = c.id
        WHERE c.tipo = ?
    """;
    try (Connection con = ConexionBD.obtenerConexion();
         PreparedStatement stmt = con.prepareStatement(sql)) {
        stmt.setString(1, tipoCuenta);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            total = rs.getDouble(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return total;
}
    public void registrarTransaccion(int idCuenta, double monto, String tipo, java.sql.Date fecha, String descripcion) {
        String sql = "INSERT INTO transaccion (cuenta_id, monto, tipo, fecha, descripcion) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCuenta);
            stmt.setDouble(2, monto);
            stmt.setString(3, tipo); // "DEBE" o "HABER"
            stmt.setDate(4, fecha);
            stmt.setString(5, descripcion);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al registrar transacción contable: " + e.getMessage());
        }

    }
    public boolean transaccionExiste(String descripcion) {
        String sql = "SELECT COUNT(*) FROM transaccion WHERE descripcion = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}



