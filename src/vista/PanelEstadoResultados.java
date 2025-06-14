package vista;

import com.toedter.calendar.JDateChooser;
import controlador.EstadoResultadosController;
import modelo.MovimientoContable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.geom.Point2D; // si reemplazaras la lógica de anclaje tú mismo (no necesario si importas bien)
import org.jfree.chart.ui.TextAnchor;
import util.GeneradorPDF;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PanelEstadoResultados extends JPanel {

    private JDateChooser dateDesde, dateHasta;
    private JLabel lblVentas, lblGastos, lblResultado;
    private JTable tabla;
    private DefaultTableModel modelo;
    private EstadoResultadosController controller;
    private JTable tablaResumen;
    private DefaultTableModel modeloResumen;
    private JPanel panelGrafico;

    public PanelEstadoResultados() {
        controller = new EstadoResultadosController();
        setLayout(new BorderLayout());

        // Panel superior de filtros
        JPanel filtrosPanel = new JPanel();
        filtrosPanel.setBorder(BorderFactory.createTitledBorder("Periodo"));
        filtrosPanel.setLayout(new FlowLayout());

        dateDesde = new JDateChooser();
        dateHasta = new JDateChooser();

        dateDesde.setPreferredSize(new Dimension(150, 25));
        dateHasta.setPreferredSize(new Dimension(150, 25));

        filtrosPanel.add(new JLabel("Desde:"));
        filtrosPanel.add(dateDesde);
        filtrosPanel.add(new JLabel("Hasta:"));
        filtrosPanel.add(dateHasta);

        JButton btnBuscar = new JButton("Buscar");
        filtrosPanel.add(btnBuscar);

        add(filtrosPanel, BorderLayout.NORTH);

        // Panel resumen financiero (izquierda)
        JPanel resumenPanel = new JPanel(new GridLayout(3, 1));
        resumenPanel.setBorder(BorderFactory.createTitledBorder("Resumen Financiero"));

        lblVentas = new JLabel("Ventas: $0", JLabel.CENTER);
        lblVentas.setForeground(Color.BLUE);
        lblVentas.setFont(new Font("Arial", Font.BOLD, 16));

        lblGastos = new JLabel("Gastos: $0", JLabel.CENTER);
        lblGastos.setForeground(Color.RED);
        lblGastos.setFont(new Font("Arial", Font.BOLD, 16));

        lblResultado = new JLabel("Resultado Neto: $0", JLabel.CENTER);
        lblResultado.setFont(new Font("Arial", Font.BOLD, 18));

        resumenPanel.add(lblVentas);
        resumenPanel.add(lblGastos);
        resumenPanel.add(lblResultado);

        add(resumenPanel, BorderLayout.WEST);

        // Panel de tabla de detalle de transacciones (centro)
        modelo = new DefaultTableModel(new Object[]{"Fecha", "Descripción", "Tipo", "Monto", "Cuenta"}, 0);
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBorder(BorderFactory.createTitledBorder("Detalle de Transacciones"));
        tablaPanel.add(scroll, BorderLayout.CENTER);

        add(tablaPanel, BorderLayout.CENTER);

        // Panel gráfico
        panelGrafico = new JPanel(new BorderLayout());
        panelGrafico.setBorder(BorderFactory.createTitledBorder("Visualización gráfica"));

        // Tabla resumen por cuenta
        modeloResumen = new DefaultTableModel(new Object[]{"Tipo de Cuenta", "Cuenta", "Total"}, 0);
        tablaResumen = new JTable(modeloResumen);
        JScrollPane resumenScroll = new JScrollPane(tablaResumen);

        JPanel detalleResumenPanel = new JPanel(new BorderLayout());
        detalleResumenPanel.setBorder(BorderFactory.createTitledBorder("Resumen por Cuenta"));
        detalleResumenPanel.add(resumenScroll, BorderLayout.CENTER);

// Panel inferior con diseño horizontal (lado a lado)
        JPanel panelInferior = new JPanel(new GridLayout(1, 2));
        panelInferior.add(panelGrafico);
        panelInferior.add(detalleResumenPanel);

        add(panelInferior, BorderLayout.SOUTH);


        // Acción del botón buscar
        btnBuscar.addActionListener(e -> cargarMovimientos());
        JButton btnExportarPDF = new JButton("Exportar PDF");

        btnExportarPDF.addActionListener(e -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date desde = dateDesde.getDate();
                Date hasta = dateHasta.getDate();

                // Extraer texto de los JLabel y convertir a números
                String textoVentas = lblVentas.getText().replaceAll("[^\\d]", "");
                String textoGastos = lblGastos.getText().replaceAll("[^\\d]", "");
                String textoResultado = lblResultado.getText().replaceAll("[^\\d-]", "");

                double ventas = Double.parseDouble(textoVentas);
                double gastos = Double.parseDouble(textoGastos);
                double resultado = Double.parseDouble(textoResultado);

                // Obtener el modelo de la tabla resumen
                TableModel resumen = tablaResumen.getModel();

                // Llamar al generador PDF
                GeneradorPDF generador = new GeneradorPDF();
                generador.exportarEstadoResultados(desde, hasta, ventas, gastos, resultado, resumen);

                JOptionPane.showMessageDialog(this, "PDF generado exitosamente.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage());
            }
        });




        filtrosPanel.add(btnExportarPDF);


        // Estilos tabla de transacciones
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Alineación derecha para columna "Monto"
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Estilos tabla resumen por cuenta
        tablaResumen.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tablaResumen.setRowHeight(25);
        tablaResumen.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tablaResumen.setFillsViewportHeight(true);
        tablaResumen.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaResumen.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
    }


    private void cargarMovimientos() {
        Date desde = dateDesde.getDate();
        Date hasta = dateHasta.getDate();

        if (desde == null || hasta == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un rango de fechas.");
            return;
        }

        modelo.setRowCount(0);
        modeloResumen.setRowCount(0); // Limpiar tabla resumen

        List<MovimientoContable> lista = controller.obtenerMovimientosPorFecha(desde, hasta);

        double ventas = 0;
        double gastos = 0;

        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

        for (MovimientoContable mov : lista) {
            String tipoCuenta = mov.getTipoCuenta();
            String tipoMovimiento = mov.getTipo().toUpperCase();
            double monto = mov.getMonto();

            // Sumar ingresos y gastos
            if (tipoCuenta.equalsIgnoreCase("INGRESO") && tipoMovimiento.equals("HABER")) {
                ventas += monto;
            } else if (tipoCuenta.equalsIgnoreCase("GASTO") && tipoMovimiento.equals("DEBE")) {
                gastos += monto;
            }

            // Agregar fila a la tabla de transacciones
            modelo.addRow(new Object[]{
                    formatoFecha.format(mov.getFecha()),
                    mov.getDescripcion(),
                    mov.getTipo(),
                    String.format("%,.0f", mov.getMonto()),
                    mov.getCuentaNombre()
            });
        }

        // Mostrar resumen financiero
        double resultado = ventas - gastos;

        lblVentas.setText("Ventas: $" + String.format("%,.0f", ventas));
        lblGastos.setText("Gastos: $" + String.format("%,.0f", gastos));
        if (resultado >= 0) {
            lblResultado.setText("Utilidad Neta: $" + String.format("%,.0f", resultado));
            lblResultado.setForeground(Color.GREEN.darker());
        } else {
            lblResultado.setText("Pérdida Neta: $" + String.format("%,.0f", Math.abs(resultado)));
            lblResultado.setForeground(Color.RED.darker());
        }
        mostrarGraficoBarras(ventas, gastos);


        // Cargar resumen por cuenta
        try {
            Map<String, Map<String, Double>> resumen = controller.obtenerResumenPorCuenta(desde, hasta);

            for (String tipo : resumen.keySet()) {
                for (Map.Entry<String, Double> cuenta : resumen.get(tipo).entrySet()) {
                    modeloResumen.addRow(new Object[]{
                            tipo,
                            cuenta.getKey(),
                            String.format("$%,.0f", cuenta.getValue())
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener resumen por cuenta: " + ex.getMessage());
        }
    }

    private void mostrarGraficoBarras(double ventas, double gastos) {
        double utilidadNeta = ventas - gastos;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(ventas, "Ventas", "Ventas");
        dataset.addValue(gastos, "Gastos", "Gastos");
        dataset.addValue(utilidadNeta, "Utilidad Neta", "Utilidad Neta");

        JFreeChart chart = ChartFactory.createBarChart(
                "Estado de Resultados",
                "Categoría",
                "Valor ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);

        // Alineación de las etiquetas X de forma horizontal y centrada
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0)); // 0° = horizontal
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.BOLD, 12));
        domainAxis.setCategoryMargin(0.3);  // Espacio entre categorías

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(102, 178, 255));  // Azul claro
        renderer.setSeriesPaint(1, new Color(255, 102, 102));  // Rojo suave
        renderer.setSeriesPaint(2, new Color(144, 238, 144));  // Verde claro

        renderer.setMaximumBarWidth(200);
        renderer.setItemMargin(0.0); // Sin espacio entre barras en la misma categoría

        // Mostrar etiquetas de valor encima de las barras
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 12));
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(480, 280));

        panelGrafico.removeAll();
        panelGrafico.add(chartPanel, BorderLayout.CENTER);
        panelGrafico.revalidate();
        panelGrafico.repaint();
    }






}