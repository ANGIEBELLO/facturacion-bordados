package modelo;

public class ItemFactura {
    private int id;
    private String tipo;           // "BORDADO" o "PROGRAMA"
    private String descripcion;    // Detalle del trabajo
    private int cantidad;
    private double valorUnitario;
    private String empleado;       // Empleado asignado
    private String nombre;         // Nombre del servicio o producto
    private double subtotal;
    private int idItem;
    private String producto;

    private String tipoTrabajo;
    private String empleadoAsignado;

// Getters y setters

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }


    public void setTipoTrabajo(String tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    public void setEmpleadoAsignado(String empleadoAsignado) {
        this.empleadoAsignado = empleadoAsignado;
    }

    public ItemFactura() {
    }

    public ItemFactura(String tipo, String descripcion, int cantidad, double valorUnitario, String nombre) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
        this.nombre = nombre;

    }

    // Getters y Setters


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

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // Calcula el subtotal del ítem
    public double getSubtotal() {
        return cantidad * valorUnitario;
    }
}




