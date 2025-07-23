package vista;

import controlador.ItemFacturaDAO;
import modelo.ItemFactura;
import util.GeneradorPDF;
import modelo.Empleado;
import controlador.EmpleadoDAO;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List; // ✅ Correcto



public class HistorialPagosEmpleadoDialog extends JDialog {

        private JTable tablaHistorial;
        private DefaultTableModel modeloTabla;
        private ItemFacturaDAO itemDAO = new ItemFacturaDAO();
        private Empleado empleado;



    public HistorialPagosEmpleadoDialog(JFrame parent, int idEmpleado) {
            super(parent, "Historial de Pagos del Empleado", true);
            setSize(800, 400);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            this.empleado = empleadoDAO.obtenerEmpleadoPorId(idEmpleado);


        JButton btnGenerarPDF = new JButton("Generar PDF");
            btnGenerarPDF.addActionListener(e -> generarPDF());

            JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            abajo.add(btnGenerarPDF);
            add(abajo, BorderLayout.SOUTH);



            JPanel panelBotones = new JPanel();
            panelBotones.add(btnGenerarPDF);
            add(panelBotones, BorderLayout.SOUTH);




            modeloTabla = new DefaultTableModel(new Object[]{
                    "Seleccionar", "Fecha de Pago", "ID Factura", "Producto", "Tipo", "Cantidad", "Valor Unitario", "Subtotal", "Pago","ID Item"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0; // Solo la columna de seleccionar
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : Object.class;
                }
            };

            tablaHistorial = new JTable(modeloTabla);
            add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);

            cargarHistorial(idEmpleado);
        }

        private void cargarHistorial(int idEmpleado) {
            ItemFacturaDAO dao = new ItemFacturaDAO();
            List<ItemFactura> historial = dao.obtenerItemsPagadosPorEmpleado(idEmpleado);

            for (ItemFactura item : historial) {
                double porcentaje = item.getTipo().equalsIgnoreCase("Bordado") ? 0.15 : 0.30;
                double pago = item.getSubtotal() * porcentaje;

                modeloTabla.addRow(new Object[]{
                        false,
                        item.getFechaPago() != null ? item.getFechaPago().toString() : "—",
                        item.getIdFactura(),
                        item.getNombre(),
                        item.getTipo(),
                        item.getCantidad(),
                        String.format("%,.0f", item.getValorUnitario()),
                        String.format("%,.0f", item.getSubtotal()),
                        String.format("%,.0f", pago),
                        item.getIdItem() // << Agrega esta columna extra
                });



            }
        }

    private void generarPDF() {
        List<ItemFactura> seleccionados = new ArrayList<>();

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            boolean seleccionado = (Boolean) modeloTabla.getValueAt(i, 0); // columna de selección
            if (seleccionado) {
                int idItem = Integer.parseInt(modeloTabla.getValueAt(i, 9).toString());
                // conversión segura
                ItemFactura item = itemDAO.obtenerPorId(idItem);
                if (item != null) {
                    seleccionados.add(item);
                }
            }

        }

        if (seleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un ítem.");
            return;
        }

        try {
            GeneradorPDF.generarPagoEmpleadoPDF(empleado, seleccionados); // pasa el empleado y los ítems
            JOptionPane.showMessageDialog(this, "PDF generado exitosamente.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el PDF.");
        }
    }



}


