package dao;

import db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import model.EnumEstadoPeticion;
import model.Peticion;
import model.EnumPrioridad;

public class PeticionDAO {
    
    public int insertar(Peticion p){
        try (Connection con = DB.getConnection()) {
            return insertar(con, p);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean actualizar(Connection con, Peticion peticion) {
        String sql = "UPDATE peticion "
                + "SET prioridad = ?, id_tipo_muestra = ? "
                + "WHERE id_peticion = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, peticion.getPrioridad().name());
            ps.setInt(2, peticion.getIdTipoMuestra());
            ps.setInt(3, peticion.getIdPeticion());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insertar(Connection con, Peticion p) {

        String sql = "INSERT INTO peticion (fecha_registro, prioridad, estado, cip_paciente, id_usuario, id_tipo_muestra) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            
            ps.setString(1, p.getFechaRegistro().toString());
            ps.setString(2, p.getPrioridad().name());
            ps.setString(3, p.getEstado().name());
            ps.setString(4, p.getCipPaciente());
            ps.setInt(5, p.getIdUsuario());
            ps.setInt(6, p.getIdTipoMuestra());
            
            int filas = ps.executeUpdate();
            if(filas != 1) return -1;
            
            try(ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()){
                    return rs.getInt(1);
                }
            }
            
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Peticion buscarPorId(int idPeticion) {

        String sql = "SELECT id_peticion, fecha_registro, prioridad, estado, cip_paciente, id_usuario, id_tipo_muestra FROM peticion WHERE id_peticion = ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPeticion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    LocalDateTime fecha = LocalDateTime.parse(rs.getString("fecha_registro"));
                    EnumPrioridad prioridad = EnumPrioridad.valueOf(rs.getString("prioridad"));
                    EnumEstadoPeticion estado = EnumEstadoPeticion.valueOf(rs.getString("estado"));

                    String cip = rs.getString("cip_paciente");
                    int idUsuario = rs.getInt("id_usuario");
                    int idTipoMuestra = rs.getInt("id_tipo_muestra");

                    return new Peticion(
                            rs.getInt("id_peticion"),
                            fecha,
                            prioridad,
                            estado,
                            cip,
                            idUsuario,
                            idTipoMuestra
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    
    public boolean actualizarEstado(Connection con, int idPeticion, EnumEstadoPeticion estado) {
    String sql = "UPDATE peticion SET estado = ? WHERE id_peticion = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, estado.name());
        ps.setInt(2, idPeticion);
        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}