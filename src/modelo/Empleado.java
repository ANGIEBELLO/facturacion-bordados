package modelo;

public class Empleado {
    private int id;
    private String nombre;
    private String cedula;
    private String telefono;
    private String cargo;

    public Empleado() {}

    public Empleado(int id, String nombre, String cedula, String telefono, String cargo) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.cargo = cargo;
    }

    public Empleado(String nombre, String cedula, String telefono, String cargo) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.cargo = cargo;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
}

