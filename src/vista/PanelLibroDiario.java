package vista;

import util.ConexionBD;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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
    private JDateChooser fechaDesde;
    private JDateChooser fechaHasta;


    public PanelLibroDiario() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Inicializar fechas ANTES de usar
        fechaDesde = new JDateChooser();
        fechaHasta = new JDateChooser();

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
        campoMonto.getDocument().addDocumentListener(new DocumentListener() {
            private boolean isUpdating = false;

            @Override
            public void insertUpdate(DocumentEvent e) {
                formatearCampoMonto();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                formatearCampoMonto();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                formatearCampoMonto();
            }

            private void formatearCampoMonto() {
                if (isUpdating) return;
                isUpdating = true;

                SwingUtilities.invokeLater(() -> {
                    String texto = campoMonto.getText().replaceAll("[^\\d]", "");

                    if (texto.isEmpty()) {
                        campoMonto.setText("");
                        isUpdating = false;
                        return;
                    }

                    try {
                        long valor = Long.parseLong(texto);
                        NumberFormat formatoColombiano = NumberFormat.getNumberInstance(new Locale("es", "CO"));
                        formatoColombiano.setMaximumFractionDigits(0); // sin decimales
                        String formateado = formatoColombiano.format(valor);
                        campoMonto.setText(formateado);
                    } catch (NumberFormatException ex) {
                        // Ignora entradas inválidas
                    } finally {
                        isUpdating = false;
                    }
                });
            }
        });


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

        // Inicializar filtros (primero)
        fechaDesde = new JDateChooser();
        fechaHasta = new JDateChooser();
        fechaDesde.setDate(new Date()); // hoy
        fechaHasta.setDate(new Date()); // hoy

        fechaDesde.setPreferredSize(new Dimension(150, 25));
        fechaHasta.setPreferredSize(new Dimension(150, 25));

        comboFiltroCuenta = new JComboBox<>();
        campoFiltroFecha = new JTextField(10);
        btnFiltrar = new JButton("Filtrar");

        lblSaldo = new JLabel("Saldo actual:");
        campoSaldo = new JTextField(10);
        campoSaldo.setEditable(false);
        campoSaldo.setForeground(Color.BLUE);
        campoSaldo.setFont(campoSaldo.getFont().deriveFont(Font.BOLD));

        // Panel de filtros organizado con GridBagLayout
        JPanel panelFiltros = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // márgenes entre componentes
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFiltros.add(new JLabel("Desde:"), gbc);
        gbc.gridx = 1;
        panelFiltros.add(fechaDesde, gbc);

        gbc.gridx = 2;
        panelFiltros.add(new JLabel("Hasta:"), gbc);
        gbc.gridx = 3;
        panelFiltros.add(fechaHasta, gbc);

        gbc.gridx = 4;
        panelFiltros.add(new JLabel("Cuenta:"), gbc);
        gbc.gridx = 5;
        panelFiltros.add(comboFiltroCuenta, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFiltros.add(lblSaldo, gbc);
        gbc.gridx = 1;
        panelFiltros.add(campoSaldo, gbc);

        gbc.gridx = 4;
        panelFiltros.add(btnFiltrar, gbc);

        // Agregar el panel de filtros al norte
        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"Fecha", "Descripción", "Cuenta", "Tipo", "Monto"}, 0);
        tablaMovimientos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaMovimientos);
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        // Agregar todo al panel principal
        add(panelCentro, BorderLayout.CENTER);

        // Evento del combo
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
        String montoStr = campoMonto.getText().trim().replace(".", "").replace(",", ".");
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
        Date desde = fechaDesde.getDate();
        Date hasta = fechaHasta.getDate();
        String cuentaSeleccionada = (String) comboFiltroCuenta.getSelectedItem();

        if (desde == null || hasta == null || cuentaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un rango de fechas y una cuenta.");
            return;
        }

        int cuentaId = Integer.parseInt(cuentaSeleccionada.split(" - ")[0]);
        java.sql.Date sqlDesde = new java.sql.Date(desde.getTime());
        java.sql.Date sqlHasta = new java.sql.Date(hasta.getTime());

        String sql = "SELECT fecha, descripcion, tipo, monto FROM transaccion " +
                "WHERE cuenta_id = ? AND fecha BETWEEN ? AND ? ORDER BY fecha ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cuentaId);
            stmt.setDate(2, sqlDesde);
            stmt.setDate(3, sqlHasta);

            ResultSet rs = stmt.executeQuery();

            // Limpiar la tabla antes de insertar nuevos datos
            modeloTabla.setRowCount(0);

            while (rs.next()) {
                Object[] fila = {
                        rs.getDate("fecha"),
                        rs.getString("descripcion"),
                        cuentaSeleccionada.split(" - ")[1],  // solo nombre cuenta
                        rs.getString("tipo"),
                        String.format("$ %,.2f", rs.getDouble("monto"))
                };
                modeloTabla.addRow(fila);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar movimientos: " + ex.getMessage());
        }
    }




}
