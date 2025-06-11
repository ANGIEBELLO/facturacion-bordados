package modelo;


public class CuentaContable {
    private int id;
    private String codigo; // Ej: 1.1.01
    private String nombre; // Ej: "Caja", "Clientes"
    private String tipo;   // ACTIVO, PASIVO, INGRESO, GASTO, PATRIMONIO

    public CuentaContable(int id, String codigo, String nombre, String tipo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public CuentaContable(String codigo, String nombre, String tipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
