package controlador;

import modelo.ParametroContable;
import util.ConexionBD;

import java.sql.*;

public class ParametroContableDAO {

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
