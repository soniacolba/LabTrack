package dao;

import java.sql.Connection;
import model.Paciente;
import db.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class PacienteDAO {
    
    public boolean insertar (Paciente p){
        String sql = "INSERT INTO paciente (cip, nombre, apellidos, fecha_nacimiento) VALUES (?, ?, ?, ?)";
        
        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, p.getCip());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getApellidos());
            ps.setString(4, p.getFechaNacimiento().toString());
            
            return ps.executeUpdate() == 1;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Paciente buscarPorCip(String cip){
        String sql = "SELECT cip, nombre, apellidos, fecha_nacimiento FROM paciente WHERE cip = ?";
        
        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, cip);
            
            try (ResultSet rs = ps.executeQuery()){
                
                if(rs.next()){
                    String nombre = rs.getString("nombre");
                    String apellidos = rs.getString("apellidos");
                    LocalDate fecha = LocalDate.parse(rs.getString("fecha_nacimiento"));
                    
                    return new Paciente(cip, nombre, apellidos, fecha);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
