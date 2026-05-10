package dao;

import db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasDAO {

    private int contar(String sql) {

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int contarPacientes() {
        String sql = "SELECT COUNT(*) FROM paciente";
        return contar(sql);
    }

    public int contarPeticiones() {
        String sql = "SELECT COUNT(*) FROM peticion";
        return contar(sql);
    }

    public int contarPeticionesUrgentes() {
        String sql = "SELECT COUNT(*) "
                   + "FROM peticion "
                   + "WHERE prioridad = 'URGENTE'";

        return contar(sql);
    }

    public int contarPruebasRealizadas() {
        String sql = "SELECT COUNT(*) "
                   + "FROM peticion_prueba "
                   + "WHERE estado = 'REALIZADA'";

        return contar(sql);
    }

    public int contarPeticionesAnuladas() {
        String sql = "SELECT COUNT(*) "
                + "FROM peticion "
                + "WHERE estado = 'ANULADA'";

        return contar(sql);
    }

    public Object[][] obtenerPeticionesPorEstado() {

        String sql = "SELECT estado, COUNT(*) AS total "
                + "FROM peticion "
                + "GROUP BY estado";

        int pendientes = 0;
        int realizadas = 0;
        int informadasValidadas = 0;
        int anuladas = 0;

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String estado = rs.getString("estado");
                int total = rs.getInt("total");

                switch (estado) {
                    case "PENDIENTE":
                        pendientes = total;
                        break;

                    case "REALIZADA":
                        realizadas = total;
                        break;

                    case "INFORMADA":
                    case "VALIDADA":
                        informadasValidadas += total;
                        break;

                    case "ANULADA":
                        anuladas = total;
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalGeneral = pendientes + realizadas + informadasValidadas + anuladas;

        return new Object[][]{
            {"Pendiente", pendientes},
            {"Realizada", realizadas},
            {"Informada / Validada", informadasValidadas},
            {"Anulada", anuladas},
            {"Total", totalGeneral}
        };
    }

    public Object[][] obtenerTopPruebasSolicitadas() {

        String sql = "SELECT p.nombre, COUNT(*) AS total "
                + "FROM peticion_prueba pp "
                + "JOIN prueba p ON pp.id_prueba = p.id_prueba "
                + "GROUP BY p.nombre "
                + "ORDER BY total DESC "
                + "LIMIT 5";

        List<Object[]> filas = new ArrayList<>();

        try (Connection con = DB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getString("nombre"),
                    rs.getInt("total")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return filas.toArray(new Object[0][]);
    }
}
