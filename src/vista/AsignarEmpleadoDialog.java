package vista;

import controlador.EmpleadoDAO;
import controlador.ItemFacturaDAO;
import modelo.Empleado;
import modelo.ItemFactura;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class AsignarEmpleadoDialog extends JDialog {
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private List<ItemFactura> items;
    private List<Empleado> empleados;

    public AsignarEmpleadoDialog(Frame parent) {
        super(parent, "Asignar Empleado a Ítems", true);
        setSize(800, 400);
        setLocationRelativeTo(parent);

        modeloTabla = new DefaultTableModel(new Object[]{
                "ID Ítem", "Factura", "Producto", "Tipo", "Cantidad", "Valor Unitario", "Subtotal", "Empleado"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tabla = new JTable(modeloTabla);
        JScrollPane scroll = new JScrollPane(tabla);

        JButton btnAsignar = new JButton("Asignar seleccionado");

        btnAsignar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asignarEmpleados();
            }
        });

        cargarDatos();

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(btnAsignar, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        ItemFacturaDAO itemDAO = new ItemFacturaDAO();
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();

        items = itemDAO.obtenerItemsSinEmpleado();
        empleados = empleadoDAO.obtenerTodos();

        // Preparar los nombres para el JComboBox global
        Vector<String> nombresEmpleados = new Vector<>();
        for (Empleado emp : empleados) {
            nombresEmpleados.add(emp.getId() + " - " + emp.getNombre());
        }

        // Cargar filas sin combo en la celda
        for (ItemFactura item : items) {
            modeloTabla.addRow(new Object[]{
                    item.getIdItem(),
                    item.getIdFactura(),
                    item.getNombre(),
                    item.getTipo(),
                    item.getCantidad(),
                    item.getValorUnitario(),
                    item.getSubtotal(),
                    null  // la columna Empleado será editable con el comboBox
            });
        }

        // Asignar comboBox como editor para la columna de empleados
        tabla.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JComboBox<>(nombresEmpleados)));
    }


    private void asignarEmpleados() {
        ItemFacturaDAO itemDAO = new ItemFacturaDAO();
        boolean exito = true;

        for (int i = 0; i < tabla.getRowCount(); i++) {
            int idItem = (int) tabla.getValueAt(i, 0);
            JComboBox combo = (JComboBox) tabla.getCellEditor(i, 7).getTableCellEditorComponent(tabla, tabla.getValueAt(i, 7), true, i, 7);
            String seleccionado = (String) combo.getSelectedItem();

            if (seleccionado != null && seleccionado.contains("-")) {
                int idEmpleado = Integer.parseInt(seleccionado.split(" - ")[0].trim());
                if (!itemDAO.asignarEmpleadoAItem(idItem, idEmpleado)) {
                    exito = false;
                }
            }
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "Empleados asignados correctamente.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al asignar uno o más empleados.");
        }
    }
}

