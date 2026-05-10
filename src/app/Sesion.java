package app;

import model.Usuario;

public class Sesion {

    private static Usuario usuarioActual;

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static int getIdUsuario() {
        return usuarioActual.getIdUsuario();
    }

    public static String getUsername() {
        return usuarioActual.getUsername();
    }

    public static String getRol() {
        return usuarioActual.getRol();
    }

    public static boolean esTecnico() {
        return "TECNICO".equals(usuarioActual.getRol());
    }

    public static boolean esFacultativo() {
        return "FACULTATIVO".equals(usuarioActual.getRol());
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}