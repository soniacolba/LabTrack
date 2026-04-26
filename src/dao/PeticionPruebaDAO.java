package dao;

import java.sql.Connection;
import model.PeticionPrueba;
import db.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.EnumEstadoPeticionPrueba;
import model.Prueba;

public class PeticionPruebaDAO {

    public boolean insertar(PeticionPrueba pp) {
        try (Connection con = DB.getConnection()) {
            return insertar(con, pp);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertar(Connection con, PeticionPrueba pp) {

        String sql = "INSERT INTO peticion_prueba (estado, resultado, id_peticion, id_prueba) VALUES (?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

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

    public int insertarVarias(int idPeticion, List<Integer> idsPrueba) {
        try (Connection con = DB.getConnection()) {
            return insertarVarias(con, idPeticion, idsPrueba);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int insertarVarias(Connection con, int idPeticion, List<Integer> idsPrueba) {

        String sql = "INSERT INTO peticion_prueba (estado, resultado, id_peticion, id_prueba) VALUES (?,?,?,?)";
        int insertadas = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Integer idPrueba : idsPrueba) {
                ps.setString(1, EnumEstadoPeticionPrueba.PENDIENTE.name());
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

    public List<Prueba> listarPruebasDePeticion(int idPeticion) {
        List<Prueba> lista = new ArrayList<>();

        String sql = "SELECT p.id_prueba, p.nombre "
                + "FROM peticion_prueba pp "
                + "JOIN prueba p ON pp.id_prueba = p.id_prueba "
                + "WHERE pp.id_peticion = ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPeticion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Prueba(rs.getInt("id_prueba"), rs.getString("nombre")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public boolean borrarPorPeticion(Connection con, int idPeticion) {
        String sql = "DELETE FROM peticion_prueba WHERE id_peticion = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPeticion);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PeticionPrueba> listarPorPeticion(int idPeticion) {

        List<PeticionPrueba> lista = new ArrayList<>();

        String sql = "SELECT id, estado, resultado, id_peticion, id_prueba "
                + "FROM peticion_prueba WHERE id_peticion = ?";

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPeticion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    PeticionPrueba pp = new PeticionPrueba(
                            rs.getInt("id"),
                            EnumEstadoPeticionPrueba.valueOf(rs.getString("estado")),
                            rs.getString("resultado"),
                            rs.getInt("id_peticion"),
                            rs.getInt("id_prueba")
                    );

                    lista.add(pp);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
    
    public boolean anularPorPeticion(Connection con, int idPeticion, String motivo) {
    String sql = "UPDATE peticion_prueba "
            + "SET estado = ?, resultado = ? "
            + "WHERE id_peticion = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, EnumEstadoPeticionPrueba.ANULADA.name());
        ps.setString(2, "ANULADA - " + motivo);
        ps.setInt(3, idPeticion);

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
    
}
