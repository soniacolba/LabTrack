package app;

import db.DB;
import view.VentanaLogin;

public class LabTrack {

    public static void main(String[] args) {
        DB.initDatabase();

        java.awt.EventQueue.invokeLater(() -> {
            new VentanaLogin().setVisible(true);
        });
    }
}