package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL = "jdbc:sqlite:data/labtrack.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        String sqlPaciente
                = "CREATE TABLE IF NOT EXISTS paciente ("
                + "  cip TEXT PRIMARY KEY,"
                + "  nombre TEXT NOT NULL,"
                + "  apellidos TEXT NOT NULL,"
                + "  fecha_nacimiento TEXT NOT NULL"
                + ");";

        try (Connection con = getConnection();
                java.sql.Statement stmt = con.createStatement()) {

            stmt.execute(sqlPaciente);
            System.out.println("Tabla paciente creada");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
