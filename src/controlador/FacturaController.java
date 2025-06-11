package controlador;

import modelo.*;
import util.ConexionBD;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.swing.JOptionPane;

public class FacturaController {

    public int guardarOActualizarFacturaConItems(Factura factura) {
        factura.calcularTotales();

        if (factura.getAbono() >= factura.getTotal()) {
            factura.setEstado("Cancelada");
        } else {
            factura.setEstado("Pendiente");
        }

        String sqlInsertFactura = "INSERT INTO facturas (fecha, total_factura, abono_realizado, saldo_pendiente, estado_pago, estado_trabajo, id_cliente) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdateFactura = "UPDATE facturas SET fecha = ?, total_factura = ?, abono_realizado = ?, saldo_pendiente = ?, estado_pago = ?, estado_trabajo = ? WHERE id_factura = ?";
        String sqlDeleteItems = "DELETE FROM items_factura WHERE id_factura = ?";
        String sqlInsertItem = "INSERT INTO items_factura (id_factura, tipo, producto, cantidad, valor_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        int idFactura = factura.getId();

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                if (idFactura > 0) {
                    // Actualizar factura existente
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateFactura)) {
                        stmtUpdate.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
                        stmtUpdate.setDouble(2, factura.getTotal());
                        stmtUpdate.setDouble(3, factura.getAbono());
                        stmtUpdate.setDouble(4, factura.getSaldo());
                        stmtUpdate.setString(5, factura.getEstado());
                        stmtUpdate.setString(6, factura.getEstadoTrabajo());
                        stmtUpdate.setInt(7, idFactura);
                        stmtUpdate.executeUpdate();
                    }

                    // Borrar ítems antiguos
                    try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteItems)) {
                        stmtDelete.setInt(1, idFactura);
                        stmtDelete.executeUpdate();
                    }

                } else {
                    // Insertar nueva factura
                    try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertFactura, Statement.RETURN_GENERATED_KEYS)) {
                        stmtInsert.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
                        stmtInsert.setDouble(2, factura.getTotal());
                        stmtInsert.setDouble(3, factura.getAbono());
                        stmtInsert.setDouble(4, factura.getSaldo());
                        stmtInsert.setString(5, factura.getEstado());
                        stmtInsert.setString(6, factura.getEstadoTrabajo());
                        stmtInsert.setInt(7, factura.getIdCliente());

                        stmtInsert.executeUpdate();

                        try (ResultSet rs = stmtInsert.getGeneratedKeys()) {
                            if (rs.next()) {
                                idFactura = rs.getInt(1);
                                factura.setId(idFactura);
                            } else {
                                conn.rollback();
                                JOptionPane.showMessageDialog(null, "No se pudo obtener el ID de la factura.");
                                return -1;
                            }
                        }
                    }
                }

                // Insertar ítems
                try (PreparedStatement stmtItems = conn.prepareStatement(sqlInsertItem)) {
                    for (ItemFactura item : factura.getItems()) {
                        stmtItems.setInt(1, idFactura);
                        stmtItems.setString(2, item.getTipo());
                        stmtItems.setString(3, item.getDescripcion());
                        stmtItems.setInt(4, item.getCantidad());
                        stmtItems.setDouble(5, item.getValorUnitario());
                        stmtItems.setDouble(6, item.getSubtotal());
                        stmtItems.addBatch();
                    }
                    stmtItems.executeBatch();
                }

                // Registrar transacciones contables si la factura está cancelada
                if ("Cancelada".equalsIgnoreCase(factura.getEstado())) {
                    ParametroContableDAO paramDAO = new ParametroContableDAO();
                    TransaccionDAO transDAO = new TransaccionDAO();

                    ParametroContable cuentas = paramDAO.obtenerCuentasPorEvento("FACTURA_CANCELADA");

                    if (cuentas != null) {
                        String descripcionTrans = "Factura Cancelada ID: " + factura.getId();

                        if (!transDAO.transaccionExiste(descripcionTrans)) {
                            java.sql.Date fecha = new java.sql.Date(factura.getFecha().getTime());
                            double monto = factura.getTotal();

                            transDAO.registrarTransaccion(cuentas.getCuentaDebeId(), monto, "DEBE", fecha, descripcionTrans);
                            transDAO.registrarTransaccion(cuentas.getCuentaHaberId(), monto, "HABER", fecha, descripcionTrans);

                            System.out.println(">> Transacciones contables registradas para factura ID: " + factura.getId());
                        } else {
                            System.out.println(">> Transacciones ya registradas previamente para factura ID: " + factura.getId());
                        }
                    } else {
                        System.out.println(">> No se encontraron cuentas contables para FACTURA_CANCELADA.");
                    }
                }

                conn.commit();

                if (factura.getId() > 0) {
                    JOptionPane.showMessageDialog(null, "Factura actualizada exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(null, "Factura e ítems guardados exitosamente.");
                }

                return idFactura;

            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Error en la operación de factura: " + e.getMessage());
                return -1;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en la conexión: " + e.getMessage());
            return -1;
        }
    }


    public Factura obtenerFacturaConItems(int idFactura) {
        Factura factura = null;

        String sqlFactura = "SELECT f.*, c.nombre AS nombre_cliente, c.telefono " +
                "FROM facturas f JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "WHERE f.id_factura = ?";
        String sqlItems = "SELECT * FROM items_factura WHERE id_factura = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement psFactura = conn.prepareStatement(sqlFactura);
             PreparedStatement psItems = conn.prepareStatement(sqlItems)) {

            psFactura.setInt(1, idFactura);
            ResultSet rsFactura = psFactura.executeQuery();

            if (rsFactura.next()) {
                Cliente cliente = new Cliente();
                cliente.setNombre(rsFactura.getString("nombre_cliente"));
                cliente.setTelefono(rsFactura.getString("telefono"));

                factura = new Factura(
                        rsFactura.getInt("id_factura"),
                        rsFactura.getDate("fecha"),
                        rsFactura.getDouble("total_factura"),
                        rsFactura.getDouble("abono_realizado"),
                        rsFactura.getInt("saldo_pendiente"),
                        rsFactura.getString("estado_pago"),
                        rsFactura.getString("estado_trabajo")
                );
                factura.setCliente(cliente);
                factura.setTelefonoCliente(cliente.getTelefono());
            }

            if (factura != null) {
                psItems.setInt(1, idFactura);
                ResultSet rsItems = psItems.executeQuery();
                List<ItemFactura> items = new ArrayList<>();
                while (rsItems.next()) {
                    ItemFactura item = new ItemFactura();
                    item.setTipo(rsItems.getString("tipo"));
                    item.setDescripcion(rsItems.getString("producto"));
                    item.setCantidad(rsItems.getInt("cantidad"));
                    item.setValorUnitario(rsItems.getDouble("valor_unitario"));

                    items.add(item);

                }
                factura.setItems(items);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return factura;
    }

    public List<Factura> obtenerTodasLasFacturas() {
        List<Factura> listaFacturas = new ArrayList<>();

        String sql = "SELECT f.id_factura, f.fecha, f.total_factura, f.abono_realizado, f.saldo_pendiente, f.id_cliente, f.estado_pago, f.estado_trabajo, " +
                "c.nombre AS clienteNombre, c.telefono AS clienteTelefono " +
                "FROM facturas f " +
                "JOIN clientes c ON f.id_cliente = c.id_cliente";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_factura");
                java.sql.Date fecha = rs.getDate("fecha");
                double total = rs.getDouble("total_factura");
                double abono = rs.getDouble("abono_realizado");
                double saldo = rs.getDouble("saldo_pendiente");
                int idCliente = rs.getInt("id_cliente");
                String estado = rs.getString("estado_pago");
                String estadoTrabajo = rs.getString("estado_trabajo");
                String clienteNombre = rs.getString("clienteNombre");
                String clienteTelefono = rs.getString("clienteTelefono");

                // Creamos la factura con los datos del cliente
                Factura factura = new Factura(id, fecha, total, abono, saldo, idCliente, clienteNombre, clienteTelefono);
                factura.setEstado(estado);
                factura.setEstadoTrabajo(estadoTrabajo);

                listaFacturas.add(factura);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listaFacturas;
    }
    public List<Factura> buscarFacturasPorProducto(String nombreProducto) {
        List<Factura> facturas = new ArrayList<>();

        String sql = "SELECT f.id_factura, f.fecha, f.total_factura, f.abono_realizado, f.saldo_pendiente, " +
                "f.estado_pago, f.estado_trabajo, c.nombre AS clienteNombre, c.telefono AS clienteTelefono, " +
                "i.id_item, i.producto, i.cantidad, i.valor_unitario, i.tipo " +
                "FROM facturas f " +
                "JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "JOIN items_factura i ON f.id_factura = i.id_factura " +
                "WHERE i.producto LIKE ? " +
                "ORDER BY f.id_factura";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombreProducto + "%");
            ResultSet rs = ps.executeQuery();

            Map<Integer, Factura> mapaFacturas = new LinkedHashMap<>();

            while (rs.next()) {
                int id = rs.getInt("id_factura");

                Factura factura = mapaFacturas.get(id);
                if (factura == null) {
                    Date fecha = rs.getDate("fecha");
                    double total = rs.getDouble("total_factura");
                    double abono = rs.getDouble("abono_realizado");
                    double saldo = rs.getDouble("saldo_pendiente");
                    String estado = rs.getString("estado_pago");
                    String estadoTrabajo = rs.getString("estado_trabajo");
                    String clienteNombre = rs.getString("clienteNombre");
                    String telefono = rs.getString("clienteTelefono");

                    factura = new Factura(id, fecha, clienteNombre, total, abono, saldo, 0, estado, estadoTrabajo);
                    factura.setTelefonoCliente(telefono);
                    factura.setItems(new ArrayList<>());  // Asegúrate de inicializar la lista
                    mapaFacturas.put(id, factura);
                }

                // Construir el ítem asociado a la factura
                ItemFactura item = new ItemFactura();
                item.setIdItem(rs.getInt("id_item"));
                item.setProducto(rs.getString("producto"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setTipoTrabajo(rs.getString("tipo"));


                factura.agregarItem(item);
            }

            facturas.addAll(mapaFacturas.values());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return facturas;
    }


    public List<Factura> obtenerFacturasPorCliente(int idCliente) {
        List<Factura> lista = new ArrayList<>();
        System.out.println("Consultando facturas para cliente ID: " + idCliente); // DEPURACIÓN

        String sql = "SELECT id_factura, fecha, total_factura, abono_realizado, saldo_pendiente, estado_pago, estado_trabajo FROM facturas WHERE id_cliente = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setId(rs.getInt("id_factura"));
                factura.setFecha(rs.getDate("fecha"));
                factura.setTotal(rs.getDouble("total_factura"));
                factura.setAbono(rs.getDouble("abono_realizado"));
                factura.setSaldo(rs.getDouble("saldo_pendiente"));
                factura.setEstado(rs.getString("estado_pago"));
                factura.setEstadoTrabajo(rs.getString("estado_trabajo"));
                factura.setIdCliente(idCliente);

                System.out.println("Factura encontrada: ID " + factura.getId() + ", Total " + factura.getTotal()); // DEPURACIÓN

                lista.add(factura);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener facturas: " + e.getMessage());
        }
        return lista;
    }

    public void guardarFacturaConItems(Factura factura) {
        factura.calcularTotales();

        if (factura.getAbono() >= factura.getTotal()) {
            factura.setEstado("Cancelada");
        } else {
            factura.setEstado("Pendiente");
        }

        String sqlFactura = "INSERT INTO facturas (fecha, total_factura, abono_realizado, saldo_pendiente, estado_pago, estado_trabajo, id_cliente) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO items_factura (id_factura, tipo, producto, cantidad, valor_unitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtFactura = conn.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                stmtFactura.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
                stmtFactura.setDouble(2, factura.getTotal());
                stmtFactura.setDouble(3, factura.getAbono());
                stmtFactura.setDouble(4, factura.getSaldo());
                stmtFactura.setString(5, factura.getEstado());
                stmtFactura.setString(6, factura.getEstadoTrabajo());
                stmtFactura.setInt(7, factura.getIdCliente());

                stmtFactura.executeUpdate();

                ResultSet rs = stmtFactura.getGeneratedKeys();
                if (rs.next()) {
                    int idFacturaGenerada = rs.getInt(1);

                    try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                        for (ItemFactura item : factura.getItems()) {
                            stmtItem.setInt(1, idFacturaGenerada);
                            stmtItem.setString(2, item.getTipo());
                            stmtItem.setString(3, item.getDescripcion());
                            stmtItem.setInt(4, item.getCantidad());
                            stmtItem.setDouble(5, item.getValorUnitario());
                            stmtItem.setDouble(6, item.getSubtotal());
                            stmtItem.addBatch();
                        }
                        stmtItem.executeBatch();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(null, "Factura e ítems guardados exitosamente.");
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "No se pudo obtener el ID de la factura.");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar la factura: " + e.getMessage());
        }
    }

    public boolean eliminarFactura(int idFactura) {
        Connection conn = null;
        PreparedStatement stmtItems = null;
        PreparedStatement stmtFactura = null;

        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción

            // Eliminar primero los ítems asociados a la factura
            String sqlEliminarItems = "DELETE FROM items_factura WHERE id_factura = ?";
            stmtItems = conn.prepareStatement(sqlEliminarItems);
            stmtItems.setInt(1, idFactura);
            stmtItems.executeUpdate();

            // Luego eliminar la factura
            String sqlEliminarFactura = "DELETE FROM facturas WHERE id_factura = ?";
            stmtFactura = conn.prepareStatement(sqlEliminarFactura);
            stmtFactura.setInt(1, idFactura);
            int filasAfectadas = stmtFactura.executeUpdate();

            conn.commit(); // Confirmar cambios si todo salió bien
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // Revertir cambios si hubo error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            ConexionBD.cerrar(null, stmtItems, null);
            ConexionBD.cerrar(conn, stmtFactura, null);
        }

        return false;
    }


    public Factura obtenerFacturaPorId(int idFactura) {
        Factura factura = null;
        String sql = "SELECT f.id_factura, f.fecha, f.total_factura, f.abono_realizado, f.saldo_pendiente, f.estado_pago, f.estado_trabajo, " +
                "c.id_cliente, c.nombre, c.telefono FROM facturas f JOIN clientes c ON f.id_cliente = c.id_cliente WHERE f.id_factura = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id_cliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setTelefono(rs.getString("telefono"));

                factura = new Factura();
                factura.setId(rs.getInt("id_factura"));
                factura.setFecha(rs.getDate("fecha"));
                factura.setTotal(rs.getDouble("total_factura"));
                factura.setAbono(rs.getDouble("abono_realizado"));
                factura.setSaldo(rs.getDouble("saldo_pendiente"));
                factura.setEstado(rs.getString("estado_pago"));
                factura.setEstadoTrabajo(rs.getString("estado_trabajo"));
                factura.setCliente(cliente);

                factura.setItems(obtenerItemsPorFactura(idFactura));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener factura: " + e.getMessage());
        }

        return factura;
    }


    public List<ItemFactura> obtenerItemsPorFactura(int idFactura) {
        List<ItemFactura> items = new ArrayList<>();
        String sql = "SELECT tipo, producto, cantidad, valor_unitario, subtotal FROM items_factura WHERE id_factura = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ItemFactura item = new ItemFactura();
                item.setTipo(rs.getString("tipo"));
                item.setDescripcion(rs.getString("producto"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setSubtotal(rs.getDouble("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener ítems: " + e.getMessage());
        }
        return items;
    }

    public ResumenFacturasMes obtenerFacturasDelMes(Date fecha) {
        List<Factura> facturasDelMes = new ArrayList<>();
        double totalFacturado = 0.0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int mes = cal.get(Calendar.MONTH) + 1;
        int anio = cal.get(Calendar.YEAR);

        String sql = "SELECT f.id_factura, f.id_cliente, f.fecha, f.total_factura, f.abono_realizado, f.estado_trabajo, c.nombre " +
                "FROM facturas f " +
                "JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "WHERE MONTH(f.fecha) = ? AND YEAR(f.fecha) = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mes);
            stmt.setInt(2, anio);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setId(rs.getInt("id_factura"));
                factura.setClienteNombre(rs.getString("nombre"));
                factura.setIdCliente(rs.getInt("id_cliente"));
                factura.setFecha(rs.getDate("fecha"));
                factura.setAbono(rs.getDouble("abono_realizado"));
                factura.setTotalFactura(rs.getDouble("total_factura"));

                totalFacturado += factura.getTotalFactura(); // Acumulador

                facturasDelMes.add(factura);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        ResumenFacturasMes resumen = new ResumenFacturasMes();
        resumen.setFacturas(facturasDelMes);
        resumen.setTotalFacturado(totalFacturado);
        return resumen;
    }





    public List<Factura> obtenerFacturasPorMesYAnio(int mes, int anio) {
        List<Factura> lista = new ArrayList<>();

        String sql = "SELECT f.id_factura, f.id_cliente, f.fecha, f.total_factura, f.abono_realizado, f.estado_trabajo, c.nombre " +
                "FROM facturas f " +
                "JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "WHERE MONTH(f.fecha) = ? AND YEAR(f.fecha) = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mes);
            stmt.setInt(2, anio);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Factura f = new Factura();
                f.setId(rs.getInt("id_factura"));
                f.setIdCliente(rs.getInt("id_cliente"));
                f.setClienteNombre(rs.getString("nombre"));
                f.setFecha(rs.getDate("fecha"));
                f.setAbono(rs.getDouble("abono_realizado"));
                f.setIdCliente(rs.getInt("id_cliente"));
                f.setTotalFactura(rs.getDouble("total_factura"));
                lista.add(f);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }





}


