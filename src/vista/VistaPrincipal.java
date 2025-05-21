package vista;

import modelo.Cliente;
import modelo.Factura;

import javax.swing.*;
import java.awt.*;

public class VistaPrincipal extends JFrame {

    private static VistaPrincipal instancia; // Instancia estática

    private JTabbedPane pestañas; // Para gestionar las pestañas

    public VistaPrincipal() {
        instancia = this; // Guardamos la instancia activa

        setTitle("Sistema de Gestión de Bordados");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Crear los paneles
        PanelClientes panelClientes = new PanelClientes();

        PanelFacturasListado panelFacturasListado = new PanelFacturasListado();

        // Crear pestañas
        pestañas = new JTabbedPane();

        pestañas.addTab("Clientes", panelClientes);

        pestañas.addTab("Facturas", panelFacturasListado);

        // Agregar pestañas al frame
        add(pestañas, BorderLayout.CENTER);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            vista.setVisible(true);
        });
    }
}

