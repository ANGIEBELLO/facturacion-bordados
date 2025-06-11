package modelo;

import java.util.Date;

public class MovimientoContable {
    private int cuentaId;            // Id de la cuenta (si usas)
    private String cuentaCodigo;     // Código de cuenta
    private String cuentaNombre;     // Nombre de la cuenta
    private String descripcion;      // Descripción del movimiento
    private String tipo;             // "DEBE" o "HABER"
    private double monto;            // Monto del movimiento
    private Date fecha;              // Fecha del movimiento
    private double debe;             // Alternativa si usas separado debe/haber
    private double haber;

    public MovimientoContable() {}

    // Constructor completo (puedes agregar más según necesites)
    public MovimientoContable(int cuentaId, String cuentaCodigo, String cuentaNombre, String descripcion, String tipo, double monto, Date fecha, double debe, double haber) {
        this.cuentaId = cuentaId;
        this.cuentaCodigo = cuentaCodigo;
        this.cuentaNombre = cuentaNombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.debe = debe;
        this.haber = haber;
    }

    // Getters y Setters

    public int getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(int cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getCuentaCodigo() {
        return cuentaCodigo;
    }

    public void setCuentaCodigo(String cuentaCodigo) {
        this.cuentaCodigo = cuentaCodigo;
    }

    public String getCuentaNombre() {
        return cuentaNombre;
    }

    public void setCuentaNombre(String cuentaNombre) {
        this.cuentaNombre = cuentaNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getDebe() {
        return debe;
    }

    public void setDebe(double debe) {
        this.debe = debe;
    }

    public double getHaber() {
        return haber;
    }

    public void setHaber(double haber) {
        this.haber = haber;
    }
}

