package vista;

import controlador.FacturaController;
import modelo.Cliente;
import modelo.Factura;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class VentanaListaFacturasCliente extends JFrame {
    private Cliente cliente;
    private FacturaController facturaController;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;

    public VentanaListaFacturasCliente(Cliente cliente) {
        this.cliente = cliente;
        this.facturaController = new FacturaController();
        setTitle("Facturas de " + cliente.getNombre());
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarFacturas();
    }

    private void initComponents() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.setColumnIdentifiers(new Object[]{"ID", "Fecha", "Total", "Abono", "Saldo", "Estado"});

        tablaFacturas = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnAbrirFactura = new JButton("Abrir Factura");
        btnAbrirFactura.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaFacturas.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    int idFactura = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

                    // Buscar la factura completa por ID
                    Factura factura = facturaController.obtenerFacturaPorId(idFactura);

                    if (factura != null) {
                        JFrame ventana = new JFrame("Factura #" + factura.getId());
                        ventana.setContentPane(new PanelFacturas(cliente, factura));
                        ventana.pack();
                        ventana.setLocationRelativeTo(null);
                        ventana.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "No se pudo cargar la factura.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Selecciona una factura para abrir.");
                }
            }
        });

        JPanel panelBoton = new JPanel();
        panelBoton.add(btnAbrirFactura);
        add(panelBoton, BorderLayout.SOUTH);
    }

    private void cargarFacturas() {
        modeloTabla.setRowCount(0);
        List<Factura> facturas = facturaController.obtenerFacturasPorCliente(cliente.getId());
        NumberFormat formatoPesos = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        for (Factura f : facturas) {
            double saldo = f.getTotal() - f.getAbono();
            modeloTabla.addRow(new Object[]{
                    f.getId(),
                    f.getFecha(),
                    formatoPesos.format(f.getTotal()),
                    formatoPesos.format(f.getAbono()),
                    formatoPesos.format(saldo),
                    f.getEstado()
            });
        }
    }
}

