package controlador;

public class EstadoResultadosController {

    private TransaccionDAO transaccionDAO; // o usa tu DAO existente

    public EstadoResultadosController() {
        transaccionDAO = new TransaccionDAO();
    }

    public double calcularIngresosTotales() {
        return transaccionDAO.obtenerTotalPorTipoCuenta("INGRESO");
    }

    public double calcularGastosTotales() {
        return transaccionDAO.obtenerTotalPorTipoCuenta("GASTO");
    }

    public double calcularUtilidad() {
        return calcularIngresosTotales() - calcularGastosTotales();
    }


}
