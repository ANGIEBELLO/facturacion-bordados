package modelo;

import java.util.List;

    public class ResumenFacturasMes {
        private List<Factura> facturas;
        private double totalFacturado;

        public List<Factura> getFacturas() {
            return facturas;
        }

        public void setFacturas(List<Factura> facturas) {
            this.facturas = facturas;
        }

        public double getTotalFacturado() {
            return totalFacturado;
        }

        public void setTotalFacturado(double totalFacturado) {
            this.totalFacturado = totalFacturado;
        }
    }

