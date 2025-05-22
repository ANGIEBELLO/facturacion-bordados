package vista;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class VistaPrincipal extends JFrame {

    public VistaPrincipal() {
        setTitle("Sistema de Gestión de Bordados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        try {
            // Aplicar apariencia moderna
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar FlatLaf: " + e.getMessage());
        }

        // Layout principal
        getContentPane().setLayout(new BorderLayout());

        // Panel superior con logotipo y título
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(new Color(255, 248, 232));
        panelEncabezado.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Cargar logotipo
        ImageIcon icono = null;
        try {
            URL imagenURL = getClass().getResource("/recursos/ECLAT1ok.PNG");
            if (imagenURL != null) {
                Image imagenOriginal = new ImageIcon(imagenURL).getImage();
                Image imagenRedimensionada = imagenOriginal.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                icono = new ImageIcon(imagenRedimensionada);
            } else {
                System.out.println("No se encontró el recurso de imagen.");
            }
        } catch (Exception e) {
            System.out.println("Error cargando el logotipo: " + e.getMessage());
        }

        JLabel labelLogo = new JLabel(icono);

        // Título estilizado
        JLabel labelTexto = new JLabel("Bordados Éclat");
        labelTexto.setFont(new Font("Serif", Font.BOLD, 33));


        JPanel panelTextoLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTextoLogo.setBackground(new Color(255, 248, 232));
        panelTextoLogo.add(labelLogo);
        panelTextoLogo.add(labelTexto);

        panelEncabezado.add(panelTextoLogo, BorderLayout.WEST);

        // Agregar panel encabezado
        getContentPane().add(panelEncabezado, BorderLayout.NORTH);

        // Pestañas de la aplicación
        JTabbedPane pestanas = new JTabbedPane();

        // Panel Clientes
        JPanel panelClientes = new PanelClientes(); // Asegúrate que esta clase exista
        pestanas.addTab("Clientes", panelClientes);

        // Panel Facturas
        JPanel panelFacturas = new PanelFacturasListado(); // Asegúrate que esta clase exista
        pestanas.addTab("Facturas", panelFacturas);

        getContentPane().add(pestanas, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VistaPrincipal().setVisible(true);
        });
    }
}
