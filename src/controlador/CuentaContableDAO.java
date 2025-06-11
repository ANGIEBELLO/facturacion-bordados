package controlador;


import modelo.CuentaContable;
import util.ConexionBD;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaContableDAO {

    public boolean agregarCuenta(CuentaContable cuenta) {
        String sql = "INSERT INTO cuenta (codigo, nombre, tipo) VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cuenta.getCodigo());
            stmt.setString(2, cuenta.getNombre());
            stmt.setString(3, cuenta.getTipo());

            int filas = stmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CuentaContable> listarCuentas() {
        List<CuentaContable> lista = new ArrayList<>();
        String sql = "SELECT * FROM cuenta ORDER BY codigo";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CuentaContable cuenta = new CuentaContable(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("tipo")
                );
                lista.add(cuenta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    public boolean eliminarCuenta(String codigo) {
        String sql = "DELETE FROM cuenta WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
