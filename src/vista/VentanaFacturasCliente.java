package vista;

import modelo.Cliente;
import modelo.Factura;
import controlador.FacturaController;
import modelo.ItemFactura;
import util.GeneradorPDF;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VentanaFacturasCliente extends JFrame {
    private Cliente cliente;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JButton btnNueva, btnEliminar, btnModificar, btnActualizar;
    private FacturaController facturaController;
    private JTable tablaItems;

    public VentanaFacturasCliente(Cliente cliente) {
        this.cliente = cliente;
        this.facturaController = new FacturaController();

        setTitle("Gestión de Facturas - " + cliente.getNombre());
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        cargarFacturas();

        configurarEventosTablaFacturas();
    }

    private void initComponents() {
        // Tabla de Facturas
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Fecha", "Total", "Abono","Saldo", "Estado"}, 0);
        tablaFacturas = new JTable(modeloTabla);
        JScrollPane scrollFacturas = new JScrollPane(tablaFacturas);

        // Tabla de Ítems
        tablaItems = new JTable();
        tablaItems.setEnabled(false);
        JScrollPane scrollItems = new JScrollPane(tablaItems);

        // Panel dividido para mostrar ambas tablas
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollFacturas, scrollItems);
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);

        // Panel de Botones
        JPanel panelBotones = new JPanel();
        btnNueva = new JButton("Nueva Factura");
        btnEliminar = new JButton("Eliminar Factura");
        btnModificar = new JButton("Modificar Factura");
        btnActualizar = new JButton("Actualizar");
        JButton btnAbrirFacturaPDF = new JButton("Generar PDF");
        JButton btnCancelar = new JButton("Cancelar");

        panelBotones.add(btnNueva);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        //panelBotones.add(btnActualizar);
        panelBotones.add(btnAbrirFacturaPDF);
        panelBotones.add(btnCancelar);


        add(panelBotones, BorderLayout.SOUTH);

        // Acciones de los botones
        btnCancelar.addActionListener(e -> dispose());
        btnNueva.addActionListener(e -> crearFactura());
        btnEliminar.addActionListener(e -> eliminarFactura());
        btnModificar.addActionListener(e -> modificarFactura());
        btnActualizar.addActionListener(e -> cargarFacturas());
        btnAbrirFacturaPDF.addActionListener(e -> {
            int fila = tablaFacturas.getSelectedRow();
            if (fila >= 0) {
                int idFactura = (int) modeloTabla.getValueAt(fila, 0); // ID en columna 0
                Factura factura = facturaController.obtenerFacturaPorId(idFactura); // Debes tener este método
                if (factura != null) {
                    GeneradorPDF.generarFacturaPDF(factura);
                } else {
                    JOptionPane.showMessageDialog(null, "No se pudo obtener la factura.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione una factura.");
            }
        });


    }

    private void cargarFacturas() {
        modeloTabla.setRowCount(0);
        List<Factura> facturas = facturaController.obtenerFacturasPorCliente(cliente.getId());

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        NumberFormat formatoPesos = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        for (Factura f : facturas) {
            modeloTabla.addRow(new Object[]{
                    f.getId(),
                    formatoFecha.format(f.getFecha()),
                    formatoPesos.format(f.getTotal()),
                    formatoPesos.format(f.getAbono()),
                    formatoPesos.format(f.getSaldo()),
                    f.getEstado()
            });
        }

        tablaItems.setModel(new DefaultTableModel()); // Limpia tabla de ítems al recargar
    }

    private void crearFactura() {
        PanelFacturas panel = new PanelFacturas(cliente);
        JFrame frame = new JFrame("Nueva Factura");
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cargarFacturas();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                cargarFacturas();
            }
        });
    }

    private void eliminarFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila != -1) {
            int idFactura = (int) modeloTabla.getValueAt(fila, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar esta factura?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean eliminada = facturaController.eliminarFactura(idFactura);
                if (eliminada) {
                    JOptionPane.showMessageDialog(this, "Factura eliminada exitosamente.");
                    cargarFacturas();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la factura.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona una factura.");
        }
    }


    private void modificarFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila != -1) {
            int idFactura = (int) modeloTabla.getValueAt(fila, 0);
            Factura factura = facturaController.obtenerFacturaPorId(idFactura);
            if (factura != null) {
                PanelFacturas panel = new PanelFacturas(cliente, factura);
                JFrame frame = new JFrame("Modificar Factura");
                frame.setContentPane(panel);
                frame.pack();
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        cargarFacturas();
                    }

                    @Override
                    public void windowClosing(WindowEvent e) {
                        cargarFacturas();
                    }
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona una factura para modificar.");
        }
    }

    private void llenarTablaConItems(int idFactura) {
        Factura factura = facturaController.obtenerFacturaPorId(idFactura);

        if (factura == null) {
            JOptionPane.showMessageDialog(this, "Factura no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<ItemFactura> items = factura.getItems();
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new String[]{"Tipo", "Producto", "Cantidad", "Valor Unitario", "Subtotal"});

        for (ItemFactura item : items) {
            modelo.addRow(new Object[]{
                    item.getTipo(),
                    item.getDescripcion(),
                    item.getCantidad(),
                    String.format("$%,.0f", item.getValorUnitario()),
                    String.format("$%,.0f", item.getSubtotal())
            });
        }

        tablaItems.setModel(modelo);
    }

    private void configurarEventosTablaFacturas() {
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaFacturas.getSelectedRow();
                if (fila != -1) {
                    int idFactura = (int) modeloTabla.getValueAt(fila, 0);
                    llenarTablaConItems(idFactura);
                }
            }
        });
    }
}
