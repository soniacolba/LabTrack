package dao;

import java.sql.Connection;
import model.Paciente;
import db.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public List<Paciente> buscarPorApellidos(String apellidos) {
        List<Paciente> listaPacientes = new ArrayList<>();

        String sql = "SELECT cip, nombre, apellidos, fecha_nacimiento "
                + "FROM paciente "
                + "WHERE apellidos LIKE ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + apellidos + "%");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String cip = rs.getString("cip");
                    String nombre = rs.getString("nombre");
                    String apellidosPaciente = rs.getString("apellidos");
                    LocalDate fecha = LocalDate.parse(rs.getString("fecha_nacimiento"));

                    Paciente p = new Paciente(cip, nombre, apellidosPaciente, fecha);
                    listaPacientes.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaPacientes;
    }

    public boolean actualizar(Paciente p) {
        String sql = "UPDATE paciente SET nombre = ?, apellidos = ?, fecha_nacimiento = ? WHERE cip = ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getFechaNacimiento().toString());
            ps.setString(4, p.getCip());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
