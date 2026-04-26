/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import db.DB;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;

public class GeneradorInformePDF {

    public static boolean generarInforme(int idPeticion) {

        try (Connection con = DB.getConnection()) {

            String rutaJrxml = "src/reportes/informe_peticion.jrxml";
            

            JasperReport reporte = JasperCompileManager.compileReport(rutaJrxml);

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idPeticion", idPeticion);

            JasperPrint print = JasperFillManager.fillReport(reporte, parametros, con);

            File carpeta = new File("informes");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            String rutaSalida = "informes/peticion_" + idPeticion + ".pdf";

            JasperExportManager.exportReportToPdfFile(print, rutaSalida);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
