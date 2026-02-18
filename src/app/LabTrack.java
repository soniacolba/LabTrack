package app;

import dao.PacienteDAO;
import java.sql.Connection;
import db.DB;
import java.time.LocalDate;
import model.Paciente;


public class LabTrack {

    public static void main(String[] args) {
        
        try (Connection con = DB.getConnection()){
            System.out.println("Conectado");
        } catch (Exception e) {
            System.out.println("Error al conectar");
            e.printStackTrace();
        }
        
        DB.initDatabase();
        
        PacienteDAO dao = new PacienteDAO();
        Paciente p = new Paciente("12345678", "Sonia", "Colchon Barrio", LocalDate.of(1989, 2, 1));
        
        boolean ok = dao.insertar(p);
        System.out.println("Paciente insertado: " + ok);
        
        Paciente encontrado = dao.buscarPorCip("12345678");
        System.out.println("Encontrado: " + encontrado);
        
    }
    
}
