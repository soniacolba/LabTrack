package dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import model.TipoMuestra;
import db.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TipoMuestraDAO {

    public List<TipoMuestra> listarTodos() {

        List<TipoMuestra> lista = new ArrayList<>();

        String sql = "SELECT id_tipo_muestra, nombre FROM tipo_muestra ORDER BY nombre";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id_tipo_muestra");
                String nombre = rs.getString("nombre");
                
                lista.add(new TipoMuestra(id, nombre));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }


    
}
