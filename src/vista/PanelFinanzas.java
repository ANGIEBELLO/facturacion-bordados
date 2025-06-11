package vista;

import controlador.FacturaController;
import modelo.Factura;
import modelo.ResumenFacturasMes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;


    public class PanelFinanzas extends JPanel {

        private FacturaController facturaController;
        private DefaultTableModel modeloTabla;
        private JTable tablaFinanzas;
        private JLabel lblTotalMes;
        private JLabel lblTotalAbonos;
        private JLabel lblGanancia;

        public PanelFinanzas() {
            setLayout(new BorderLayout());
            facturaController = new FacturaController();

            modeloTabla = new DefaultTableModel(new Object[]{"ID", "Cliente", "Fecha", "Total", "Abono"}, 0);
            tablaFinanzas = new JTable(modeloTabla);
            JScrollPane scrollPane = new JScrollPane(tablaFinanzas);

            String[] meses = {
                    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            };
            JComboBox<String> comboMes = new JComboBox<>(meses);

            int anioActual = Year.now().getValue();
            Vector<String> anios = new Vector<>();
            for (int i = anioActual; i >= anioActual - 10; i--) {
                anios.add(String.valueOf(i));
            }
            JComboBox<String> comboAnio = new JComboBox<>(anios);

            lblTotalMes = new JLabel("Total del Mes: $0");
            lblTotalAbonos = new JLabel("Total Abonos: $0");
            lblGanancia = new JLabel("Ganancia Neta: $0");

            JPanel panelFiltros = new JPanel();
            panelFiltros.add(new JLabel("Mes:"));
            panelFiltros.add(comboMes);
            panelFiltros.add(new JLabel("Año:"));
            panelFiltros.add(comboAnio);
            add(panelFiltros, BorderLayout.NORTH);

            JPanel panelResumen = new JPanel(new GridLayout(1, 3));
            panelResumen.add(lblTotalMes);
            panelResumen.add(lblTotalAbonos);
            panelResumen.add(lblGanancia);

            JButton btnFiltrar = new JButton("Filtrar");
            panelFiltros.add(btnFiltrar);

            btnFiltrar.addActionListener(e -> {
                int mes = comboMes.getSelectedIndex() + 1; // Enero es 0 → mes 1
                int anio = Integer.parseInt((String) comboAnio.getSelectedItem());

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, mes - 1);
                cal.set(Calendar.YEAR, anio);

                ResumenFacturasMes resumen = facturaController.obtenerFacturasDelMes(new java.sql.Date(cal.getTimeInMillis()));
                mostrarFacturasEnTabla(resumen);
            });

            add(scrollPane, BorderLayout.CENTER);
            add(panelResumen, BorderLayout.SOUTH);

            cargarDatos();
        }

        private void mostrarFacturasEnTabla(ResumenFacturasMes resumen) {
            modeloTabla.setRowCount(0);
            double totalAbonos = 0;

            for (Factura f : resumen.getFacturas()) {
                modeloTabla.addRow(new Object[]{
                        f.getId(),
                        f.getClienteNombre() != null ? f.getClienteNombre() : "Cliente #" + f.getIdCliente(),
                        new SimpleDateFormat("dd/MM/yyyy").format(f.getFecha()),
                        String.format("$%,.0f", f.getTotalFactura()),
                        String.format("$%,.0f", f.getAbono())
                });

                totalAbonos += f.getAbono();
            }

            double totalMes = resumen.getTotalFacturado();
            double ganancia = totalMes - totalAbonos;

            lblTotalMes.setText("Total del Mes: " + String.format("$%,.0f", totalMes));
            lblTotalAbonos.setText("Total Abonos: " + String.format("$%,.0f", totalAbonos));
            lblGanancia.setText("Total Saldos Pendientes: " + String.format("$%,.0f", ganancia));
        }

        private void cargarDatos() {
            ResumenFacturasMes resumen = facturaController.obtenerFacturasDelMes(new java.sql.Date(System.currentTimeMillis()));
            mostrarFacturasEnTabla(resumen);
        }
    }
