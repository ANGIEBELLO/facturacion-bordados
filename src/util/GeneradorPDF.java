package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import modelo.Factura;
import modelo.ItemFactura;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Map;

import java.util.List;

public class GeneradorPDF {
    public static void generarFacturaPDF(Factura factura) {
        String rutaSalida = "Factura_" + factura.getId() + ".pdf"; // Declaración de la ruta

        try {
            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream("Factura_" + factura.getId() + ".pdf"));
            documento.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph titulo = new Paragraph("FACTURA", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph(" ")); // Espacio
            Paragraph encabezado = new Paragraph("FACTURA N° " + factura.getId(), tituloFont);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            documento.add(encabezado);

            documento.add(new Paragraph("Cliente: " + factura.getCliente().getNombre()));
            documento.add(new Paragraph("Teléfono: " + factura.getCliente().getTelefono()));
            documento.add(new Paragraph("Fecha: " + factura.getFecha()));
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10f);
            tabla.setSpacingAfter(10f);

            tabla.addCell("Tipo");
            tabla.addCell("Descripción");
            tabla.addCell("Cantidad");
            tabla.addCell("Valor Unitario");
            tabla.addCell("Subtotal");

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

            // Totales
            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(50);
            resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.setSpacingBefore(10f);

            resumen.addCell("TOTAL");
            resumen.addCell(formatoMoneda.format(factura.getTotal()));

            resumen.addCell("ABONO");
            resumen.addCell(formatoMoneda.format(factura.getAbono()));

            resumen.addCell("SALDO");
            resumen.addCell(formatoMoneda.format(factura.getSaldo()));

            documento.add(resumen);

            // Mensaje de agradecimiento
            Paragraph agradecimiento = new Paragraph("Gracias por su compra. ¡Esperamos volver a verlo pronto!");
            agradecimiento.setAlignment(Element.ALIGN_CENTER);
            agradecimiento.setSpacingBefore(20f);
            documento.add(agradecimiento);

            documento.close();

            // Abrir PDF automáticamente
            File pdfFile = new File(rutaSalida);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    System.out.println("Desktop no soportado. No se puede abrir el PDF automáticamente.");
                }
            }
            System.out.println("Factura PDF generada con éxito.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Resumen de empleados y salarios
    public static void generarResumenEmpleadosPDF(Map<String, Map<String, Integer>> resumen, Map<String, Double> salarios) throws Exception {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Resumen de Empleados como PDF");
        fileChooser.setSelectedFile(new File("Resumen_Empleados.pdf"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivoPDF = fileChooser.getSelectedFile();
        Document documento = new Document();
        DecimalFormat df = new DecimalFormat("#,###.##");

        PdfWriter.getInstance(documento, new FileOutputStream(archivoPDF));
        documento.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("RESUMEN DE TRABAJOS Y SALARIOS DE EMPLEADOS", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        documento.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.addCell("Empleado");
        tabla.addCell("Bordados");
        tabla.addCell("Programas");
        tabla.addCell("Salario Total");

        for (String nombre : resumen.keySet()) {
            Map<String, Integer> trabajos = resumen.get(nombre);
            int bordados = trabajos.getOrDefault("bordado", 0);
            int programas = trabajos.getOrDefault("programa", 0);
            double salario = salarios.getOrDefault(nombre, 0.0);

            tabla.addCell(nombre);
            tabla.addCell(String.valueOf(bordados));
            tabla.addCell(String.valueOf(programas));
            tabla.addCell("$" + df.format(salario));
        }

        documento.add(tabla);
        documento.close();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(archivoPDF);
        } else {
            JOptionPane.showMessageDialog(null, "PDF generado en:\n" + archivoPDF.getAbsolutePath());
        }
    }

}