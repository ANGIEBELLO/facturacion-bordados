package modelo;

import java.util.Date;
import java.util.List;


public class TransaccionContable {
    private int id;
    private Date fecha;
    private String descripcion;
    private List<MovimientoContable> movimientos;

    // Constructor, getters y setters
    public TransaccionContable() {
        // Constructor vacío necesario para poder usar `new TransaccionContable()`
    }



    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getDescripcion(){
        return  descripcion;
    }

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
    }


    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<MovimientoContable> getMovimientos(){
        return movimientos;
    }

    public void setMovimientos(List<MovimientoContable> movimientos){
        this.movimientos = movimientos;
    }
}
