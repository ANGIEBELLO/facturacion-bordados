package modelo;



import java.util.Date;

public class ItemFactura {
    private int idItem;
    private int idFactura;
    private int idEmpleado;
    private String tipo;
    private String descripcion;
    private int cantidad;
    private double valorUnitario;
    private String nombre;
    private Date fecha;
    private boolean pagado;  // Nuevo campo para saber si el ítem ya fue pagado
    private String tipoTrabajo;
    private double subtotal;
    private Date fechaPago;

    // --- Constructores ---
    public ItemFactura() {}

    public ItemFactura(String tipo, String descripcion, int cantidad, double valorUnitario, String nombre) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
        this.nombre = nombre;
    }

    // --- Getters y Setters ---

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public double getSubtotal() {
        return cantidad * valorUnitario;
    }


    public String getProducto() {
        return nombre;
    }

    public void setProducto(String producto) {
        this.nombre = producto;
    }

    public String getTipoTrabajo() {
        return tipoTrabajo;
    }

    public void setTipoTrabajo(String tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }



    // (Opcional) Override para debugging o visualización
    @Override
    public String toString() {
        return nombre + " (" + tipo + ") - $" + getSubtotal();
    }

}



