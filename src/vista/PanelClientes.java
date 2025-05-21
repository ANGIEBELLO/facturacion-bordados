package vista;

import controlador.ClienteController;
import modelo.Cliente;
import util.Capitalizador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class PanelClientes extends JPanel {
    private JTextField txtNombre, txtTelefono, txtBuscar;
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private ClienteController controller;
    private JButton btnGestionarFacturas;

    public PanelClientes() {
        controller = new ClienteController();
        setLayout(new BorderLayout());

        // Panel superior
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelFormulario.add(txtNombre, gbc);

        // Teléfono
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefono = new JTextField(20);
        panelFormulario.add(txtTelefono, gbc);

        // Buscar
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Buscar (nombre o celular):"), gbc);
        gbc.gridx = 1;
        txtBuscar = new JTextField(20);
        panelFormulario.add(txtBuscar, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnBuscar = new JButton("Buscar");
        btnGestionarFacturas = new JButton("Gestionar Facturas");
        btnGestionarFacturas.setEnabled(false); // Desactivado inicialmente
        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnBuscar);
        panelBotones.add(btnGestionarFacturas); // Nuevo botón

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // Tabla de clientes
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Teléfono"}, 0);
        tablaClientes = new JTable(modeloTabla);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // Carga inicial
        cargarClientes();

        // Listener Agregar
        btnAgregar.addActionListener((ActionEvent e) -> {
            String nombreRaw = txtNombre.getText().trim();
            if (nombreRaw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
                return;
            }
            String nombre = Capitalizador.capitalizarPorPalabras(nombreRaw);
            String telefono = txtTelefono.getText().trim();
            if (telefono.isEmpty()) {
                if (controller.clienteExistePorNombre(nombre)) {
                    JOptionPane.showMessageDialog(this, "El cliente '" + nombre + "' ya existe.");
                    return;
                }
            } else {
                if (controller.clienteExiste(nombre, telefono)) {
                    JOptionPane.showMessageDialog(this, "El cliente '" + nombre + "' con teléfono '" + telefono + "' ya existe.");
                    return;
                }
            }
            if (controller.agregarCliente(new Cliente(nombre, telefono))) {
                JOptionPane.showMessageDialog(this, "Cliente agregado correctamente.");
                limpiarCampos();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar cliente.");
            }
        });

        // Listener Actualizar
        btnActualizar.addActionListener((ActionEvent e) -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente para actualizar.");
                return;
            }
            int id = (int) modeloTabla.getValueAt(fila, 0);
            String nombreRaw = txtNombre.getText().trim();
            if (nombreRaw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
                return;
            }
            String nombre = Capitalizador.capitalizarPorPalabras(nombreRaw);
            String telefono = txtTelefono.getText().trim();
            if (controller.actualizarCliente(new Cliente(id, nombre, telefono))) {
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.");
                limpiarCampos();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar cliente.");
            }
        });

        // Listener Eliminar
        btnEliminar.addActionListener((ActionEvent e) -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.");
                return;
            }
            int id = (int) modeloTabla.getValueAt(fila, 0);
            if (controller.eliminarCliente(id)) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
                limpiarCampos();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar cliente.");
            }
        });

        // Listener Buscar
        btnBuscar.addActionListener((ActionEvent e) -> {
            String criterio = txtBuscar.getText().trim();
            if (criterio.isEmpty()) {
                cargarClientes();
            } else {
                cargarClientes(controller.buscarPorNombreOTelefono(criterio));
            }
        });

        // Selección de fila
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtTelefono.setText(modeloTabla.getValueAt(fila, 2).toString());
                btnGestionarFacturas.setEnabled(true);
            } else {
                btnGestionarFacturas.setEnabled(false);
            }
        });

        // Listener del botón "Gestionar Facturas"
        btnGestionarFacturas.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                int id = (int) modeloTabla.getValueAt(fila, 0);
                String nombre = modeloTabla.getValueAt(fila, 1).toString();
                String telefono = modeloTabla.getValueAt(fila, 2).toString();
                Cliente cliente = new Cliente(id, nombre, telefono);

                VentanaFacturasCliente ventana = new VentanaFacturasCliente(cliente);
                ventana.setVisible(true);
            }
        });
    }

    private void cargarClientes() {
        cargarClientes(controller.obtenerTodos());
    }

    private void cargarClientes(List<Cliente> lista) {
        modeloTabla.setRowCount(0);
        for (Cliente c : lista) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getNombre(), c.getTelefono()});
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtTelefono.setText("");
        txtBuscar.setText("");
        tablaClientes.clearSelection();
        btnGestionarFacturas.setEnabled(false);
    }
}
