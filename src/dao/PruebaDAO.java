package dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import model.Prueba;
import db.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PruebaDAO {
    
    public List<Prueba> listarPorTipoMuestra(int idTipoMuestra){
        
        List<Prueba> lista = new ArrayList<>();
        
        String sql = "SELECT p.id_prueba, p.nombre FROM prueba p " + 
                "JOIN tipo_muestra_prueba tmp ON tmp.id_prueba = p.id_prueba " + 
                "WHERE tmp.id_tipo_muestra = ? " + 
                "ORDER BY p.nombre";
        
        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setInt(1, idTipoMuestra);
            
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    int id = rs.getInt("id_prueba");
                    String nombre = rs.getString("nombre");
                    lista.add(new Prueba(id, nombre));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lista;
    }
    
    public Prueba buscarPorId(int id) {

        String sql = "SELECT id_prueba, nombre FROM prueba WHERE id_prueba = ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Prueba(
                            rs.getInt("id_prueba"),
                            rs.getString("nombre")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
