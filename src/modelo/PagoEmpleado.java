package modelo;

import java.util.Date;
import java.util.List;

public class PagoEmpleado {
    private int idPago;
    private int idEmpleado;
    private Date fecha;
    private double totalPagado;
    private List<ItemFactura> itemsPagados;

    public PagoEmpleado() {}

    public PagoEmpleado(int idEmpleado, Date fecha, double totalPagado) {
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.totalPagado = totalPagado;
    }

    // Getters y Setters

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public List<ItemFactura> getItemsPagados() {
        return itemsPagados;
    }

    public void setItemsPagados(List<ItemFactura> itemsPagados) {
        this.itemsPagados = itemsPagados;
    }
}

