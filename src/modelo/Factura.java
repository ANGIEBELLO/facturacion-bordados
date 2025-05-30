package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Factura {
    private int id;
    private Date fecha;
    private double total;
    private double abono;
    private double saldo;
    private double totalFactura;
    private int idCliente;
    private String estado;         // Estado del pago (Pagada, Pendiente)
    private String estadoTrabajo;  // Estado del trabajo (En proceso, Listo, etc.)
    private List<ItemFactura> items;
    private Cliente cliente;
    private String clienteNombre;
    private String telefonoCliente;


    public Factura() {
        this.fecha = new Date();
        this.items = new ArrayList<>();
    }
    public Factura(int id, java.sql.Date fecha, double total, double abono, double saldo, int idCliente, String clienteNombre, String clienteTelefono) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.abono = abono;
        this.saldo = saldo;
        this.idCliente = idCliente;
        this.clienteNombre = clienteNombre;
        this.telefonoCliente = clienteTelefono;
        this.items = new ArrayList<>();
    }
    public Factura(int id, Date fecha, String clienteNombre, double total, double abono, double saldo, int idCliente, String estado, String estadoTrabajo) {
        this.id = id;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.total = total;
        this.abono = abono;
        this.saldo = saldo;
        this.idCliente = idCliente;
        this.estado = estado;
        this.estadoTrabajo = estadoTrabajo;
    }




    public Factura(int id, Date fecha, double total, double abono, int idCliente, String estado, String estadoTrabajo) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.abono = abono;
        this.idCliente = idCliente;
        this.estado = estado;
        this.estadoTrabajo = estadoTrabajo;
        this.items = new ArrayList<>();
    }


    public void agregarItem(ItemFactura item) {
        if (items == null) items = new ArrayList<>();
        this.items.add(item);
    }

    // Getters y Setters




    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getAbono() {
        return abono;
    }

    public void setAbono(double abono) {
        this.abono = abono;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoTrabajo() {
        return estadoTrabajo;
    }

    public void setEstadoTrabajo(String estadoTrabajo) {
        this.estadoTrabajo = estadoTrabajo;
    }

    public List<ItemFactura> getItems() {
        return items;
    }

    public void setItems(List<ItemFactura> items) {
        this.items = items;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void calcularTotales() {
        double totalCalculado = 0.0;

        for (ItemFactura item : items) {
            double subtotal = item.getCantidad() * item.getValorUnitario();
            item.setSubtotal(subtotal);
            totalCalculado += subtotal;
        }

        this.total = totalCalculado;

        // Evita valores negativos o inconsistentes
        if (this.abono < 0) {
            this.abono = 0;
        }

        this.saldo = this.total - this.abono;

        // Determinar estado de pago con formato correcto
        if (this.saldo <= 0) {
            this.estado = "Cancelada";
            this.saldo = 0; // Para evitar negativos
        } else {
            this.estado = "Pendiente";
        }
    }

    public void setTotalFactura(double totalFactura) {
        this.totalFactura = totalFactura;
    }

    public double getTotalFactura() {
        return totalFactura;
    }
}
