package controlador;


import modelo.MovimientoContable;
import util.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovimientoContableDAO {

    public void insertarMovimiento(MovimientoContable movimiento) {
        String sql = "INSERT INTO transaccion (cuenta_id, descripcion, tipo, monto, fecha) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movimiento.getCuentaId());
            stmt.setString(2, movimiento.getDescripcion());
            stmt.setString(3, movimiento.getTipo()); // "DEBE" o "HABER"
            stmt.setDouble(4, movimiento.getMonto());
            stmt.setDate(5, new Date(movimiento.getFecha().getTime())); // java.sql.Date desde java.util.Date

            stmt.executeUpdate();

            System.out.println("✅ Movimiento contable insertado correctamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar movimiento contable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

