package vista;

import modelo.ItemFactura;
import controlador.ItemFacturaDAO;
import controlador.PagoEmpleadoController;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PagosEmpleadoDialog extends JDialog {

    private PagoEmpleadoController pagoController = new PagoEmpleadoController();


    private JTable tablaItems;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private JButton btnPagar;
    private JButton btnHistorial;
    private int idEmpleado;
    private ItemFacturaDAO itemDAO = new ItemFacturaDAO();

    public PagosEmpleadoDialog(JFrame parent, int idEmpleado) {
        super(parent, "Pagos a Empleado", true);
        this.idEmpleado = idEmpleado;

        setSize(700, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        modeloTabla = new DefaultTableModel(new Object[]{
                "Seleccionar", "ID Ítem", "Factura", "Producto", "Tipo", "Cantidad", "Valor Unitario", "Subtotal", "Pago"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // solo la columna "Seleccionar"
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }
        };

        tablaItems = new JTable(modeloTabla);
        add(new JScrollPane(tablaItems), BorderLayout.CENTER);

        lblTotal = new JLabel("Total a pagar: $0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));

        btnPagar = new JButton("Pagar seleccionado");
        btnPagar.addActionListener(e -> pagarSeleccionados());

        btnHistorial = new JButton("Ver Historial de Pagos");
        btnHistorial.addActionListener(e -> {
            HistorialPagosEmpleadoDialog dialog = new HistorialPagosEmpleadoDialog(parent, idEmpleado);
            dialog.setVisible(true);
        });

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(lblTotal, BorderLayout.WEST);

// 👉 Panel para botones a la derecha
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnPagar);
        panelBotones.add(btnHistorial);

        abajo.add(panelBotones, BorderLayout.EAST);

        add(abajo, BorderLayout.SOUTH);
        ;

        cargarItems();
    }

    private void cargarItems() {
        modeloTabla.setRowCount(0);
        List<ItemFactura> items = itemDAO.obtenerItemsNoPagadosPorEmpleado(idEmpleado);

        double total = 0;
        for (ItemFactura item : items) {
            double porcentaje = item.getTipo().equalsIgnoreCase("Bordado") ? 0.15 : 0.30;
            double pago = item.getSubtotal() * porcentaje;

            modeloTabla.addRow(new Object[]{
                    false,  // ⬅ casilla de selección
                    item.getIdItem(),
                    item.getIdFactura(),
                    item.getNombre(),
                    item.getTipo(),
                    item.getCantidad(),
                    String.format("%,.0f", item.getValorUnitario()),
                    String.format("%,.0f", item.getSubtotal()),
                    String.format("%,.0f", pago)
            });

            total += pago;
        }

        lblTotal.setText("Total estimado a pagar: $" + String.format("%,.0f", total));
    }


    private void pagarSeleccionados() {
        List<ItemFactura> itemsSeleccionados = new ArrayList<>();

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            boolean seleccionado = (boolean) modeloTabla.getValueAt(i, 0);
            if (seleccionado) {
                int idItem = (int) modeloTabla.getValueAt(i, 1); // columna ID Ítem
                ItemFactura item = itemDAO.obtenerPorId(idItem);
                if (item != null) {
                    itemsSeleccionados.add(item);
                }
            }
        }

        if (itemsSeleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un ítem para pagar.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Confirmas que los ítems seleccionados fueron pagados?",
                "Confirmar pago", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            PagoEmpleadoController pagoController = new PagoEmpleadoController();
            boolean exito = pagoController.marcarItemsComoPagados(itemsSeleccionados);

            if (exito) {
                JOptionPane.showMessageDialog(this, "Pago registrado correctamente y transacción guardada.");
                cargarItems();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el pago.");
            }
        }
    }


}

