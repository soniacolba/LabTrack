package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

    private static final String URL = "jdbc:sqlite:data/labtrack.db";

    public static Connection getConnection() throws SQLException {
        // Activa claves foráneas por conexión (importante en SQLite)
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return con;
    }

    public static void initDatabase() {

        String[] ddl = new String[] {
            "CREATE TABLE IF NOT EXISTS paciente (" +
            "  cip TEXT PRIMARY KEY," +
            "  nombre TEXT NOT NULL," +
            "  apellidos TEXT NOT NULL," +
            "  fecha_nacimiento TEXT NOT NULL" +
            ");",

            "CREATE TABLE IF NOT EXISTS usuario (" +
            "  id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT NOT NULL UNIQUE," +
            "  password TEXT NOT NULL," +
            "  rol TEXT NOT NULL CHECK (rol IN ('TECNICO', 'FACULTATIVO'))" +
            ");",

            "CREATE TABLE IF NOT EXISTS tipo_muestra (" +
            "  id_tipo_muestra INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nombre TEXT NOT NULL UNIQUE" +
            ");",

            "CREATE TABLE IF NOT EXISTS prueba (" +
            "  id_prueba INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nombre TEXT NOT NULL UNIQUE" +
            ");",

            "CREATE TABLE IF NOT EXISTS tipo_muestra_prueba (" +
            "  id_tipo_muestra INTEGER NOT NULL," +
            "  id_prueba INTEGER NOT NULL," +
            "  PRIMARY KEY (id_tipo_muestra, id_prueba)," +
            "  FOREIGN KEY (id_tipo_muestra) REFERENCES tipo_muestra(id_tipo_muestra) ON DELETE CASCADE," +
            "  FOREIGN KEY (id_prueba) REFERENCES prueba(id_prueba) ON DELETE CASCADE" +
            ");",

            "CREATE TABLE IF NOT EXISTS peticion (" +
            "  id_peticion INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  fecha_registro TEXT NOT NULL," +
            "  prioridad TEXT NOT NULL CHECK (prioridad IN ('NORMAL', 'URGENTE'))," +
            "  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','REALIZADA','VALIDADA','INFORMADA','ANULADA'))," +
            "  cip_paciente TEXT NOT NULL," +
            "  id_usuario INTEGER NOT NULL," +
            "  id_tipo_muestra INTEGER NOT NULL," +
            "  FOREIGN KEY (cip_paciente) REFERENCES paciente(cip)," +
            "  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)," +
            "  FOREIGN KEY (id_tipo_muestra) REFERENCES tipo_muestra(id_tipo_muestra)" +
            ");",

            "CREATE TABLE IF NOT EXISTS peticion_prueba (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','REALIZADA','VALIDADA'))," +
            "  resultado TEXT," +
            "  id_peticion INTEGER NOT NULL," +
            "  id_prueba INTEGER NOT NULL," +
            "  FOREIGN KEY (id_peticion) REFERENCES peticion(id_peticion) ON DELETE CASCADE," +
            "  FOREIGN KEY (id_prueba) REFERENCES prueba(id_prueba)" +
            ");",

            "CREATE TABLE IF NOT EXISTS incidencia (" +
            "  id_incidencia INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  fecha TEXT NOT NULL," +
            "  tipo TEXT NOT NULL CHECK (tipo IN ('CONTAMINADA','INSUFICIENTE','DERRAMADA','COAGULADA'))," +
            "  id_peticion INTEGER NOT NULL UNIQUE," +
            "  id_usuario INTEGER NOT NULL," +
            "  FOREIGN KEY (id_peticion) REFERENCES peticion(id_peticion) ON DELETE CASCADE," +
            "  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)" +
            ");"
        };

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            for (String sql : ddl) {
                st.execute(sql);
            }

            st.execute("INSERT OR IGNORE INTO usuario (id_usuario, username, password, rol) VALUES (1, 'tecnico', '1234', 'TECNICO');");

            st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (1, 'Sangre');");
            st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (2, 'Exudado faríngeo');");

            st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (1, 'Hemograma');");
            st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (2, 'Bioquímica');");
            st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (3, 'Cultivo bacteriano');");
            st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (4, 'PCR');");

            st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1,1);");
            st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1,2);");
            st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2,3);");
            st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2,4);");
            
            System.out.println("BD inicializada con todas las tablas");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

