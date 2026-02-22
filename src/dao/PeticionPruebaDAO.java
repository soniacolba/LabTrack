package dao;

import java.sql.Connection;
import model.PeticionPrueba;
import db.DB;
import java.sql.PreparedStatement;
import java.util.List;
import model.EstadoPeticionPrueba;

public class PeticionPruebaDAO {
    
    public boolean insertar(PeticionPrueba pp){
        
        String sql = "INSERT INTO peticion_prueba (estado, resultado, id_peticion, id_prueba) VALUES (?,?,?,?)";
        
        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, pp.getEstado().name());
            ps.setString(2, pp.getResultado());
            ps.setInt(3, pp.getIdPeticion());
            ps.setInt(4, pp.getIdPrueba());
            
            return ps.executeUpdate() == 1;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
    }
    
    public int insertarVarias(int idPeticion, List<Integer> idsPrueba){
        
        String sql = "INSERT INTO peticion_prueba (estado, resultado, id_peticion, id_prueba) VALUES (?,?,?,?)";
        int insertadas = 0;
        
        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            for(Integer idPrueba : idsPrueba){
                ps.setString(1, EstadoPeticionPrueba.PENDIENTE.name());
                ps.setString(2, null);
                ps.setInt(3, idPeticion);
                ps.setInt(4, idPrueba);
                insertadas += ps.executeUpdate();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insertadas;
    }
    
}
