package vista;

import controlador.EstadoResultadosController;

import javax.swing.*;
import java.awt.*;

public class PanelEstadoResultados extends JPanel {
    private JLabel lblIngresos, lblGastos, lblUtilidad;
    private EstadoResultadosController controller;

    public PanelEstadoResultados() {
        controller = new EstadoResultadosController();
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Estado de Resultados"));

        add(new JLabel("Ingresos totales:"));
        lblIngresos = new JLabel();
        add(lblIngresos);

        add(new JLabel("Gastos totales:"));
        lblGastos = new JLabel();
        add(lblGastos);

        add(new JLabel("Utilidad / Pérdida:"));
        lblUtilidad = new JLabel();
        add(lblUtilidad);

        cargarDatos();
    }

    private void cargarDatos() {
        double ingresos = controller.calcularIngresosTotales();
        double gastos = controller.calcularGastosTotales();
        double utilidad = ingresos - gastos;

        lblIngresos.setText(String.format("$ %, .2f", ingresos));
        lblGastos.setText(String.format("$ %, .2f", gastos));
        lblUtilidad.setText(String.format("$ %, .2f", utilidad));
    }
}

