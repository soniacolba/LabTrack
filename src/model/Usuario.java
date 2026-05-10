package model;

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String username;
    private String password;
    private String rol;

    public Usuario(int idUsuario,String nombre, String username, String password, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
    
    public String getNombre(){
        return nombre;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }
}