package model;

import java.time.LocalDate;

public class Paciente {
    
    private String cip;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;

    public Paciente(String cip, String nombre, String apellidos, LocalDate fechaNacimiento) {
        this.cip = cip;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getCip() {
        return cip;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public String toString() {
        return "Paciente{" + nombre + " " + apellidos + " (" + cip + ") - " + fechaNacimiento + '}';
    }
    
    
    
}
