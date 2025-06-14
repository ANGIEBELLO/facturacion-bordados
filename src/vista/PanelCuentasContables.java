package vista;

import controlador.CuentaContableDAO;

import modelo.CuentaContable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelCuentasContables extends JPanel {
    private JTextField txtCodigo, txtNombre;
    private JComboBox<String> comboTipo;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private CuentaContableDAO cuentaDAO;

    public PanelCuentasContables() {
        setLayout(new BorderLayout());
        cuentaDAO = new CuentaContableDAO();

        // Formulario
        JPanel form = new JPanel(new GridLayout(4, 2));
        form.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        form.add(txtCodigo);

        form.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        form.add(txtNombre);

        form.add(new JLabel("Tipo:"));
        comboTipo = new JComboBox<>(new String[]{"ACTIVO", "PASIVO", "INGRESO", "GASTO", "PATRIMONIO"});
        form.add(comboTipo);

        JButton btnAgregar = new JButton("Agregar");
        form.add(btnAgregar);

        JButton btnEliminar = new JButton("Eliminar");
        form.add(btnEliminar);  // Agregar al panel de formulario


        add(form, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"Código", "Nombre", "Tipo"}, 0);
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarCuenta());
        btnEliminar.addActionListener(e -> eliminarCuenta());

        cargarCuentas();
    }

    private void agregarCuenta() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim().toUpperCase(); // <- Convertimos a mayúsculas
        String tipo = (String) comboTipo.getSelectedItem();

        if (codigo.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        CuentaContable cuenta = new CuentaContable(codigo, nombre, tipo);
        if (cuentaDAO.agregarCuenta(cuenta)) {
            JOptionPane.showMessageDialog(this, "Cuenta agregada exitosamente.");
            cargarCuentas();
            txtCodigo.setText("");
            txtNombre.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Error al agregar la cuenta.");
        }
    }

    private void eliminarCuenta() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta para eliminar.");
            return;
        }

        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar la cuenta " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (cuentaDAO.eliminarCuenta(codigo)) {
                JOptionPane.showMessageDialog(this, "Cuenta eliminada exitosamente.");
                cargarCuentas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar la cuenta.");
            }
        }
    }


    private void cargarCuentas() {
        modeloTabla.setRowCount(0);
        List<CuentaContable> lista = cuentaDAO.listarCuentas();

        for (CuentaContable c : lista) {
            modeloTabla.addRow(new Object[]{
                    c.getCodigo(),
                    c.getNombre(),
                    c.getTipo()
            });
        }
    }
}
