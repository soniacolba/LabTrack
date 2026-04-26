/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import dao.PacienteDAO;
import dao.PeticionDAO;
import dao.TipoMuestraDAO;
import db.DB;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import model.EnumEstadoPeticion;
import model.Peticion;
import model.Paciente;
import model.TipoMuestra;
import util.GeneradorInformePDF;

/**
 *
 * @author leia6
 */
public class PanelInformesPeticion extends javax.swing.JPanel {

    private JPanel panelPrincipal;

    private Peticion peticionActual;
    private Paciente pacienteActual;
    private Map<Integer, String> mapaTiposMuestra = new HashMap<>();

    public PanelInformesPeticion(JPanel panelPrincipal) {
        this.panelPrincipal = panelPrincipal;
        initComponents();
        personalizarTabla();
        cargarMapaTiposMuestra();
        limpiarFormulario();
        cargarPendientesInformar();
    }

    private void personalizarTabla() {
        tblPendientes.setBackground(Color.WHITE);
        tblPendientes.setForeground(Color.BLACK);
        tblPendientes.setSelectionBackground(new Color(79, 129, 189));
        tblPendientes.setSelectionForeground(Color.WHITE);
        tblPendientes.setRowHeight(22);

        tblPendientes.getTableHeader().setBackground(new Color(220, 230, 241));
        tblPendientes.getTableHeader().setForeground(Color.BLACK);
    }

    private void cargarMapaTiposMuestra() {
        TipoMuestraDAO dao = new TipoMuestraDAO();
        List<TipoMuestra> lista = dao.listarTodos();

        mapaTiposMuestra.clear();

        for (TipoMuestra tm : lista) {
            mapaTiposMuestra.put(tm.getId(), tm.getNombre());
        }
    }

    private void limpiarFormulario() {
        peticionActual = null;
        pacienteActual = null;

        txtBuscarId.setText("");

        txtId.setText("");
        txtFecha.setText("");
        txtEstado.setText("");
        txtTipoMuestra.setText("");

        txtCip.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtFechaNacimiento.setText("");
    }

    private void cargarPeticion(int idPeticion) {
        PeticionDAO peticionDAO = new PeticionDAO();
        PacienteDAO pacienteDAO = new PacienteDAO();

        Peticion p = peticionDAO.buscarPorId(idPeticion);

        if (p == null) {
            JOptionPane.showMessageDialog(this, "No existe ninguna petición con ese ID.");
            limpiarFormulario();
            return;
        }

        peticionActual = p;
        pacienteActual = pacienteDAO.buscarPorCip(p.getCipPaciente());

        cargarDatosPeticion();
        cargarDatosPaciente();
    }

    private void cargarDatosPeticion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        txtId.setText(String.valueOf(peticionActual.getIdPeticion()));
        txtFecha.setText(peticionActual.getFechaRegistro().format(formatter));
        txtEstado.setText(peticionActual.getEstado().name());

        txtTipoMuestra.setText(
                mapaTiposMuestra.getOrDefault(peticionActual.getIdTipoMuestra(), "--")
        );
    }

    private void cargarDatosPaciente() {
        if (pacienteActual == null) {
            txtCip.setText("");
            txtNombre.setText("");
            txtApellidos.setText("");
            txtFechaNacimiento.setText("");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        txtCip.setText(pacienteActual.getCip());
        txtNombre.setText(pacienteActual.getNombre());
        txtApellidos.setText(pacienteActual.getApellidos());
        txtFechaNacimiento.setText(pacienteActual.getFechaNacimiento().format(formatter));
    }

    public void cargarPendientesInformar() {
        PeticionDAO dao = new PeticionDAO();
        PacienteDAO pacienteDAO = new PacienteDAO();

        List<Peticion> lista = dao.listarPorEstado(EnumEstadoPeticion.VALIDADA);

        DefaultTableModel modelo = (DefaultTableModel) tblPendientes.getModel();
        modelo.setRowCount(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Peticion p : lista) {
            Paciente paciente = pacienteDAO.buscarPorCip(p.getCipPaciente());

            modelo.addRow(new Object[]{
                p.getIdPeticion(),
                p.getFechaRegistro().format(formatter),
                paciente != null ? paciente.getApellidos() + ", " + paciente.getNombre() : "--",
                p.getCipPaciente(),
                p.getPrioridad(),
                mapaTiposMuestra.getOrDefault(p.getIdTipoMuestra(), "--")
            });
        }
    }

    private void abrirInforme(int idPeticion) {
        try {
            File pdf = new File("informes/peticion_" + idPeticion + ".pdf");

            if (pdf.exists()) {
                Desktop.getDesktop().open(pdf);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el PDF generado.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo abrir el informe.");
        }
    }

    private void generarYAbrirInforme(int idPeticion) {
        boolean generado = GeneradorInformePDF.generarInforme(idPeticion);

        if (!generado) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el informe.");
            return;
        }

        abrirInforme(idPeticion);
    }

    private void informarPeticion(int idPeticion) {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas informar esta petición?",
                "Confirmar informe",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean generado = GeneradorInformePDF.generarInforme(idPeticion);

        if (!generado) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el informe.");
            return;
        }

        try (Connection con = DB.getConnection()) {
            PeticionDAO dao = new PeticionDAO();

            boolean ok = dao.actualizarEstado(
                    con,
                    idPeticion,
                    EnumEstadoPeticion.INFORMADA
            );

            if (ok) {
                JOptionPane.showMessageDialog(this, "Petición informada correctamente.");
                abrirInforme(idPeticion);
                cargarPendientesInformar();

                if (peticionActual != null && peticionActual.getIdPeticion() == idPeticion) {
                    cargarPeticion(idPeticion);
                }

            } else {
                JOptionPane.showMessageDialog(this, "No se pudo informar la petición.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al informar la petición.");
        }
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
        panelSuperior = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        panelCentral = new javax.swing.JPanel();
        panelDatosPaciente = new javax.swing.JPanel();
        datosPaciente = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        panelBtnBuscar1 = new javax.swing.JPanel();
        btnBuscar1 = new javax.swing.JLabel();
        txtBuscarId = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        formularioPaciente = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtFechaNacimiento = new javax.swing.JTextField();
        txtCip = new javax.swing.JTextField();
        panelBusqueda = new javax.swing.JPanel();
        datosPaciente1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        formularioPaciente1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtFecha = new javax.swing.JTextField();
        txtEstado = new javax.swing.JTextField();
        txtTipoMuestra = new javax.swing.JTextField();
        panelBtnVerInforme = new javax.swing.JPanel();
        btnVerInforme = new javax.swing.JLabel();
        panelDatosPeticion = new javax.swing.JPanel();
        detallesPeticion = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        formularioPeticion = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblPendientes = new javax.swing.JTable();
        panelInferior = new javax.swing.JPanel();
        panelBtnSalir = new javax.swing.JPanel();
        btnSalir = new javax.swing.JLabel();
        panelBtnInformar = new javax.swing.JPanel();
        btnInformar = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setMinimumSize(new java.awt.Dimension(1048, 840));
        setPreferredSize(new java.awt.Dimension(1048, 840));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Informe por petición");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelSuperior.add(jLabel1, java.awt.BorderLayout.CENTER);

        add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        panelCentral.setBackground(new java.awt.Color(255, 255, 255));
        panelCentral.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 1));
        panelCentral.setMinimumSize(new java.awt.Dimension(1008, 601));
        panelCentral.setPreferredSize(new java.awt.Dimension(1008, 601));
        panelCentral.setLayout(new javax.swing.BoxLayout(panelCentral, javax.swing.BoxLayout.Y_AXIS));

        panelDatosPaciente.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPaciente.setMaximumSize(new java.awt.Dimension(2147483647, 230));
        panelDatosPaciente.setMinimumSize(new java.awt.Dimension(1006, 230));
        panelDatosPaciente.setName(""); // NOI18N
        panelDatosPaciente.setPreferredSize(new java.awt.Dimension(956, 230));
        panelDatosPaciente.setLayout(new java.awt.BorderLayout());

        datosPaciente.setBackground(new java.awt.Color(215, 232, 247));
        datosPaciente.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        datosPaciente.setMinimumSize(new java.awt.Dimension(124, 70));
        datosPaciente.setPreferredSize(new java.awt.Dimension(391, 51));
        datosPaciente.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("Datos del paciente");
        datosPaciente.add(jLabel2, java.awt.BorderLayout.WEST);

        jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel7.setMinimumSize(new java.awt.Dimension(1006, 0));
        jPanel7.setOpaque(false);
        jPanel7.setPreferredSize(new java.awt.Dimension(319, 31));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelBtnBuscar1.setBackground(new java.awt.Color(75, 113, 167));

        btnBuscar1.setBackground(new java.awt.Color(255, 255, 255));
        btnBuscar1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnBuscar1.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnBuscar1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/lupab.png"))); // NOI18N
        btnBuscar1.setText("Buscar");
        btnBuscar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBuscar1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnBuscar1Layout = new javax.swing.GroupLayout(panelBtnBuscar1);
        panelBtnBuscar1.setLayout(panelBtnBuscar1Layout);
        panelBtnBuscar1Layout.setHorizontalGroup(
            panelBtnBuscar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnBuscar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
        );
        panelBtnBuscar1Layout.setVerticalGroup(
            panelBtnBuscar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnBuscar1, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        jPanel7.add(panelBtnBuscar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 0, -1, -1));

        txtBuscarId.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jPanel7.add(txtBuscarId, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 0, 90, 30));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel17.setText("Nº petición");
        jPanel7.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 80, 30));

        datosPaciente.add(jPanel7, java.awt.BorderLayout.LINE_END);

        panelDatosPaciente.add(datosPaciente, java.awt.BorderLayout.NORTH);

        formularioPaciente.setMaximumSize(new java.awt.Dimension(1006, 160));
        formularioPaciente.setMinimumSize(new java.awt.Dimension(1006, 160));
        formularioPaciente.setOpaque(false);
        formularioPaciente.setPreferredSize(new java.awt.Dimension(1006, 160));
        formularioPaciente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setText("CIP");
        formularioPaciente.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("Nombre");
        formularioPaciente.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 20, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Fecha de nacimiento");
        formularioPaciente.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 20, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Apellidos");
        formularioPaciente.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        txtApellidos.setEditable(false);
        txtApellidos.setBackground(new java.awt.Color(255, 255, 255));
        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 336, 29));

        txtNombre.setEditable(false);
        txtNombre.setBackground(new java.awt.Color(255, 255, 255));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 50, 336, 29));

        txtFechaNacimiento.setEditable(false);
        txtFechaNacimiento.setBackground(new java.awt.Color(255, 255, 255));
        txtFechaNacimiento.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtFechaNacimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 50, 237, 29));

        txtCip.setEditable(false);
        txtCip.setBackground(new java.awt.Color(255, 255, 255));
        txtCip.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtCip, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 336, 29));

        panelDatosPaciente.add(formularioPaciente, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelDatosPaciente);

        panelBusqueda.setBackground(new java.awt.Color(243, 245, 249));
        panelBusqueda.setMaximumSize(new java.awt.Dimension(2147483647, 280));
        panelBusqueda.setMinimumSize(new java.awt.Dimension(956, 200));
        panelBusqueda.setName(""); // NOI18N
        panelBusqueda.setPreferredSize(new java.awt.Dimension(956, 200));
        panelBusqueda.setLayout(new java.awt.BorderLayout());

        datosPaciente1.setBackground(new java.awt.Color(215, 232, 247));
        datosPaciente1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        datosPaciente1.setMinimumSize(new java.awt.Dimension(124, 70));
        datosPaciente1.setLayout(new java.awt.BorderLayout());

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Datos de la petición");
        datosPaciente1.add(jLabel8, java.awt.BorderLayout.WEST);

        panelBusqueda.add(datosPaciente1, java.awt.BorderLayout.NORTH);

        formularioPaciente1.setMaximumSize(new java.awt.Dimension(1006, 160));
        formularioPaciente1.setMinimumSize(new java.awt.Dimension(1006, 160));
        formularioPaciente1.setOpaque(false);
        formularioPaciente1.setPreferredSize(new java.awt.Dimension(1006, 160));
        formularioPaciente1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Estado");
        formularioPaciente1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Fecha y hora");
        formularioPaciente1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel16.setText("ID");
        formularioPaciente1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel11.setText("Tipo de muestra");
        formularioPaciente1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(255, 255, 255));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente1.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 70, 29));

        txtFecha.setEditable(false);
        txtFecha.setBackground(new java.awt.Color(255, 255, 255));
        txtFecha.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFechaActionPerformed(evt);
            }
        });
        formularioPaciente1.add(txtFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 220, 29));

        txtEstado.setEditable(false);
        txtEstado.setBackground(new java.awt.Color(255, 255, 255));
        txtEstado.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente1.add(txtEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 30, 180, 29));

        txtTipoMuestra.setEditable(false);
        txtTipoMuestra.setBackground(new java.awt.Color(255, 255, 255));
        txtTipoMuestra.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente1.add(txtTipoMuestra, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 90, 360, 29));

        panelBtnVerInforme.setBackground(new java.awt.Color(75, 113, 167));

        btnVerInforme.setBackground(new java.awt.Color(255, 255, 255));
        btnVerInforme.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnVerInforme.setForeground(new java.awt.Color(255, 255, 255));
        btnVerInforme.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnVerInforme.setText("Ver Informe");
        btnVerInforme.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnVerInformeMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnVerInformeLayout = new javax.swing.GroupLayout(panelBtnVerInforme);
        panelBtnVerInforme.setLayout(panelBtnVerInformeLayout);
        panelBtnVerInformeLayout.setHorizontalGroup(
            panelBtnVerInformeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBtnVerInformeLayout.createSequentialGroup()
                .addComponent(btnVerInforme, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelBtnVerInformeLayout.setVerticalGroup(
            panelBtnVerInformeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnVerInforme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        formularioPaciente1.add(panelBtnVerInforme, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 90, 100, 30));

        panelBusqueda.add(formularioPaciente1, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelBusqueda);

        panelDatosPeticion.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPeticion.setMinimumSize(new java.awt.Dimension(1006, 230));
        panelDatosPeticion.setPreferredSize(new java.awt.Dimension(1006, 230));
        panelDatosPeticion.setRequestFocusEnabled(false);
        panelDatosPeticion.setLayout(new java.awt.BorderLayout());

        detallesPeticion.setBackground(new java.awt.Color(215, 232, 247));
        detallesPeticion.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detallesPeticion.setMinimumSize(new java.awt.Dimension(124, 70));
        detallesPeticion.setPreferredSize(new java.awt.Dimension(381, 51));
        detallesPeticion.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Pendientes de informar");
        detallesPeticion.add(jLabel7, java.awt.BorderLayout.WEST);

        panelDatosPeticion.add(detallesPeticion, java.awt.BorderLayout.NORTH);

        formularioPeticion.setMaximumSize(new java.awt.Dimension(2147483647, 0));
        formularioPeticion.setMinimumSize(new java.awt.Dimension(1006, 0));
        formularioPeticion.setName(""); // NOI18N
        formularioPeticion.setOpaque(false);
        formularioPeticion.setPreferredSize(new java.awt.Dimension(1006, 0));
        formularioPeticion.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(1006, 23));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(1006, 402));

        tblPendientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nº Petición", "Fecha", "Paciente", "CIP", "Prioridad", "Tipo de muestra"
            }
        ));
        tblPendientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPendientesMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblPendientes);

        formularioPeticion.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        panelDatosPeticion.add(formularioPeticion, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelDatosPeticion);

        add(panelCentral, java.awt.BorderLayout.CENTER);

        panelInferior.setBackground(new java.awt.Color(255, 255, 255));
        panelInferior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 20));
        panelInferior.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        panelBtnSalir.setBackground(new java.awt.Color(243, 245, 249));

        btnSalir.setBackground(new java.awt.Color(255, 255, 255));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSalir.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnSalir.setText("Salir");
        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSalirMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnSalirLayout = new javax.swing.GroupLayout(panelBtnSalir);
        panelBtnSalir.setLayout(panelBtnSalirLayout);
        panelBtnSalirLayout.setHorizontalGroup(
            panelBtnSalirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnSalirLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnSalirLayout.setVerticalGroup(
            panelBtnSalirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnSalirLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelInferior.add(panelBtnSalir);

        panelBtnInformar.setBackground(new java.awt.Color(75, 113, 167));

        btnInformar.setBackground(new java.awt.Color(255, 255, 255));
        btnInformar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnInformar.setForeground(new java.awt.Color(255, 255, 255));
        btnInformar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnInformar.setText("Informar");
        btnInformar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnInformarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnInformarLayout = new javax.swing.GroupLayout(panelBtnInformar);
        panelBtnInformar.setLayout(panelBtnInformarLayout);
        panelBtnInformarLayout.setHorizontalGroup(
            panelBtnInformarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnInformarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnInformar, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnInformarLayout.setVerticalGroup(
            panelBtnInformarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnInformarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnInformar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelInferior.add(panelBtnInformar);

        add(panelInferior, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void txtFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFechaActionPerformed

    private void btnSalirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSalirMouseClicked

        limpiarFormulario();
        CardLayout cl = (CardLayout) panelPrincipal.getLayout();
        cl.show(panelPrincipal, "Pantalla Inicio");
    }//GEN-LAST:event_btnSalirMouseClicked

    private void btnInformarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnInformarMouseClicked
        int fila = tblPendientes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una petición.");
            return;
        }

        int idPeticion = Integer.parseInt(
                tblPendientes.getValueAt(fila, 0).toString()
        );

        informarPeticion(idPeticion);
    }//GEN-LAST:event_btnInformarMouseClicked

    private void btnVerInformeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerInformeMouseClicked
        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
            return;
        }

        generarYAbrirInforme(peticionActual.getIdPeticion());

    }//GEN-LAST:event_btnVerInformeMouseClicked

    private void btnBuscar1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscar1MouseClicked
        String textoId = txtBuscarId.getText().trim();

        if (textoId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduce un ID de petición.");
            return;
        }

        int idPeticion;
        try {
            idPeticion = Integer.parseInt(textoId);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número.");
            return;
        }

        try {
            cargarPeticion(idPeticion);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al buscar la petición.");
        }
    }//GEN-LAST:event_btnBuscar1MouseClicked

    private void tblPendientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPendientesMouseClicked
         if (evt.getClickCount() == 2) {
            int fila = tblPendientes.getSelectedRow();

            if (fila != -1) {
                int idPeticion = Integer.parseInt(
                        tblPendientes.getValueAt(fila, 0).toString()
                );

                cargarPeticion(idPeticion);
            }
        }
    }//GEN-LAST:event_tblPendientesMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btnBuscar1;
    private javax.swing.JLabel btnInformar;
    private javax.swing.JLabel btnSalir;
    private javax.swing.JLabel btnVerInforme;
    private javax.swing.ButtonGroup buttonGroupPrioridad;
    private javax.swing.JPanel datosPaciente;
    private javax.swing.JPanel datosPaciente1;
    private javax.swing.JPanel detallesPeticion;
    private javax.swing.JPanel formularioPaciente;
    private javax.swing.JPanel formularioPaciente1;
    private javax.swing.JPanel formularioPeticion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelBtnBuscar1;
    private javax.swing.JPanel panelBtnInformar;
    private javax.swing.JPanel panelBtnSalir;
    private javax.swing.JPanel panelBtnVerInforme;
    private javax.swing.JPanel panelBusqueda;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelDatosPaciente;
    private javax.swing.JPanel panelDatosPeticion;
    private javax.swing.JPanel panelInferior;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JTable tblPendientes;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtBuscarId;
    private javax.swing.JTextField txtCip;
    private javax.swing.JTextField txtEstado;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTipoMuestra;
    // End of variables declaration//GEN-END:variables
}
