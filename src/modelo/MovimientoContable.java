package modelo;

import java.util.Date;

public class MovimientoContable {
    private int cuentaId;
    private String cuentaCodigo;
    private String cuentaNombre;
    private String descripcion;
    private String tipo;         // "DEBE" o "HABER"
    private double monto;
    private Date fecha;

    public MovimientoContable() {}

    public MovimientoContable(int cuentaId, String cuentaCodigo, String cuentaNombre,
                              String descripcion, String tipo, double monto, Date fecha) {
        this.cuentaId = cuentaId;
        this.cuentaCodigo = cuentaCodigo;
        this.cuentaNombre = cuentaNombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
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

    private String tipoCuenta;

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }


}
