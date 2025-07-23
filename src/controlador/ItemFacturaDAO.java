package controlador;


import modelo.ItemFactura;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemFacturaDAO {

    public boolean insertarItemFactura(ItemFactura item, int idFactura, int idEmpleado) {
        String sql = "INSERT INTO items_factura (id_factura, producto, tipo, cantidad, valor_unitario, valor_total, empleado, pagado, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            stmt.setString(2, item.getProducto());
            stmt.setString(3, item.getTipo());
            stmt.setInt(4, item.getCantidad());
            stmt.setDouble(5, item.getValorUnitario());
            stmt.setDouble(6, item.getSubtotal());
            stmt.setInt(7, item.getIdEmpleado());
            stmt.setInt(8, idEmpleado);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ItemFactura> listarPorFactura(int idFactura) {
        List<ItemFactura> lista = new ArrayList<>();
        String sql = "SELECT * FROM items_factura WHERE id_factura = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setProducto(rs.getString("producto"));
                item.setTipo(rs.getString("tipo"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setIdEmpleado(rs.getInt("id_empleado"));  // ✔️ Correcto

                lista.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public List<ItemFactura> listarPorEmpleadoNoPagados(int idEmpleado) {
        List<ItemFactura> lista = new ArrayList<>();
        String sql = "SELECT * FROM items_factura WHERE id_empleado = ? AND pagado = 0";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setProducto(rs.getString("producto"));
                item.setTipo(rs.getString("tipo"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setIdEmpleado(rs.getInt("id_empleado"));

                lista.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public List<ItemFactura> obtenerItemsNoPagadosPorEmpleado(int idEmpleado) {
        List<ItemFactura> lista = new ArrayList<>();
        String sql = "SELECT i.*, f.fecha FROM items_factura i " +
                "JOIN facturas f ON i.id_factura = f.id_factura " +
                "WHERE i.id_empleado = ? AND i.pagado = 0";


        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setIdFactura(rs.getInt("id_factura"));
                item.setIdEmpleado(rs.getInt("id_empleado"));
                item.setTipo(rs.getString("tipo"));
                item.setDescripcion(rs.getString("producto"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setNombre(rs.getString("producto"));
                item.setFecha(rs.getDate("fecha"));


                java.sql.Date sqlDate = rs.getDate("fecha");
                if (sqlDate != null) {
                    item.setFecha(new java.util.Date(sqlDate.getTime()));
                }

                lista.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    public boolean marcarItemComoPagado(int idItem) {
        String sql = "UPDATE items_factura SET pagado = 1 WHERE id_item = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idItem);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ItemFactura> obtenerItemsSinEmpleado() {
        List<ItemFactura> items = new ArrayList<>();

        String sql = "SELECT * FROM items_factura WHERE id_empleado IS NULL OR id_empleado = 0";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setIdFactura(rs.getInt("id_factura"));
                item.setTipo(rs.getString("tipo"));
                item.setDescripcion(rs.getString("producto"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setNombre(rs.getString("producto"));
                item.setIdEmpleado(0); // No asignado aún
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public boolean asignarEmpleadoAItem(int idItem, int idEmpleado) {
        String sql = "UPDATE items_factura SET id_empleado = ? WHERE id_item = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setInt(2, idItem);

            int filasActualizadas = stmt.executeUpdate();
            return filasActualizadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ItemFactura obtenerPorId(int idItem) {
        String sql = "SELECT * FROM items_factura WHERE id_item = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idItem);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setIdFactura(rs.getInt("id_factura"));
                item.setIdEmpleado(rs.getInt("id_empleado"));
                item.setTipo(rs.getString("tipo"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setNombre(rs.getString("producto"));
                item.setPagado(rs.getBoolean("pagado"));
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ItemFactura> obtenerItemsPagadosPorEmpleado(int idEmpleado) {
        List<ItemFactura> lista = new ArrayList<>();

        String sql = "SELECT * FROM items_factura WHERE id_empleado = ? AND pagado = 1 ORDER BY fecha_pago DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setIdFactura(rs.getInt("id_factura"));
                item.setNombre(rs.getString("producto"));
                item.setTipo(rs.getString("tipo"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setSubtotal(rs.getDouble("subtotal"));
                item.setFechaPago(rs.getDate("fecha_pago")); // 👈 solo si existe ese campo

                lista.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }








}


