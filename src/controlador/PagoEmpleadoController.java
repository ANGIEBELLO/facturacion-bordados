package controlador;

import util.ConexionBD;
import modelo.ItemFactura;
import util.ConexionBD;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoEmpleadoController {


    public List<ItemFactura> obtenerItemsImpagosPorEmpleado(int idEmpleado) {
        List<ItemFactura> lista = new ArrayList<>();
        String sql = "SELECT * FROM items_factura WHERE id_empleado = ? AND pagado = 0";

        try (Connection conn = util.ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setIdFactura(rs.getInt("id_factura"));
                item.setIdEmpleado(rs.getInt("id_empleado"));
                item.setTipo(rs.getString("tipo"));
                item.setDescripcion(rs.getString("descripcion"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setNombre(rs.getString("producto"));
                item.setPagado(rs.getBoolean("pagado"));
                lista.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public boolean marcarItemsComoPagados(List<ItemFactura> items) {
        System.out.println("🟢 Ejecutando método marcarItemsComoPagados");

        String sql = "UPDATE items_factura SET pagado = 1, fecha_pago = CURRENT_DATE WHERE id_item = ?";

        Connection conn = null;

        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // ⚠️ Inicia la transacción

            // 1. Marcar ítems como pagados
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (ItemFactura item : items) {
                    stmt.setInt(1, item.getIdItem());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 2. Calcular el total a pagar
            double totalPago = 0;
            for (ItemFactura item : items) {
                double porcentaje = item.getTipo().equalsIgnoreCase("Bordado") ? 0.15 : 0.30;
                totalPago += item.getSubtotal() * porcentaje;
            }

            // 3. Obtener nombre del empleado (usando el primer ítem)
            int idEmpleado = items.get(0).getIdEmpleado();
            String nombreEmpleado = obtenerNombreEmpleadoPorId(idEmpleado); // 👉 método adicional más abajo

            // 4. Registrar en la tabla transaccion (una sola vez)
            String descripcion = "Pago a empleado: " + nombreEmpleado;
            boolean transaccionOK = registrarTransaccionPago(conn, totalPago, 6, descripcion);

            if (!transaccionOK) {
                System.out.println("❌ Error al registrar la transacción.");
                conn.rollback();
                return false;
            }

            conn.commit(); // ✅ Confirmar todo
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String obtenerNombreEmpleadoPorId(int idEmpleado) {
        String sql = "SELECT nombre FROM empleados WHERE id_empleado = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }

    public boolean registrarTransaccionPago(Connection conn, double monto, int cuentaEmpleadoId, String descripcion) {
        String sql = "INSERT INTO transaccion (fecha, descripcion, cuenta_id, tipo, monto) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            java.sql.Date fechaActual = new java.sql.Date(System.currentTimeMillis());

            // ➤ 1. Registrar en cuenta de empleado (DEBE)
            stmt.setDate(1, fechaActual);
            stmt.setString(2, descripcion);
            stmt.setInt(3, cuentaEmpleadoId); // cuenta de gasto por pagos de salario (ej. 6)
            stmt.setString(4, "DEBE");
            stmt.setDouble(5, monto);
            stmt.executeUpdate();

            // ➤ 2. Registrar contrapartida en cuenta de caja (HABER)
            stmt.setDate(1, fechaActual);
            stmt.setString(2, "Salida de efectivo por pago a empleado");
            stmt.setInt(3, 2); // cuenta de caja o efectivo (ej. 2)
            stmt.setString(4, "HABER");
            stmt.setDouble(5, monto);
            stmt.executeUpdate();

            System.out.println("✅ Doble transacción registrada correctamente.");
            return true;

        } catch (SQLException e) {
            System.err.println("🔴 Error al registrar transacción doble: " + e.getMessage());
            return false;
        }
    }






}








