package vista;

import modelo.Cliente;
import modelo.Factura;
import controlador.FacturaController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

public class PanelFacturasListado extends JPanel {

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private FacturaController facturaController;
    private JTextField txtBuscarProducto;
    private JButton btnBuscarProducto;
    private JButton btnAbrirFactura;
    // Panel de descripción inferior
    private JTextArea txtDescripcionFactura;


    public PanelFacturasListado() {
        facturaController = new FacturaController();
        setLayout(new BorderLayout());


        // Crear campos de búsqueda
        txtBuscarProducto = new JTextField(20);
        btnBuscarProducto = new JButton("Buscar por producto");
        btnAbrirFactura = new JButton("Abrir");

        JPanel panelBusqueda = new JPanel();
        panelBusqueda.add(btnAbrirFactura);
        panelBusqueda.add(new JLabel("Producto:"));
        panelBusqueda.add(txtBuscarProducto);
        panelBusqueda.add(btnBuscarProducto);
        this.add(panelBusqueda, BorderLayout.NORTH);

        // Definir columnas de la tabla
        String[] columnas = {"ID", "Fecha", "Cliente", "Teléfono", "Total", "Abono", "Saldo", "Estado", "Trabajo"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modeloTabla);

        JScrollPane scroll = new JScrollPane(tabla);
        this.add(scroll, BorderLayout.CENTER);

        // Área de descripción de factura
        txtDescripcionFactura = new JTextArea(5, 50);
        txtDescripcionFactura.setEditable(false);
        txtDescripcionFactura.setLineWrap(true);
        txtDescripcionFactura.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcionFactura);

        this.add(scrollDescripcion, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    int idFactura = (int) tabla.getValueAt(fila, 0);
                    Factura factura = facturaController.obtenerFacturaConItems(idFactura);

                    if (factura != null && factura.getItems() != null) {
                        StringBuilder descripcion = new StringBuilder();
                        descripcion.append("Productos:\n");

                        factura.getItems().forEach(item -> {
                            descripcion.append("- ").append(item.getDescripcion())
                                    .append(" | Cant: ").append(item.getCantidad())
                                    .append(" | Unit: $").append(item.getValorUnitario())
                                    .append(" | Subtotal: $").append(item.getSubtotal())
                                    .append("\n");
                        });

                        txtDescripcionFactura.setText(descripcion.toString());
                    }
                }
            }
        });



        // Cargar todas las facturas
        cargarFacturas();

        btnAbrirFactura.addActionListener((ActionEvent e) -> {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada >= 0) {
                int idFactura = (int) tabla.getValueAt(filaSeleccionada, 0);
                Factura factura = facturaController.obtenerFacturaConItems(idFactura);

                if (factura != null) {
                    // Obtener cliente y crear el nuevo panel
                    Cliente cliente = factura.getCliente();
                    PanelFacturas panelDetalle = new PanelFacturas(cliente, factura, null); // puedes pasar null si no vas a volver

                    // Crear nueva ventana
                    JFrame nuevaVentana = new JFrame("Factura #" + factura.getId());
                    nuevaVentana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // solo cierra esta ventana
                    nuevaVentana.setContentPane(panelDetalle);
                    nuevaVentana.pack(); // ajusta el tamaño
                    nuevaVentana.setLocationRelativeTo(null); // centra la ventana
                    nuevaVentana.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo cargar la factura.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una factura para abrir.");
            }
        });


        // Acción del botón de búsqueda
        btnBuscarProducto.addActionListener((ActionEvent e) -> {
            String producto = txtBuscarProducto.getText().trim();

            if (!producto.isEmpty()) {
                // Buscar facturas que contengan el producto ingresado
                List<Factura> facturas = facturaController.buscarFacturasPorProducto(producto);
                mostrarFacturasEnTabla(facturas);
            } else {
                // Si no hay texto en el campo, se cargan todas las facturas
                List<Factura> todas = facturaController.obtenerTodasLasFacturas();
                mostrarFacturasEnTabla(todas);
            }
        });

    }




    private void mostrarFacturasEnTabla(List<Factura> facturas) {
        modeloTabla.setRowCount(0); // Limpiar tabla
        for (Factura f : facturas) {
            modeloTabla.addRow(new Object[]{
                    f.getId(),
                    f.getFecha(),
                    f.getClienteNombre(),
                    f.getTelefonoCliente(),
                    f.getTotal(),
                    f.getAbono(),
                    f.getSaldo(),
                    f.getEstado(),
                    f.getEstadoTrabajo(),
                    f.getItems()
            });
        }
    }

    private void cargarFacturas() {
        List<Factura> facturas = facturaController.obtenerTodasLasFacturas();
        facturas.sort(Comparator.comparing(Factura::getFecha).reversed());
        mostrarFacturasEnTabla(facturas);
    }
}
