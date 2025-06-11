package modelo;

public class ParametroContable {
    private int cuentaDebeId;
    private int cuentaHaberId;

    public ParametroContable(int cuentaDebeId,int cuentaHaberId){
        this.cuentaDebeId = cuentaDebeId;
        this.cuentaHaberId = cuentaHaberId;
    }

    public ParametroContable() {

    }
    // Getters y Setters

    public int getCuentaDebeId(){
        return cuentaDebeId;
    }
    public void setCuentaDebeId(int cuentaDebeId){
        this.cuentaDebeId =cuentaDebeId;
    }

    public int getCuentaHaberId(){
        return  cuentaHaberId;
    }
    public void  setCuentaHaberId(int cuentaHaberId){
        this.cuentaHaberId = cuentaHaberId;
    }



}

