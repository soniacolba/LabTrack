package app;

import db.DB;
import view.PantallaPrincipal;

public class LabTrack {

    public static void main(String[] args) {
        DB.initDatabase();

        java.awt.EventQueue.invokeLater(() -> {
            new PantallaPrincipal().setVisible(true);
        });
    }
}