package util;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import modelo.Factura;
import modelo.ItemFactura;
import modelo.MovimientoContable;
import modelo.TransaccionContable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;

public class GeneradorPDF {

    public static void generarFacturaPDF(Factura factura) {
        String rutaSalida = "Factura_" + factura.getId() + ".pdf";

        try {
            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream(rutaSalida));
            documento.open();

            // LOGO + NOMBRE EMPRESA
            PdfPTable encabezadoTabla = new PdfPTable(2);
            encabezadoTabla.setWidthPercentage(100);
            encabezadoTabla.setWidths(new float[]{1, 3});

            // Cargar imagen
            Image logo = Image.getInstance("src/recursos/ECLAT1ok.PNG"); // Asegúrate de que esté en la ruta correcta
            logo.scaleToFit(110, 110);
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            encabezadoTabla.addCell(logoCell);

            // Nombre empresa
            Font fontEmpresa = new Font(Font.FontFamily.HELVETICA, 30, Font.BOLD);
            PdfPCell nombreEmpresa = new PdfPCell(new Phrase("Bordados Éclat", fontEmpresa));
            nombreEmpresa.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nombreEmpresa.setHorizontalAlignment(Element.ALIGN_LEFT);
            nombreEmpresa.setBorder(Rectangle.NO_BORDER);
            encabezadoTabla.addCell(nombreEmpresa);

            documento.add(encabezadoTabla);

            documento.add(new Paragraph(" ")); // Espacio
            LineSeparator separator = new LineSeparator();
            documento.add(new Chunk(separator));
            documento.add(new Paragraph(" "));

            // TÍTULO DE FACTURA
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph encabezado = new Paragraph("FACTURA N° " + factura.getId(), tituloFont);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            documento.add(encabezado);

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("Cliente: " + factura.getCliente().getNombre()));
            documento.add(new Paragraph("Teléfono: " + factura.getCliente().getTelefono()));
            documento.add(new Paragraph("Fecha: " + factura.getFecha()));
            documento.add(new Paragraph(" "));

            // TABLA DE ITEMS
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10f);
            tabla.setWidths(new float[]{2, 4, 2, 2, 2});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Tipo", "Descripción", "Cantidad", "Valor Unitario", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }

            DecimalFormat formatoMoneda = new DecimalFormat("$ #,##0.00");

            List<ItemFactura> items = factura.getItems();
            for (ItemFactura item : items) {
                tabla.addCell(item.getTipo());
                tabla.addCell(item.getDescripcion());
                tabla.addCell(String.valueOf(item.getCantidad()));
                tabla.addCell(formatoMoneda.format(item.getValorUnitario()));
                tabla.addCell(formatoMoneda.format(item.getSubtotal()));
            }

            documento.add(tabla);

            // TOTALES
            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(40);
            resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.setSpacingBefore(10f);
            resumen.setWidths(new float[]{2, 2});

            resumen.addCell(getCeldaNegrita("TOTAL:"));
            resumen.addCell(getCeldaValor(formatoMoneda.format(factura.getTotal())));

            resumen.addCell(getCeldaNegrita("ABONO:"));
            resumen.addCell(getCeldaValor(formatoMoneda.format(factura.getAbono())));

            resumen.addCell(getCeldaNegrita("SALDO:"));
            resumen.addCell(getCeldaValor(formatoMoneda.format(factura.getSaldo())));

            documento.add(resumen);

            // AGRADECIMIENTO
            Paragraph agradecimiento = new Paragraph("Gracias por su compra. ¡Esperamos volver a verlo pronto!");
            agradecimiento.setAlignment(Element.ALIGN_CENTER);
            agradecimiento.setSpacingBefore(20f);
            documento.add(agradecimiento);

            documento.close();

            // ABRIR PDF
            File pdfFile = new File(rutaSalida);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage());
        }
    }


    // CELDAS DE TOTALES ESTÉTICAS
    private static PdfPCell getCeldaNegrita(String texto) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private static PdfPCell getCeldaValor(String texto) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }


    public void exportarEstadoResultados(Date desde, Date hasta, double ventas, double gastos, double resultado, TableModel resumen) throws Exception {
        // Crear documento y definir salida
        Document documento = new Document(PageSize.A4);
        String nombreArchivo = "EstadoResultados_" + System.currentTimeMillis() + ".pdf";
        PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));


        // Ruta del logo
        String logoPath = "src/recursos/ECLAT1ok.PNG"; // Asegúrate que exista
        Image logo = Image.getInstance(logoPath);
        logo.scaleAbsolute(110, 110); // Tamaño del logo

// Crear tabla de 2 columnas: imagen + texto
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 4}); // proporción imagen:texto

// Celda de la imagen
        PdfPCell imgCell = new PdfPCell(logo);
        imgCell.setBorder(Rectangle.NO_BORDER);
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imgCell);

// Celda del texto
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 25, Font.BOLD);
        Paragraph titulo1 = new Paragraph("Bordados Éclat", tituloFont);
        titulo1.setAlignment(Element.ALIGN_LEFT);

        PdfPCell textoCell = new PdfPCell();
        textoCell.addElement(titulo1);
        textoCell.setBorder(Rectangle.NO_BORDER);
        textoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textoCell);

// Añadir al documento
        documento.open();
        documento.add(headerTable);


        // ✅ Solo línea horizontal inferior
        LineSeparator lineaInferior = new LineSeparator();
        lineaInferior.setLineColor(BaseColor.BLACK);
        lineaInferior.setLineWidth(1);
        documento.add(new Chunk(lineaInferior));
        documento.add(Chunk.NEWLINE); // espacio debajo de la línea si se quiere


        documento.add(Chunk.NEWLINE); // Espacio después del encabezado



        // Fuentes
        tituloFont.setColor(BaseColor.BLACK); // por ejemplo

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        // Título
        Paragraph titulo = new Paragraph("Estado de Resultados", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        documento.add(new Paragraph(" "));

        // Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        documento.add(new Paragraph("Periodo: " + sdf.format(desde) + " al " + sdf.format(hasta), normalFont));
        documento.add(new Paragraph(" "));

        // Resumen financiero
        documento.add(new Paragraph("Resumen Financiero:", boldFont));
        documento.add(new Paragraph("Ventas: $" + String.format("%,.0f", ventas), normalFont));
        documento.add(new Paragraph("Gastos: $" + String.format("%,.0f", gastos), normalFont));
        documento.add(new Paragraph("Resultado Neto: $" + String.format("%,.0f", resultado), normalFont));
        documento.add(new Paragraph(" "));

        // Tabla de resumen por cuenta
        documento.add(new Paragraph("Detalle por Cuenta:", boldFont));
        PdfPTable tabla = new PdfPTable(resumen.getColumnCount());
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10f);
        tabla.setSpacingAfter(10f);

        // Encabezados
        for (int i = 0; i < resumen.getColumnCount(); i++) {
            PdfPCell celda = new PdfPCell(new Phrase(resumen.getColumnName(i), boldFont));
            celda.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celda);
        }

        // Filas
        for (int fila = 0; fila < resumen.getRowCount(); fila++) {
            for (int col = 0; col < resumen.getColumnCount(); col++) {
                Object valor = resumen.getValueAt(fila, col);
                PdfPCell celda = new PdfPCell(new Phrase(valor != null ? valor.toString() : "", normalFont));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(celda);
            }
        }

        documento.add(tabla);
        agregarGraficoEstadoResultados(documento, ventas, gastos);

        documento.close();




        // Abrir el PDF al terminar (opcional)
        java.awt.Desktop.getDesktop().open(new java.io.File(nombreArchivo));
    }

    public void agregarGraficoEstadoResultados(Document document, double ventas, double gastos) throws Exception {
        double utilidadNeta = ventas - gastos;

        // Crear dataset y gráfico
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

        // Personalización
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(102, 178, 255));  // Azul
        renderer.setSeriesPaint(1, new Color(255, 102, 102));  // Rojo
        renderer.setSeriesPaint(2, new Color(144, 238, 144));  // Verde

        renderer.setMaximumBarWidth(0.25);
        renderer.setItemMargin(0.0);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12)); // 👈 usa la clase completa
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));

        // Crear imagen del gráfico
        BufferedImage chartImage = chart.createBufferedImage(500, 320);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(chartImage, "png", baos);
        Image chartItextImage = Image.getInstance(baos.toByteArray());

        // Agregar título y gráfico al PDF
        document.add(new Paragraph("Gráfico Estado de Resultados", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD)));
        document.add(Chunk.NEWLINE);
        document.add(chartItextImage);
    }




}

