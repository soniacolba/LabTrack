package app;

import dao.PacienteDAO;
import dao.PeticionDAO;
import dao.PeticionPruebaDAO;
import java.sql.Connection;
import db.DB;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import model.EnumEstadoPeticion;
import model.Paciente;
import model.Peticion;
import model.EnumPrioridad;


public class LabTrack {

    public static void main(String[] args) {
        
        try (Connection con = DB.getConnection()){
            System.out.println("Conectado");
        } catch (Exception e) {
            System.out.println("Error al conectar");
            e.printStackTrace();
        }
        
        DB.initDatabase();

        PeticionDAO peticionDAO = new PeticionDAO();
        PeticionPruebaDAO ppDAO = new PeticionPruebaDAO();

        Peticion p = new Peticion(
                0,
                LocalDateTime.now(),
                EnumPrioridad.NORMAL,
                EnumEstadoPeticion.PENDIENTE,
                "123456789", // CIP existente
                1,           // usuario seed (id 1)
                1            // tipo muestra seed (id 1: Sangre)
        );

        int idPeticion = peticionDAO.insertar(p);
        System.out.println("Petición creada: " + idPeticion);

        List<Integer> pruebas = Arrays.asList(1, 2); // Hemograma, Bioquímica
        int n = ppDAO.insertarVarias(idPeticion, pruebas);
        System.out.println("Pruebas asignadas: " + n);
     
    }
    
}
