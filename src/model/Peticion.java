package model;

import java.time.LocalDateTime;


public class Peticion {
    
    private int idPeticion;
    private LocalDateTime fechaRegistro;
    private EnumPrioridad prioridad;
    private EnumEstadoPeticion estado;
    
    private String cipPaciente;
    private int idUsuario;
    private int idTipoMuestra;

    public Peticion(int idPeticion, LocalDateTime fechaRegistro, EnumPrioridad prioridad, EnumEstadoPeticion estado, String cipPaciente, int idUsuario, int idTipoMuestra) {
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

    public EnumPrioridad getPrioridad() {
        return prioridad;
    }

    public EnumEstadoPeticion getEstado() {
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
