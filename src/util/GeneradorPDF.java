package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import modelo.Factura;
import modelo.ItemFactura;

import javax.swing.*;
import java.awt.Desktop;
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
            logo.scaleToFit(120, 120);
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
}
