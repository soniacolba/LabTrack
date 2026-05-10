/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import dao.EstadisticasDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

/**
 *
 * @author leia6
 */
public class PanelEstadisticas extends javax.swing.JPanel {

    private JPanel panelPrincipal;

    public PanelEstadisticas(JPanel panelPrincipal) {

        initComponents();
        cargarResumenGeneral();
        cargarPeticionesPorEstado();
        personalizarTablas();
        cargarGraficoPeticionesPorEstado();
        cargarPruebasSolicitadas();
        cargarGraficoPruebasSolicitadas();
    }

    private void cargarResumenGeneral() {

        EstadisticasDAO dao = new EstadisticasDAO();

        lblTotalPacientes.setText(String.valueOf(dao.contarPacientes()));
        lblTotalPeticiones.setText(String.valueOf(dao.contarPeticiones()));
        lblUrgentes.setText(String.valueOf(dao.contarPeticionesUrgentes()));
        lblPruebasRealizadas.setText(String.valueOf(dao.contarPruebasRealizadas()));
        lblAnuladas.setText(String.valueOf(dao.contarPeticionesAnuladas()));
    }

    private void cargarPeticionesPorEstado() {

        EstadisticasDAO dao = new EstadisticasDAO();

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Estado", "Nº de peticiones"}, 0
        );

        Object[][] datos = dao.obtenerPeticionesPorEstado();

        for (Object[] fila : datos) {
            modelo.addRow(fila);
        }

        tablaEstados.setModel(modelo);
    }

    private void personalizarTablas() {
        tablaEstados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaEstados.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                c.setFont(new Font("Segoe UI",
                        row == 4 ? Font.BOLD : Font.PLAIN,
                        13));

                return c;
            }
        });
        
        tablaPruebas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void cargarGraficoPeticionesPorEstado() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        EstadisticasDAO dao = new EstadisticasDAO();

        Object[][] datos = dao.obtenerPeticionesPorEstado();

        for (Object[] fila : datos) {

            String estado = fila[0].toString();
            int total = Integer.parseInt(fila[1].toString());

            if (!estado.equals("Total")) {
                dataset.addValue(total, "Peticiones", estado);
            }
        }

        JFreeChart grafico = ChartFactory.createBarChart(
                "Peticiones por estado",
                "Estado",
                "Cantidad",
                dataset
        );

        ChartPanel panel = new ChartPanel(grafico);

        panel.setPreferredSize(new Dimension(550, 250));
        panelGraficoEstados.removeAll();
        panelGraficoEstados.setLayout(new BorderLayout());
        panelGraficoEstados.add(panel, BorderLayout.CENTER);
        panelGraficoEstados.revalidate();
        panelGraficoEstados.repaint();

        CategoryPlot plot = grafico.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setLabel("");
        plot.getRangeAxis().setLabel("Nº de peticiones");

        grafico.setBackgroundPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(137, 188, 232));
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLowerMargin(0.08);
        domainAxis.setUpperMargin(0.08);
        domainAxis.setCategoryMargin(0.25);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        grafico.setTitle("");
        grafico.removeLegend();

    }

    private void cargarPruebasSolicitadas() {

        EstadisticasDAO dao = new EstadisticasDAO();

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Prueba", "Nº de veces solicitada"}, 0
        );

        Object[][] datos = dao.obtenerTopPruebasSolicitadas();

        for (Object[] fila : datos) {
            modelo.addRow(fila);
        }

        tablaPruebas.setModel(modelo);
    }

    private void cargarGraficoPruebasSolicitadas() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        EstadisticasDAO dao = new EstadisticasDAO();

        Object[][] datos = dao.obtenerTopPruebasSolicitadas();

        for (Object[] fila : datos) {
            String prueba = fila[0].toString();
            int total = Integer.parseInt(fila[1].toString());

            dataset.addValue(total, "Pruebas", prueba);
        }

        JFreeChart grafico = ChartFactory.createBarChart(
                "",
                "",
                "Nº de veces solicitada",
                dataset,
                PlotOrientation.HORIZONTAL,
                false,
                true,
                false
        );

        ChartPanel panel = new ChartPanel(grafico);

        panelGraficoPruebas.removeAll();
        panelGraficoPruebas.setLayout(new BorderLayout());
        panelGraficoPruebas.add(panel, BorderLayout.CENTER);
        panelGraficoPruebas.validate();

        CategoryPlot plot = grafico.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setLabel("");
        plot.getRangeAxis().setLabel("Nº de veces solicitada");

        grafico.setBackgroundPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(137, 188, 232));
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.05);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        panelGraficoPruebas.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupPrioridad = new javax.swing.ButtonGroup();
        jLabel15 = new javax.swing.JLabel();
        panelSuperior = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        panelCentral = new javax.swing.JPanel();
        panelResumenGeneral = new javax.swing.JPanel();
        resumenGeneral = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        panelTarjetas = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblTotalPacientes = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblUrgentes = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lblPruebasRealizadas = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        lblTotalPeticiones = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        lblAnuladas = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        panelPeticionesEstado = new javax.swing.JPanel();
        peticionesEstado = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        panelTabla = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaEstados = new javax.swing.JTable();
        panelGraficoEstados = new javax.swing.JPanel();
        panelPruebas = new javax.swing.JPanel();
        detallesPruebas = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        panelTablaPruebas = new javax.swing.JPanel();
        panelGraficoPruebas = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaPruebas = new javax.swing.JTable();

        jLabel15.setText("jLabel15");

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Estadísticas");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelSuperior.add(jLabel1, java.awt.BorderLayout.CENTER);

        add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        panelCentral.setBackground(new java.awt.Color(255, 255, 255));
        panelCentral.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 1));
        panelCentral.setMinimumSize(new java.awt.Dimension(0, 0));
        panelCentral.setPreferredSize(new java.awt.Dimension(0, 0));
        panelCentral.setLayout(new javax.swing.BoxLayout(panelCentral, javax.swing.BoxLayout.Y_AXIS));

        panelResumenGeneral.setBackground(new java.awt.Color(243, 245, 249));
        panelResumenGeneral.setMaximumSize(new java.awt.Dimension(2147483647, 280));
        panelResumenGeneral.setMinimumSize(new java.awt.Dimension(1220, 170));
        panelResumenGeneral.setName(""); // NOI18N
        panelResumenGeneral.setPreferredSize(new java.awt.Dimension(1220, 170));
        panelResumenGeneral.setLayout(new java.awt.BorderLayout());

        resumenGeneral.setBackground(new java.awt.Color(215, 232, 247));
        resumenGeneral.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resumenGeneral.setMinimumSize(new java.awt.Dimension(124, 70));
        resumenGeneral.setLayout(new java.awt.BorderLayout());

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Resumen general");
        resumenGeneral.add(jLabel8, java.awt.BorderLayout.WEST);

        panelResumenGeneral.add(resumenGeneral, java.awt.BorderLayout.NORTH);

        panelTarjetas.setBackground(new java.awt.Color(243, 245, 249));
        panelTarjetas.setMinimumSize(new java.awt.Dimension(100, 130));
        panelTarjetas.setPreferredSize(new java.awt.Dimension(1400, 130));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(206, 105));
        jPanel2.setRequestFocusEnabled(false);

        lblTotalPacientes.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTotalPacientes.setForeground(new java.awt.Color(60, 121, 206));
        lblTotalPacientes.setText("128");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        jLabel4.setText("<html>Total de<br>pacientes</html>");

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/pacientes.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblTotalPacientes, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addGap(24, 24, 24))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(lblTotalPacientes, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(206, 105));

        lblUrgentes.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblUrgentes.setForeground(new java.awt.Color(60, 121, 206));
        lblUrgentes.setText("128");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        jLabel9.setText("<html>Peticiones<br>urgentes</html>");

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/pendientes.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblUrgentes, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(119, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10)
                        .addGap(24, 24, 24))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(lblUrgentes, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setPreferredSize(new java.awt.Dimension(206, 105));

        lblPruebasRealizadas.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblPruebasRealizadas.setForeground(new java.awt.Color(60, 121, 206));
        lblPruebasRealizadas.setText("128");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        jLabel12.setText("<html>Pruebas<br>realizadas</html>");

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/realizadas.png"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(lblPruebasRealizadas, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(119, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13)
                        .addGap(24, 24, 24))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(lblPruebasRealizadas, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setPreferredSize(new java.awt.Dimension(206, 105));

        lblTotalPeticiones.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTotalPeticiones.setForeground(new java.awt.Color(60, 121, 206));
        lblTotalPeticiones.setText("128");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        jLabel16.setText("<html>Total de<br>peticiones</html>");

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/peticiones.png"))); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lblTotalPeticiones, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(119, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addGap(24, 24, 24))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(lblTotalPeticiones, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setPreferredSize(new java.awt.Dimension(206, 105));

        lblAnuladas.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblAnuladas.setForeground(new java.awt.Color(60, 121, 206));
        lblAnuladas.setText("128");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        jLabel25.setText("<html>Peticiones<br>anuladas</html>");

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/anuladas.png"))); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(lblAnuladas, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(119, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel26)
                        .addGap(24, 24, 24))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(lblAnuladas, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelTarjetasLayout = new javax.swing.GroupLayout(panelTarjetas);
        panelTarjetas.setLayout(panelTarjetasLayout);
        panelTarjetasLayout.setHorizontalGroup(
            panelTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTarjetasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(288, Short.MAX_VALUE))
        );
        panelTarjetasLayout.setVerticalGroup(
            panelTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTarjetasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addContainerGap())
        );

        panelResumenGeneral.add(panelTarjetas, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelResumenGeneral);

        panelPeticionesEstado.setBackground(new java.awt.Color(243, 245, 249));
        panelPeticionesEstado.setMaximumSize(new java.awt.Dimension(2147483647, 320));
        panelPeticionesEstado.setMinimumSize(new java.awt.Dimension(0, 0));
        panelPeticionesEstado.setName(""); // NOI18N
        panelPeticionesEstado.setPreferredSize(new java.awt.Dimension(956, 320));
        panelPeticionesEstado.setLayout(new java.awt.BorderLayout());

        peticionesEstado.setBackground(new java.awt.Color(215, 232, 247));
        peticionesEstado.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        peticionesEstado.setMinimumSize(new java.awt.Dimension(124, 70));
        peticionesEstado.setPreferredSize(new java.awt.Dimension(391, 51));
        peticionesEstado.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("Peticiones por estado");
        peticionesEstado.add(jLabel2, java.awt.BorderLayout.WEST);

        panelPeticionesEstado.add(peticionesEstado, java.awt.BorderLayout.NORTH);

        panelTabla.setBackground(new java.awt.Color(243, 245, 249));

        tablaEstados.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tablaEstados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Estado", "Nº de peticiones"
            }
        ));
        tablaEstados.setEnabled(false);
        tablaEstados.setFillsViewportHeight(true);
        tablaEstados.setMaximumSize(new java.awt.Dimension(2147483647, 100));
        tablaEstados.setMinimumSize(new java.awt.Dimension(30, 100));
        tablaEstados.setPreferredSize(new java.awt.Dimension(150, 100));
        tablaEstados.setRowHeight(30);
        jScrollPane1.setViewportView(tablaEstados);

        panelGraficoEstados.setMaximumSize(new java.awt.Dimension(32767, 200));
        panelGraficoEstados.setMinimumSize(new java.awt.Dimension(636, 240));

        javax.swing.GroupLayout panelGraficoEstadosLayout = new javax.swing.GroupLayout(panelGraficoEstados);
        panelGraficoEstados.setLayout(panelGraficoEstadosLayout);
        panelGraficoEstadosLayout.setHorizontalGroup(
            panelGraficoEstadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 636, Short.MAX_VALUE)
        );
        panelGraficoEstadosLayout.setVerticalGroup(
            panelGraficoEstadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelTablaLayout = new javax.swing.GroupLayout(panelTabla);
        panelTabla.setLayout(panelTablaLayout);
        panelTablaLayout.setHorizontalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(110, 110, 110)
                .addComponent(panelGraficoEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(226, Short.MAX_VALUE))
        );
        panelTablaLayout.setVerticalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addGroup(panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTablaLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelTablaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelGraficoEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        panelPeticionesEstado.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelPeticionesEstado);

        panelPruebas.setBackground(new java.awt.Color(243, 245, 249));
        panelPruebas.setMinimumSize(new java.awt.Dimension(0, 290));
        panelPruebas.setPreferredSize(new java.awt.Dimension(0, 290));
        panelPruebas.setRequestFocusEnabled(false);
        panelPruebas.setLayout(new java.awt.BorderLayout());

        detallesPruebas.setBackground(new java.awt.Color(215, 232, 247));
        detallesPruebas.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detallesPruebas.setMinimumSize(new java.awt.Dimension(124, 70));
        detallesPruebas.setPreferredSize(new java.awt.Dimension(381, 51));
        detallesPruebas.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Pruebas más solicitadas");
        detallesPruebas.add(jLabel7, java.awt.BorderLayout.WEST);

        panelPruebas.add(detallesPruebas, java.awt.BorderLayout.NORTH);

        panelTablaPruebas.setBackground(new java.awt.Color(243, 245, 249));

        panelGraficoPruebas.setMaximumSize(new java.awt.Dimension(32767, 250));
        panelGraficoPruebas.setMinimumSize(new java.awt.Dimension(100, 250));
        panelGraficoPruebas.setPreferredSize(new java.awt.Dimension(642, 250));

        javax.swing.GroupLayout panelGraficoPruebasLayout = new javax.swing.GroupLayout(panelGraficoPruebas);
        panelGraficoPruebas.setLayout(panelGraficoPruebasLayout);
        panelGraficoPruebasLayout.setHorizontalGroup(
            panelGraficoPruebasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 642, Short.MAX_VALUE)
        );
        panelGraficoPruebasLayout.setVerticalGroup(
            panelGraficoPruebasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        tablaPruebas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tablaPruebas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Prueba", "Nº de veces solicitada"
            }
        ));
        tablaPruebas.setEnabled(false);
        tablaPruebas.setFillsViewportHeight(true);
        tablaPruebas.setMaximumSize(new java.awt.Dimension(2147483647, 100));
        tablaPruebas.setMinimumSize(new java.awt.Dimension(30, 100));
        tablaPruebas.setPreferredSize(new java.awt.Dimension(150, 100));
        tablaPruebas.setRowHeight(30);
        jScrollPane2.setViewportView(tablaPruebas);

        javax.swing.GroupLayout panelTablaPruebasLayout = new javax.swing.GroupLayout(panelTablaPruebas);
        panelTablaPruebas.setLayout(panelTablaPruebasLayout);
        panelTablaPruebasLayout.setHorizontalGroup(
            panelTablaPruebasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaPruebasLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(105, 105, 105)
                .addComponent(panelGraficoPruebas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(224, Short.MAX_VALUE))
        );
        panelTablaPruebasLayout.setVerticalGroup(
            panelTablaPruebasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaPruebasLayout.createSequentialGroup()
                .addGroup(panelTablaPruebasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTablaPruebasLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelTablaPruebasLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelGraficoPruebas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(104, Short.MAX_VALUE))
        );

        panelPruebas.add(panelTablaPruebas, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelPruebas);

        add(panelCentral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupPrioridad;
    private javax.swing.JPanel detallesPruebas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAnuladas;
    private javax.swing.JLabel lblPruebasRealizadas;
    private javax.swing.JLabel lblTotalPacientes;
    private javax.swing.JLabel lblTotalPeticiones;
    private javax.swing.JLabel lblUrgentes;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelGraficoEstados;
    private javax.swing.JPanel panelGraficoPruebas;
    private javax.swing.JPanel panelPeticionesEstado;
    private javax.swing.JPanel panelPruebas;
    private javax.swing.JPanel panelResumenGeneral;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JPanel panelTablaPruebas;
    private javax.swing.JPanel panelTarjetas;
    private javax.swing.JPanel peticionesEstado;
    private javax.swing.JPanel resumenGeneral;
    private javax.swing.JTable tablaEstados;
    private javax.swing.JTable tablaPruebas;
    // End of variables declaration//GEN-END:variables
}
