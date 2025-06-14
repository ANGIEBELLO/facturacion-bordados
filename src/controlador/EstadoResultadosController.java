package controlador;

import modelo.MovimientoContable;
import util.ConexionBD;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class EstadoResultadosController {
    private Connection conexion;

    public EstadoResultadosController() {
        try {
            conexion = ConexionBD.obtenerConexion();
        } catch (SQLException e) {
            e.printStackTrace();
            // Puedes mostrar un mensaje o lanzar una RuntimeException si deseas detener la ejecución
            throw new RuntimeException("Error al obtener la conexión a la base de datos", e);
        }
    }


    // Método para obtener todos los movimientos entre dos fechas, sin filtrar por cuenta
    public List<MovimientoContable> obtenerMovimientosPorFecha(Date desde, Date hasta) {
        List<MovimientoContable> lista = new ArrayList<>();

        String sql = "SELECT t.fecha, t.descripcion, t.tipo, t.monto, c.nombre AS cuenta, c.tipo AS tipo_cuenta " +
                "FROM transaccion t JOIN cuenta c ON t.cuenta_id = c.id " +
                "WHERE t.fecha BETWEEN ? AND ?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(desde.getTime()));
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MovimientoContable mov = new MovimientoContable();
                mov.setFecha(rs.getDate("fecha"));
                mov.setDescripcion(rs.getString("descripcion"));
                mov.setTipo(rs.getString("tipo"));
                mov.setMonto(rs.getDouble("monto"));
                mov.setCuentaNombre(rs.getString("cuenta"));
                mov.setTipoCuenta(rs.getString("tipo_cuenta")); // nuevo campo

                lista.add(mov);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // Método para calcular los totales de ingresos, gastos y resultado neto
    public Map<String, Double> calcularResumenFinanciero(List<MovimientoContable> movimientos) {
        double totalIngresos = 0;
        double totalGastos = 0;

        for (MovimientoContable mov : movimientos) {
            String tipoCuenta = mov.getTipoCuenta();
            String tipoMovimiento = mov.getTipo().toUpperCase();
            double monto = mov.getMonto();

            if ("INGRESO".equalsIgnoreCase(tipoCuenta) && "HABER".equals(tipoMovimiento)) {
                totalIngresos += monto;
            } else if ("GASTO".equalsIgnoreCase(tipoCuenta) && "DEBE".equals(tipoMovimiento)) {
                totalGastos += monto;
            }
        }

        double resultadoNeto = totalIngresos - totalGastos;

        Map<String, Double> resumen = new HashMap<>();
        resumen.put("ingresos", totalIngresos);
        resumen.put("gastos", totalGastos);
        resumen.put("resultadoNeto", resultadoNeto);

        return resumen;
    }

    public Map<String, Map<String, Double>> obtenerResumenPorCuenta(Date desde, Date hasta) throws SQLException {
        Map<String, Map<String, Double>> resumen = new HashMap<>();

        String sql = """
            SELECT c.tipo, c.nombre AS cuenta, t.tipo AS tipo_mov, SUM(t.monto) AS total
            FROM transaccion t
            JOIN cuenta c ON t.cuenta_id = c.id
            WHERE t.fecha BETWEEN ? AND ?
              AND ((c.tipo = 'INGRESO' AND t.tipo = 'HABER') OR (c.tipo = 'GASTO' AND t.tipo = 'DEBE'))
            GROUP BY c.tipo, c.nombre, t.tipo
            ORDER BY c.tipo, c.nombre
            """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(desde.getTime()));
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tipoCuenta = rs.getString("tipo");
                String nombreCuenta = rs.getString("cuenta");
                double total = rs.getDouble("total");

                resumen
                        .computeIfAbsent(tipoCuenta, k -> new LinkedHashMap<>())
                        .put(nombreCuenta, total);
            }
        }

        return resumen;
    }

}
