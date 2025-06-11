package vista;

import util.ConexionBD;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

public class PanelLibroDiario extends JPanel {
    private JComboBox<String> comboOperacion;
    private JComboBox<String> comboCuentaPrincipal;
    private JComboBox<String> comboCuentaContrapartida;
    private JTextField campoDescripcion;
    private JTextField campoMonto;
    private JButton btnGuardar;

    private JComboBox<String> comboFiltroCuenta;
    private JTextField campoFiltroFecha;
    private JButton btnFiltrar;
    private JTable tablaMovimientos;
    private DefaultTableModel modeloTabla;
    private JTextField campoSaldo;
    private JLabel lblSaldo;
    private final Map<String, AbstractMap.SimpleEntry<String, String>> cuentasPorOperacion = new HashMap<>();
    private final List<String> cuentasDisponibles = new ArrayList<>();

    public PanelLibroDiario() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarOperaciones();
        crearPanelRegistro();
        crearPanelFiltrosYTabla();

        cargarCuentas();
        comboOperacion.addItemListener(this::operacionSeleccionada);
        btnGuardar.addActionListener(e -> guardarMovimiento());
        btnFiltrar.addActionListener(e -> filtrarMovimientos());
    }

    private void inicializarOperaciones() {
        cuentasPorOperacion.put("Compra de materiales", new AbstractMap.SimpleEntry<>("Gastos operativos", "Caja"));
        cuentasPorOperacion.put("Pago a empleado", new AbstractMap.SimpleEntry<>("Nómina", "Caja"));
        cuentasPorOperacion.put("Ingreso por servicio", new AbstractMap.SimpleEntry<>("Caja", "Ingresos"));
        cuentasPorOperacion.put("Otro gasto", new AbstractMap.SimpleEntry<>("Otros gastos", "Caja"));
        cuentasPorOperacion.put("Otro ingreso", new AbstractMap.SimpleEntry<>("Caja", "Otros ingresos"));
    }

    private void crearPanelRegistro() {
        JPanel panelRegistro = new JPanel(new GridBagLayout());
        panelRegistro.setBorder(BorderFactory.createTitledBorder("Registrar Movimiento"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        campoDescripcion = new JTextField(20);
        campoMonto = new JTextField(10);
        comboOperacion = new JComboBox<>(cuentasPorOperacion.keySet().toArray(new String[0]));
        comboCuentaPrincipal = new JComboBox<>();
        comboCuentaContrapartida = new JComboBox<>();
        btnGuardar = new JButton("Guardar Movimiento");

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panelRegistro.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; panelRegistro.add(campoDescripcion, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panelRegistro.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1; panelRegistro.add(campoMonto, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panelRegistro.add(new JLabel("Operación:"), gbc);
        gbc.gridx = 1; panelRegistro.add(comboOperacion, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panelRegistro.add(new JLabel("Cuenta Principal:"), gbc);
        gbc.gridx = 1; panelRegistro.add(comboCuentaPrincipal, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panelRegistro.add(new JLabel("Cuenta Contrapartida:"), gbc);
        gbc.gridx = 1; panelRegistro.add(comboCuentaContrapartida, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; panelRegistro.add(btnGuardar, gbc);

        add(panelRegistro, BorderLayout.NORTH);
    }

    private void crearPanelFiltrosYTabla() {
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboFiltroCuenta = new JComboBox<>();
        campoFiltroFecha = new JTextField(10);
        btnFiltrar = new JButton("Filtrar");

        lblSaldo = new JLabel("Saldo actual:");
        campoSaldo = new JTextField(10);
        campoSaldo.setEditable(false);
        campoSaldo.setForeground(Color.BLUE);
        campoSaldo.setFont(campoSaldo.getFont().deriveFont(Font.BOLD));

        panelFiltros.add(new JLabel("Cuenta:"));
        panelFiltros.add(comboFiltroCuenta);
        panelFiltros.add(lblSaldo);  // Añade el label
        panelFiltros.add(campoSaldo);
        panelFiltros.add(new JLabel("Fecha (AAAA-MM-DD):"));
        panelFiltros.add(campoFiltroFecha);
        panelFiltros.add(btnFiltrar);

        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"Fecha", "Descripción", "Cuenta", "Tipo", "Monto"}, 0);
        tablaMovimientos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaMovimientos);
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);
        comboFiltroCuenta.addActionListener(e -> mostrarSaldoCuentaSeleccionada());
    }

    private void mostrarSaldoCuentaSeleccionada() {
        String cuentaSel = (String) comboFiltroCuenta.getSelectedItem();
        if (cuentaSel == null) return;

        int cuentaId = Integer.parseInt(cuentaSel.split(" - ")[0]);

        String sql = "SELECT " +
                "SUM(CASE WHEN tipo = 'DEBE' THEN monto ELSE 0 END) AS total_debe, " +
                "SUM(CASE WHEN tipo = 'HABER' THEN monto ELSE 0 END) AS total_haber " +
                "FROM transaccion WHERE cuenta_id = ?";

        String tipoCuenta = obtenerTipoCuentaDesdeBD(cuentaId);

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cuentaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double totalDebe = rs.getDouble("total_debe");
                double totalHaber = rs.getDouble("total_haber");

                double saldo;
                if ("ACTIVO".equalsIgnoreCase(tipoCuenta) || "GASTO".equalsIgnoreCase(tipoCuenta)) {
                    saldo = totalDebe - totalHaber;
                } else {
                    saldo = totalHaber - totalDebe;
                }

                campoSaldo.setText(String.format("$ %,.2f", saldo));
            }

        } catch (SQLException ex) {
            campoSaldo.setText("Error");
            JOptionPane.showMessageDialog(this, "Error al calcular saldo: " + ex.getMessage());
        }
    }


    private void cargarCuentas() {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nombre FROM cuenta")) {

            while (rs.next()) {
                String item = rs.getInt("id") + " - " + rs.getString("nombre");
                cuentasDisponibles.add(item);
                comboCuentaPrincipal.addItem(item);
                comboCuentaContrapartida.addItem(item);
                comboFiltroCuenta.addItem(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar cuentas: " + e.getMessage());
        }
    }

    private void operacionSeleccionada(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String operacion = (String) e.getItem();
            AbstractMap.SimpleEntry<String, String> cuentas = cuentasPorOperacion.get(operacion);
            if (cuentas != null) {
                seleccionarCuentaPorNombre(comboCuentaPrincipal, cuentas.getKey());
                seleccionarCuentaPorNombre(comboCuentaContrapartida, cuentas.getValue());
            }
        }
    }

    private void seleccionarCuentaPorNombre(JComboBox<String> combo, String nombreCuenta) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.endsWith(" - " + nombreCuenta)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void guardarMovimiento() {
        String descripcion = campoDescripcion.getText().trim();
        String montoStr = campoMonto.getText().trim();
        String operacion = (String) comboOperacion.getSelectedItem();

        if (descripcion.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto inválido.");
            return;
        }

        String cuentaPrincipalSel = (String) comboCuentaPrincipal.getSelectedItem();
        String cuentaContraSel = (String) comboCuentaContrapartida.getSelectedItem();

        if (cuentaPrincipalSel == null || cuentaContraSel == null || cuentaPrincipalSel.equals(cuentaContraSel)) {
            JOptionPane.showMessageDialog(this, "Selecciona dos cuentas distintas.");
            return;
        }

        int cuentaPrincipalId = Integer.parseInt(cuentaPrincipalSel.split(" - ")[0]);
        int cuentaContraId = Integer.parseInt(cuentaContraSel.split(" - ")[0]);

        String tipoPrincipal = operacion.toLowerCase().contains("ingreso") ? "HABER" : "DEBE";
        String tipoContrapartida = tipoPrincipal.equals("DEBE") ? "HABER" : "DEBE";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO transaccion (fecha, descripcion, cuenta_id, tipo, monto) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());

                stmt.setDate(1, hoy);
                stmt.setString(2, descripcion);
                stmt.setInt(3, cuentaPrincipalId);
                stmt.setString(4, tipoPrincipal);
                stmt.setDouble(5, monto);
                stmt.executeUpdate();

                stmt.setDate(1, hoy);
                stmt.setString(2, descripcion + " (Contrapartida)");
                stmt.setInt(3, cuentaContraId);
                stmt.setString(4, tipoContrapartida);
                stmt.setDouble(5, monto);
                stmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Movimiento guardado correctamente.");
                campoDescripcion.setText("");
                campoMonto.setText("");
                comboOperacion.setSelectedIndex(0);
                modeloTabla.setRowCount(0); // limpiar
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private String obtenerTipoCuentaDesdeBD(int cuentaId) {
        String tipo = "ACTIVO"; // Por defecto
        String sql = "SELECT tipo FROM cuenta WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cuentaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tipo = rs.getString("tipo");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tipo;
    }



    private void filtrarMovimientos() {
        modeloTabla.setRowCount(0);
        String cuentaSel = (String) comboFiltroCuenta.getSelectedItem();
        String fecha = campoFiltroFecha.getText().trim();

        if (cuentaSel == null) return;
        int cuentaId = Integer.parseInt(cuentaSel.split(" - ")[0]);

        String sql = "SELECT t.fecha, t.descripcion, c.nombre, t.tipo, t.monto " +
                "FROM transaccion t JOIN cuenta c ON t.cuenta_id = c.id " +
                "WHERE t.cuenta_id = ?" + (!fecha.isEmpty() ? " AND t.fecha = ?" : "") + " ORDER BY t.fecha DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cuentaId);
            if (!fecha.isEmpty()) {
                stmt.setDate(2, java.sql.Date.valueOf(fecha));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getDate("fecha"),
                        rs.getString("descripcion"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getDouble("monto")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar: " + ex.getMessage());
        }
    }
}
