package model;

import java.time.LocalDateTime;


public class Peticion {
    
    private int idPeticion;
    private LocalDateTime fechaRegistro;
    private Prioridad prioridad;
    private EstadoPeticion estado;
    
    private String cipPaciente;
    private int idUsuario;
    private int idTipoMuestra;

    public Peticion(int idPeticion, LocalDateTime fechaRegistro, Prioridad prioridad, EstadoPeticion estado, String cipPaciente, int idUsuario, int idTipoMuestra) {
        this.idPeticion = idPeticion;
        this.fechaRegistro = fechaRegistro;
        this.prioridad = prioridad;
        this.estado = estado;
        this.cipPaciente = cipPaciente;
        this.idUsuario = idUsuario;
        this.idTipoMuestra = idTipoMuestra;
    }

    public int getIdPeticion() {
        return idPeticion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public EstadoPeticion getEstado() {
        return estado;
    }

    public String getCipPaciente() {
        return cipPaciente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getIdTipoMuestra() {
        return idTipoMuestra;
    }
    
    
    
}
