package vista;

import modelo.Empleado;
import controlador.EmpleadoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelEmpleados extends JPanel {
    private JTextField txtNombre, txtCedula, txtTelefono, txtCargo;
    private JButton btnAgregar, btnActualizar, btnEliminar, btnLimpiar;
    private JTable tablaEmpleados;
    private DefaultTableModel modeloTabla;

    private EmpleadoDAO empleadoDAO;
    private int empleadoSeleccionadoId = -1;

    public PanelEmpleados() {
        empleadoDAO = new EmpleadoDAO();
        setLayout(new BorderLayout());

        // Panel de formulario
        JPanel formulario = new JPanel(new GridLayout(5, 2, 10, 10));
        formulario.setBorder(BorderFactory.createTitledBorder("Gestión de Empleados"));

        JButton btnGestionarPagos = new JButton("Gestionar Pagos");
        btnGestionarPagos.setIcon(new ImageIcon("src/recursos/pagos.png")); // opcional

        btnGestionarPagos.addActionListener(e -> abrirPanelPagosEmpleado());
        add(btnGestionarPagos, BorderLayout.SOUTH);

        JButton btnAsignarItems = new JButton("Asignar ítems a empleado");
        btnAsignarItems.setBackground(new Color(70, 130, 180)); // color azul
        btnAsignarItems.setForeground(Color.WHITE);
        btnAsignarItems.setFocusPainted(false);
        btnAsignarItems.setFont(new Font("Segoe UI", Font.BOLD, 14));


        txtNombre = new JTextField();
        txtCedula = new JTextField();
        txtTelefono = new JTextField();
        txtCargo = new JTextField();

        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Cédula:"));
        formulario.add(txtCedula);
        formulario.add(new JLabel("Teléfono:"));
        formulario.add(txtTelefono);
        formulario.add(new JLabel("Cargo:"));
        formulario.add(txtCargo);

        btnAgregar = new JButton("Agregar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnAgregar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnAsignarItems);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(formulario, BorderLayout.CENTER);
        panelSuperior.add(botones, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Cédula", "Teléfono", "Cargo"}, 0);
        tablaEmpleados = new JTable(modeloTabla);
        tablaEmpleados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tablaEmpleados);
        add(scroll, BorderLayout.CENTER);

        cargarEmpleados();

        // Listeners
        btnAgregar.addActionListener(e -> agregarEmpleado());
        btnActualizar.addActionListener(e -> actualizarEmpleado());
        btnEliminar.addActionListener(e -> eliminarEmpleado());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnAsignarItems.addActionListener(e -> {
            Frame frame = JOptionPane.getRootFrame(); // o usa tu JFrame principal si lo tienes
            AsignarEmpleadoDialog dialog = new AsignarEmpleadoDialog(frame);
            dialog.setVisible(true);
        });

        tablaEmpleados.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tablaEmpleados.getSelectedRow();
                if (fila != -1) {
                    empleadoSeleccionadoId = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
                    txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                    txtCedula.setText(modeloTabla.getValueAt(fila, 2).toString());
                    txtTelefono.setText(modeloTabla.getValueAt(fila, 3).toString());
                    txtCargo.setText(modeloTabla.getValueAt(fila, 4).toString());
                }
            }
        });
    }

    private void agregarEmpleado() {
        String nombre = capitalizarTexto(txtNombre.getText().trim());
        String cedula = txtCedula.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String cargo = capitalizarTexto(txtCargo.getText().trim());

        if (nombre.isEmpty() || cedula.isEmpty() || telefono.isEmpty() || cargo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        Empleado emp = new Empleado(nombre, cedula, telefono, cargo);
        if (empleadoDAO.agregarEmpleado(emp)) {
            JOptionPane.showMessageDialog(this, "Empleado agregado correctamente.");
            cargarEmpleados();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al agregar empleado.");
        }
    }

    private void actualizarEmpleado() {
        if (empleadoSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para actualizar.");
            return;
        }

        String nombre = capitalizarTexto(txtNombre.getText().trim());
        String cedula = txtCedula.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String cargo = capitalizarTexto(txtCargo.getText().trim());

        Empleado emp = new Empleado(empleadoSeleccionadoId, nombre, cedula, telefono, cargo);
        if (empleadoDAO.actualizarEmpleado(emp)) {
            JOptionPane.showMessageDialog(this, "Empleado actualizado correctamente.");
            cargarEmpleados();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar empleado.");
        }
    }

    private void eliminarEmpleado() {
        if (empleadoSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar este empleado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (empleadoDAO.eliminarEmpleado(empleadoSeleccionadoId)) {
                JOptionPane.showMessageDialog(this, "Empleado eliminado.");
                cargarEmpleados();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }

    private void cargarEmpleados() {
        modeloTabla.setRowCount(0);
        List<Empleado> lista = empleadoDAO.listarEmpleados();
        for (Empleado emp : lista) {
            modeloTabla.addRow(new Object[]{emp.getId(), emp.getNombre(), emp.getCedula(), emp.getTelefono(), emp.getCargo()});
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtCedula.setText("");
        txtTelefono.setText("");
        txtCargo.setText("");
        empleadoSeleccionadoId = -1;
    }

    private void abrirPanelPagosEmpleado() {
        int filaSeleccionada = tablaEmpleados.getSelectedRow();
        if (filaSeleccionada != -1) {
            int idEmpleado = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

            PagosEmpleadoDialog dialog = new PagosEmpleadoDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), // ⬅️ ventana padre correcta
                    idEmpleado
            );
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado.");
        }


    }
    private String capitalizarTexto (String texto){
        String[] palabras = texto.toLowerCase().split(" ");
        StringBuilder capitalizado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                capitalizado.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1)).append(" ");
            }
        }
        return capitalizado.toString().trim();
    }
}