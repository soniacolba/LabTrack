package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

    private static final String URL = "jdbc:sqlite:data/labtrack.db";

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return con;
    }

    public static void initDatabase() {
        try (Connection con = getConnection();
                Statement st = con.createStatement()) {

            crearTablas(st);
            insertarDatosBase(st);

            System.out.println("BD inicializada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void crearTablas(Statement st) throws SQLException {

        st.execute(
                "CREATE TABLE IF NOT EXISTS paciente ("
                + "  cip TEXT PRIMARY KEY,"
                + "  nombre TEXT NOT NULL,"
                + "  apellidos TEXT NOT NULL,"
                + "  fecha_nacimiento TEXT NOT NULL"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS usuario ("
                + "  id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  username TEXT NOT NULL UNIQUE,"
                + "  password TEXT NOT NULL,"
                + "  rol TEXT NOT NULL CHECK (rol IN ('TECNICO', 'FACULTATIVO'))"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS tipo_muestra ("
                + "  id_tipo_muestra INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  nombre TEXT NOT NULL UNIQUE"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS prueba ("
                + "  id_prueba INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  nombre TEXT NOT NULL UNIQUE"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS tipo_muestra_prueba ("
                + "  id_tipo_muestra INTEGER NOT NULL,"
                + "  id_prueba INTEGER NOT NULL,"
                + "  PRIMARY KEY (id_tipo_muestra, id_prueba),"
                + "  FOREIGN KEY (id_tipo_muestra) REFERENCES tipo_muestra(id_tipo_muestra) ON DELETE CASCADE,"
                + "  FOREIGN KEY (id_prueba) REFERENCES prueba(id_prueba) ON DELETE CASCADE"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS peticion ("
                + "  id_peticion INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  fecha_registro TEXT NOT NULL,"
                + "  prioridad TEXT NOT NULL CHECK (prioridad IN ('NORMAL', 'URGENTE')),"
                + "  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','REALIZADA','VALIDADA','INFORMADA','ANULADA')),"
                + "  cip_paciente TEXT NOT NULL,"
                + "  id_usuario INTEGER NOT NULL,"
                + "  id_tipo_muestra INTEGER NOT NULL,"
                + "  FOREIGN KEY (cip_paciente) REFERENCES paciente(cip),"
                + "  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),"
                + "  FOREIGN KEY (id_tipo_muestra) REFERENCES tipo_muestra(id_tipo_muestra)"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS peticion_prueba ("
                + "  id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','REALIZADA','VALIDADA', 'ANULADA')),"
                + "  resultado TEXT,"
                + "  id_peticion INTEGER NOT NULL,"
                + "  id_prueba INTEGER NOT NULL,"
                + "  FOREIGN KEY (id_peticion) REFERENCES peticion(id_peticion) ON DELETE CASCADE,"
                + "  FOREIGN KEY (id_prueba) REFERENCES prueba(id_prueba)"
                + ");"
        );

        st.execute(
                "CREATE TABLE IF NOT EXISTS incidencia ("
                + "  id_incidencia INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  fecha TEXT NOT NULL,"
                + "  tipo TEXT NOT NULL CHECK (tipo IN ('CONTAMINADA','INSUFICIENTE','DERRAMADA','COAGULADA')),"
                + "  id_peticion INTEGER NOT NULL UNIQUE,"
                + "  id_usuario INTEGER NOT NULL,"
                + "  FOREIGN KEY (id_peticion) REFERENCES peticion(id_peticion) ON DELETE CASCADE,"
                + "  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)"
                + ");"
        );
    }

    private static void insertarDatosBase(Statement st) throws SQLException {

        insertarUsuariosBase(st);
        insertarPacientesBase(st);
        insertarTiposMuestraBase(st);
        insertarPruebasBase(st);
        insertarRelacionesTipoMuestraPrueba(st);
    }

    private static void insertarUsuariosBase(Statement st) throws SQLException {
        st.execute("INSERT OR IGNORE INTO usuario (id_usuario, username, password, rol) VALUES (1, 'tecnico', '1234', 'TECNICO');");
        st.execute("INSERT OR IGNORE INTO usuario (id_usuario, username, password, rol) VALUES (2, 'facultativo', '1234', 'FACULTATIVO');");
    }

    private static void insertarPacientesBase(Statement st) throws SQLException {
        st.execute("INSERT OR IGNORE INTO paciente (cip, nombre, apellidos, fecha_nacimiento) VALUES ('111111111', 'Ana', 'García López', '1990-05-12');");
        st.execute("INSERT OR IGNORE INTO paciente (cip, nombre, apellidos, fecha_nacimiento) VALUES ('222222222', 'Luis', 'Martín Pérez', '1985-11-03');");
        st.execute("INSERT OR IGNORE INTO paciente (cip, nombre, apellidos, fecha_nacimiento) VALUES ('333333333', 'Marta', 'Sánchez Ruiz', '2001-02-20');");
        st.execute("INSERT OR IGNORE INTO paciente (cip, nombre, apellidos, fecha_nacimiento) VALUES ('444444444', 'Carlos', 'Fernández Gómez', '1978-09-14');");
    }

    private static void insertarTiposMuestraBase(Statement st) throws SQLException {
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (1, 'Sangre');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (2, 'Suero');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (3, 'Plasma');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (4, 'Orina');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (5, 'Heces');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (6, 'Exudado faríngeo');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (7, 'Exudado nasal');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (8, 'Esputo');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (9, 'Líquido cefalorraquídeo');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (10, 'Líquido pleural');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (11, 'Líquido ascítico');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (12, 'Biopsia');");
        st.execute("INSERT OR IGNORE INTO tipo_muestra (id_tipo_muestra, nombre) VALUES (13, 'Médula ósea');");
    }

    private static void insertarPruebasBase(Statement st) throws SQLException {
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (1, 'Hemograma');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (2, 'Bioquímica básica');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (3, 'Glucosa');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (4, 'Urea');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (5, 'Creatinina');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (6, 'Ácido úrico');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (7, 'Colesterol total');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (8, 'HDL colesterol');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (9, 'LDL colesterol');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (10, 'Triglicéridos');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (11, 'Hierro sérico');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (12, 'Ferritina');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (13, 'Transaminasas');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (14, 'Bilirrubina total');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (15, 'Proteínas totales');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (16, 'Albúmina');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (17, 'Calcio');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (18, 'Sodio');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (19, 'Potasio');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (20, 'PCR ultrasensible');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (21, 'TSH');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (22, 'T4 libre');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (23, 'Vitamina D');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (24, 'Vitamina B12');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (25, 'Ácido fólico');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (26, 'Análisis sistemático de orina');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (27, 'Sedimento urinario');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (28, 'Urocultivo');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (29, 'Proteinuria en orina');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (30, 'Microalbuminuria');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (31, 'Coprocultivo');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (32, 'Sangre oculta en heces');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (33, 'Parásitos en heces');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (34, 'Calprotectina fecal');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (35, 'Cultivo bacteriano');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (36, 'Tinción de Gram');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (37, 'Antibiograma');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (38, 'PCR SARS-CoV-2');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (39, 'PCR gripe A/B');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (40, 'Detección de Streptococcus');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (41, 'Cultivo de hongos');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (42, 'Detección de VIH');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (43, 'Detección de VHB');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (44, 'Detección de VHC');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (45, 'Citología de líquidos');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (46, 'Cultivo de líquido biológico');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (47, 'Recuento celular');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (48, 'Estudio histológico');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (49, 'Inmunohistoquímica');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (50, 'Aspirado medular');");
        st.execute("INSERT OR IGNORE INTO prueba (id_prueba, nombre) VALUES (51, 'Mielograma');");
    }

    private static void insertarRelacionesTipoMuestraPrueba(Statement st) throws SQLException {

        // Sangre
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 1);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 3);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 11);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 12);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 20);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 24);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (1, 25);");

        // Suero
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 2);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 3);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 4);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 5);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 6);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 7);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 8);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 9);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 10);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 13);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 14);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 15);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 16);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 17);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 18);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 19);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 21);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 22);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 23);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 42);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 43);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (2, 44);");

        // Plasma
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (3, 3);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (3, 18);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (3, 19);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (3, 20);");

        // Orina
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (4, 26);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (4, 27);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (4, 28);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (4, 29);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (4, 30);");

        // Heces
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (5, 31);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (5, 32);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (5, 33);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (5, 34);");

        // Exudado faríngeo
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 35);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 36);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 37);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 38);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 39);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 40);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (6, 41);");

        // Exudado nasal
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (7, 35);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (7, 36);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (7, 38);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (7, 39);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (7, 41);");

        // Esputo
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (8, 35);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (8, 36);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (8, 37);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (8, 41);");

        // Líquido cefalorraquídeo
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (9, 45);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (9, 46);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (9, 47);");

        // Líquido pleural
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (10, 45);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (10, 46);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (10, 47);");

        // Líquido ascítico
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (11, 45);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (11, 46);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (11, 47);");

        // Biopsia
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (12, 48);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (12, 49);");

        // Médula ósea
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (13, 50);");
        st.execute("INSERT OR IGNORE INTO tipo_muestra_prueba VALUES (13, 51);");
    }
}
