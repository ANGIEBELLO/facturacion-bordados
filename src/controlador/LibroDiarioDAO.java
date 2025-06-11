package controlador;

import modelo.MovimientoContable;
import modelo.ParametroContable;
import modelo.TransaccionContable;
import util.ConexionBD;

import java.sql.*;
import java.util.List;

public class LibroDiarioDAO {

    public boolean registrarTransaccion(TransaccionContable transaccion) {
        String insertLibro = "INSERT INTO libro_diario (fecha, descripcion) VALUES (?, ?)";
        String insertMovimiento = "INSERT INTO movimiento (libro_id, cuenta_codigo, cuenta_nombre, debe, haber) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtLibro = conn.prepareStatement(insertLibro, Statement.RETURN_GENERATED_KEYS)) {
                stmtLibro.setDate(1, new java.sql.Date(transaccion.getFecha().getTime()));
                stmtLibro.setString(2, transaccion.getDescripcion());
                stmtLibro.executeUpdate();

                ResultSet rs = stmtLibro.getGeneratedKeys();
                if (rs.next()) {
                    int libroId = rs.getInt(1);

                    for (MovimientoContable mov : transaccion.getMovimientos()) {
                        try (PreparedStatement stmtMov = conn.prepareStatement(insertMovimiento)) {
                            stmtMov.setInt(1, libroId);
                            stmtMov.setString(2, mov.getCuentaCodigo());
                            stmtMov.setString(3, mov.getCuentaNombre());
                            stmtMov.setDouble(4, mov.getDebe());
                            stmtMov.setDouble(5, mov.getHaber());
                            stmtMov.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public ParametroContable obtenerCuentasPorEvento(String evento) {
        String sql = "SELECT cuenta_debe_id, cuenta_haber_id FROM parametros_contables WHERE evento = ?";
        ParametroContable parametro = null;

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evento);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                parametro = new ParametroContable();
                parametro.setCuentaDebeId(rs.getInt("cuenta_debe_id"));
                parametro.setCuentaHaberId(rs.getInt("cuenta_haber_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return parametro;
    }

}
