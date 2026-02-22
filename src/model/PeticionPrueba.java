package model;

public class PeticionPrueba {
    
    private int id;
    private EstadoPeticionPrueba estado;
    private String resultado;
    private int idPeticion;
    private int idPrueba;

    public PeticionPrueba(int id, EstadoPeticionPrueba estado, String resultado, int idPeticion, int idPrueba) {
        this.id = id;
        this.estado = estado;
        this.resultado = resultado;
        this.idPeticion = idPeticion;
        this.idPrueba = idPrueba;
    }

    public int getId() {
        return id;
    }

    public EstadoPeticionPrueba getEstado() {
        return estado;
    }

    public String getResultado() {
        return resultado;
    }

    public int getIdPeticion() {
        return idPeticion;
    }

    public int getIdPrueba() {
        return idPrueba;
    }
    
    
    
}
