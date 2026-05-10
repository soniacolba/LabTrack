/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import app.Sesion;
import dao.PacienteDAO;
import dao.PeticionDAO;
import dao.PeticionPruebaDAO;
import dao.PruebaDAO;
import dao.TipoMuestraDAO;
import db.DB;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import model.Peticion;
import model.EnumEstadoPeticion;
import model.EnumEstadoPeticionPrueba;
import model.Paciente;
import model.PeticionPrueba;
import model.Prueba;
import model.TipoMuestra;
import util.GeneradorInformePDF;

/**
 *
 * @author leia6
 */
public class PanelGestionPeticion extends javax.swing.JPanel {

    private JPanel panelPrincipal;

    private Peticion peticionActual;
    private Paciente pacienteActual;
    private DefaultTableModel modeloTabla;
    private Map<Integer, String> mapaTiposMuestra = new HashMap<>();

    public PanelGestionPeticion(JPanel panelPrincipal) {
        this.panelPrincipal = panelPrincipal;
        initComponents();
        personalizarTabla();
        inicializarTabla();
        cargarMapaTiposMuestra();
        limpiarFormulario();
    }

    private void personalizarTabla() {
        tblPruebas.setBackground(Color.WHITE);
        tblPruebas.setForeground(Color.BLACK);
        tblPruebas.setSelectionBackground(new Color(79, 129, 189));
        tblPruebas.setSelectionForeground(Color.WHITE);
        tblPruebas.setRowHeight(22);

        tblPruebas.getTableHeader().setBackground(new Color(220, 230, 241));
        tblPruebas.getTableHeader().setForeground(Color.BLACK);
    }

    private void inicializarTabla() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Prueba", "Resultado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (peticionActual == null || column != 2) {
                    return false;
                }

                return Sesion.esTecnico()
                        && (peticionActual.getEstado() == EnumEstadoPeticion.PENDIENTE
                        || peticionActual.getEstado() == EnumEstadoPeticion.REALIZADA);
            }
        };

        tblPruebas.setModel(modeloTabla);
    }

    private void cargarMapaTiposMuestra() {
        TipoMuestraDAO dao = new TipoMuestraDAO();
        List<TipoMuestra> lista = dao.listarTodos();

        mapaTiposMuestra.clear();

        for (TipoMuestra tm : lista) {
            mapaTiposMuestra.put(tm.getId(), tm.getNombre());
        }
    }

    public void cargarPeticion(int idPeticion) {
        PeticionDAO peticionDAO = new PeticionDAO();
        PacienteDAO pacienteDAO = new PacienteDAO();

        Peticion peticion = peticionDAO.buscarPorId(idPeticion);

        if (peticion == null) {
            JOptionPane.showMessageDialog(this, "No existe ninguna petición con ese ID.");
            limpiarFormulario();
            return;
        }

        peticionActual = peticion;
        pacienteActual = pacienteDAO.buscarPorCip(peticion.getCipPaciente());

        cargarDatosPeticion();
        cargarDatosPaciente();
        cargarTablaPruebas();
        actualizarBotonesSegunEstado();
    }

    private void cargarDatosPeticion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        txtId.setText(String.valueOf(peticionActual.getIdPeticion()));
        txtFecha.setText(peticionActual.getFechaRegistro().format(formatter));
        txtEstado.setText(peticionActual.getEstado().name());

        String nombreTipoMuestra = mapaTiposMuestra.getOrDefault(
                peticionActual.getIdTipoMuestra(),
                "--"
        );
        txtTipoMuestra.setText(nombreTipoMuestra);
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

    private void cargarTablaPruebas() {
        PeticionPruebaDAO dao = new PeticionPruebaDAO();
        PruebaDAO pruebaDAO = new PruebaDAO();

        List<PeticionPrueba> lista = dao.listarPorPeticion(peticionActual.getIdPeticion());

        modeloTabla.setRowCount(0);

        for (PeticionPrueba pp : lista) {
            Prueba prueba = pruebaDAO.buscarPorId(pp.getIdPrueba());

            modeloTabla.addRow(new Object[]{
                pp.getId(),
                prueba != null ? prueba.getNombre() : "Prueba desconocida",
                pp.getResultado() != null ? pp.getResultado() : ""
            });
        }
    }

    private void actualizarBotonesSegunEstado() {
        if (peticionActual == null) {
            btnGuardar.setEnabled(false);
            btnValidar.setEnabled(false);
            btnInformar.setEnabled(false);
            btnAnular.setEnabled(false);
            return;
        }

        EnumEstadoPeticion estado = peticionActual.getEstado();

        if (Sesion.esTecnico()) {
            btnGuardar.setEnabled(
                    estado == EnumEstadoPeticion.PENDIENTE
                    || estado == EnumEstadoPeticion.REALIZADA
            );

            btnAnular.setEnabled(
                    estado == EnumEstadoPeticion.PENDIENTE
                    || estado == EnumEstadoPeticion.REALIZADA
            );

            btnValidar.setEnabled(false);
            btnInformar.setEnabled(false);
        }

        if (Sesion.esFacultativo()) {
            btnGuardar.setEnabled(false);
            btnAnular.setEnabled(false);

            btnValidar.setEnabled(estado == EnumEstadoPeticion.REALIZADA);
            btnInformar.setEnabled(estado == EnumEstadoPeticion.VALIDADA);
        }

        tblPruebas.repaint();
    }

    private boolean resultadosCompleto() {
        if (modeloTabla.getRowCount() == 0) {
            return false;
        }

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Object valor = modeloTabla.getValueAt(i, 2);

            if (valor == null || valor.toString().trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void limpiarFormulario() {
        peticionActual = null;
        pacienteActual = null;

        txtId.setText("");
        txtFecha.setText("");
        txtEstado.setText("");
        txtTipoMuestra.setText("");

        txtCip.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtFechaNacimiento.setText("");

        if (modeloTabla != null) {
            modeloTabla.setRowCount(0);
        }

        actualizarBotonesSegunEstado();
    }

    private void actualizarEstadoPeticion(Connection con) {

        boolean pruebasCompletadas = true;

        for (int i = 0; i < tblPruebas.getRowCount(); i++) {
            String resultado = (String) tblPruebas.getValueAt(i, 2);

            if (resultado == null || resultado.trim().isEmpty()) {
                pruebasCompletadas = false;
                break;
            }
        }

        PeticionDAO dao = new PeticionDAO();

        if (pruebasCompletadas) {
            dao.actualizarEstado(con, peticionActual.getIdPeticion(), EnumEstadoPeticion.REALIZADA);
        } else {
            dao.actualizarEstado(con, peticionActual.getIdPeticion(), EnumEstadoPeticion.PENDIENTE);
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
        formularioPaciente = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtFechaNacimiento = new javax.swing.JTextField();
        txtCip = new javax.swing.JTextField();
        panelDatosPeticion = new javax.swing.JPanel();
        datosPeticion = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        formularioPeticion = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtFecha = new javax.swing.JTextField();
        txtEstado = new javax.swing.JTextField();
        txtTipoMuestra = new javax.swing.JTextField();
        panelBtnInformar = new javax.swing.JPanel();
        btnInformar = new javax.swing.JLabel();
        panelBtnValidar = new javax.swing.JPanel();
        btnValidar = new javax.swing.JLabel();
        panelBtnAnular = new javax.swing.JPanel();
        btnAnular = new javax.swing.JLabel();
        panelBtnInformar1 = new javax.swing.JPanel();
        btnVerInforme = new javax.swing.JLabel();
        panelDatosPruebas = new javax.swing.JPanel();
        detallesPruebas = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        panelTabla = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblPruebas = new javax.swing.JTable();
        panelInferior = new javax.swing.JPanel();
        panelBtnSalir = new javax.swing.JPanel();
        btnSalir = new javax.swing.JLabel();
        panelBtnGuardar = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setMinimumSize(new java.awt.Dimension(1048, 840));
        setPreferredSize(new java.awt.Dimension(1048, 840));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Gestionar petición");
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

        panelDatosPeticion.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPeticion.setMaximumSize(new java.awt.Dimension(2147483647, 280));
        panelDatosPeticion.setMinimumSize(new java.awt.Dimension(956, 200));
        panelDatosPeticion.setName(""); // NOI18N
        panelDatosPeticion.setPreferredSize(new java.awt.Dimension(956, 200));
        panelDatosPeticion.setLayout(new java.awt.BorderLayout());

        datosPeticion.setBackground(new java.awt.Color(215, 232, 247));
        datosPeticion.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        datosPeticion.setMinimumSize(new java.awt.Dimension(124, 70));
        datosPeticion.setLayout(new java.awt.BorderLayout());

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Datos de la petición");
        datosPeticion.add(jLabel8, java.awt.BorderLayout.WEST);

        panelDatosPeticion.add(datosPeticion, java.awt.BorderLayout.NORTH);

        formularioPeticion.setMaximumSize(new java.awt.Dimension(1006, 160));
        formularioPeticion.setMinimumSize(new java.awt.Dimension(1006, 160));
        formularioPeticion.setOpaque(false);
        formularioPeticion.setPreferredSize(new java.awt.Dimension(1006, 160));
        formularioPeticion.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Estado");
        formularioPeticion.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Fecha y hora");
        formularioPeticion.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel16.setText("ID");
        formularioPeticion.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel11.setText("Tipo de muestra");
        formularioPeticion.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(255, 255, 255));
        txtId.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPeticion.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 70, 29));

        txtFecha.setEditable(false);
        txtFecha.setBackground(new java.awt.Color(255, 255, 255));
        txtFecha.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFechaActionPerformed(evt);
            }
        });
        formularioPeticion.add(txtFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 220, 29));

        txtEstado.setEditable(false);
        txtEstado.setBackground(new java.awt.Color(255, 255, 255));
        txtEstado.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPeticion.add(txtEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 30, 180, 29));

        txtTipoMuestra.setEditable(false);
        txtTipoMuestra.setBackground(new java.awt.Color(255, 255, 255));
        txtTipoMuestra.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPeticion.add(txtTipoMuestra, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 90, 360, 29));

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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnInformarMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout panelBtnInformarLayout = new javax.swing.GroupLayout(panelBtnInformar);
        panelBtnInformar.setLayout(panelBtnInformarLayout);
        panelBtnInformarLayout.setHorizontalGroup(
            panelBtnInformarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBtnInformarLayout.createSequentialGroup()
                .addComponent(btnInformar, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelBtnInformarLayout.setVerticalGroup(
            panelBtnInformarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnInformar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        formularioPeticion.add(panelBtnInformar, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 90, 100, 30));

        panelBtnValidar.setBackground(new java.awt.Color(75, 113, 167));

        btnValidar.setBackground(new java.awt.Color(255, 255, 255));
        btnValidar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnValidar.setForeground(new java.awt.Color(255, 255, 255));
        btnValidar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnValidar.setText("Validar");
        btnValidar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnValidarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnValidarMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout panelBtnValidarLayout = new javax.swing.GroupLayout(panelBtnValidar);
        panelBtnValidar.setLayout(panelBtnValidarLayout);
        panelBtnValidarLayout.setHorizontalGroup(
            panelBtnValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBtnValidarLayout.createSequentialGroup()
                .addComponent(btnValidar, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelBtnValidarLayout.setVerticalGroup(
            panelBtnValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnValidar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        formularioPeticion.add(panelBtnValidar, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 90, 100, 30));

        panelBtnAnular.setBackground(new java.awt.Color(75, 113, 167));

        btnAnular.setBackground(new java.awt.Color(255, 255, 255));
        btnAnular.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnAnular.setForeground(new java.awt.Color(255, 255, 255));
        btnAnular.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnAnular.setText("Anular ");
        btnAnular.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAnularMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAnularMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout panelBtnAnularLayout = new javax.swing.GroupLayout(panelBtnAnular);
        panelBtnAnular.setLayout(panelBtnAnularLayout);
        panelBtnAnularLayout.setHorizontalGroup(
            panelBtnAnularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBtnAnularLayout.createSequentialGroup()
                .addComponent(btnAnular, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelBtnAnularLayout.setVerticalGroup(
            panelBtnAnularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnAnular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        formularioPeticion.add(panelBtnAnular, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 90, 100, 30));

        panelBtnInformar1.setBackground(new java.awt.Color(75, 113, 167));

        btnVerInforme.setBackground(new java.awt.Color(255, 255, 255));
        btnVerInforme.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnVerInforme.setForeground(new java.awt.Color(255, 255, 255));
        btnVerInforme.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnVerInforme.setText("Ver informe PDF");
        btnVerInforme.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnVerInformeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnVerInformeMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout panelBtnInformar1Layout = new javax.swing.GroupLayout(panelBtnInformar1);
        panelBtnInformar1.setLayout(panelBtnInformar1Layout);
        panelBtnInformar1Layout.setHorizontalGroup(
            panelBtnInformar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnVerInforme, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        panelBtnInformar1Layout.setVerticalGroup(
            panelBtnInformar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnVerInforme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        formularioPeticion.add(panelBtnInformar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 30, 140, 30));

        panelDatosPeticion.add(formularioPeticion, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelDatosPeticion);

        panelDatosPruebas.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPruebas.setMinimumSize(new java.awt.Dimension(1006, 230));
        panelDatosPruebas.setPreferredSize(new java.awt.Dimension(1006, 230));
        panelDatosPruebas.setRequestFocusEnabled(false);
        panelDatosPruebas.setLayout(new java.awt.BorderLayout());

        detallesPruebas.setBackground(new java.awt.Color(215, 232, 247));
        detallesPruebas.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detallesPruebas.setMinimumSize(new java.awt.Dimension(124, 70));
        detallesPruebas.setPreferredSize(new java.awt.Dimension(381, 51));
        detallesPruebas.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Pruebas");
        detallesPruebas.add(jLabel7, java.awt.BorderLayout.WEST);

        panelDatosPruebas.add(detallesPruebas, java.awt.BorderLayout.NORTH);

        panelTabla.setMaximumSize(new java.awt.Dimension(2147483647, 0));
        panelTabla.setMinimumSize(new java.awt.Dimension(1006, 0));
        panelTabla.setName(""); // NOI18N
        panelTabla.setOpaque(false);
        panelTabla.setPreferredSize(new java.awt.Dimension(1006, 0));
        panelTabla.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(1006, 23));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(1006, 402));

        tblPruebas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tblPruebas);

        panelTabla.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        panelDatosPruebas.add(panelTabla, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelDatosPruebas);

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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSalirMouseEntered(evt);
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

        panelBtnGuardar.setBackground(new java.awt.Color(75, 113, 167));

        btnGuardar.setBackground(new java.awt.Color(255, 255, 255));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnGuardar.setText("Guardar");
        btnGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGuardarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGuardarMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout panelBtnGuardarLayout = new javax.swing.GroupLayout(panelBtnGuardar);
        panelBtnGuardar.setLayout(panelBtnGuardarLayout);
        panelBtnGuardarLayout.setHorizontalGroup(
            panelBtnGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnGuardarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnGuardarLayout.setVerticalGroup(
            panelBtnGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnGuardarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelInferior.add(panelBtnGuardar);

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

    private void btnGuardarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseClicked
        if (!btnGuardar.isEnabled()) {
            return;
        }
        
        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "No hay ninguna petición cargada.");
            return;
        }

        if (peticionActual.getEstado() != EnumEstadoPeticion.PENDIENTE
                && peticionActual.getEstado() != EnumEstadoPeticion.REALIZADA) {
            JOptionPane.showMessageDialog(this, "Esta petición ya no permite modificar resultados.");
            return;
        }

        Connection con = null;

        try {
            con = DB.getConnection();
            con.setAutoCommit(false);

            for (int i = 0; i < tblPruebas.getRowCount(); i++) {

                int idPP = Integer.parseInt(tblPruebas.getValueAt(i, 0).toString());

                Object valor = tblPruebas.getValueAt(i, 2);
                String resultado = valor != null ? valor.toString().trim() : null;

                if (resultado != null && resultado.isEmpty()) {
                    resultado = null;
                }

                String estado = resultado != null
                        ? EnumEstadoPeticionPrueba.REALIZADA.name()
                        : EnumEstadoPeticionPrueba.PENDIENTE.name();

                String sql = "UPDATE peticion_prueba SET resultado = ?, estado = ? WHERE id = ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, resultado);
                    ps.setString(2, estado);
                    ps.setInt(3, idPP);
                    ps.executeUpdate();
                }
            }

            actualizarEstadoPeticion(con);

            con.commit();

            JOptionPane.showMessageDialog(this, "Resultados guardados correctamente");

            cargarPeticion(peticionActual.getIdPeticion());

        } catch (Exception e) {
            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(this, "Error al guardar resultados");

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_btnGuardarMouseClicked

    private void btnValidarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnValidarMouseClicked
        if (!btnValidar.isEnabled()) {
            return;
        }

        if (peticionActual == null) {
            return;
        }

        if (peticionActual.getEstado() != EnumEstadoPeticion.REALIZADA) {
            JOptionPane.showMessageDialog(this,
                    "Solo se pueden validar peticiones realizadas.");
            return;
        }

        if (!resultadosCompleto()) {
            JOptionPane.showMessageDialog(this,
                    "No se pueden validar peticiones con resultados incompletos.");
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas validar la petición?",
                "Confirmar validación",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = DB.getConnection()) {

            PeticionDAO dao = new PeticionDAO();

            boolean ok = dao.actualizarEstado(
                    con,
                    peticionActual.getIdPeticion(),
                    EnumEstadoPeticion.VALIDADA
            );

            if (ok) {
                JOptionPane.showMessageDialog(this, "Petición validada correctamente");

                cargarPeticion(peticionActual.getIdPeticion());
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo validar la petición");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al validar la petición");
        }
    }//GEN-LAST:event_btnValidarMouseClicked

    private void btnAnularMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAnularMouseClicked
        if (!btnAnular.isEnabled()) {
            return;
        }

        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
            return;
        }

        if (peticionActual.getEstado() != EnumEstadoPeticion.PENDIENTE
                && peticionActual.getEstado() != EnumEstadoPeticion.REALIZADA) {
            JOptionPane.showMessageDialog(this,
                    "Solo se pueden anular peticiones pendientes o realizadas.");
            return;
        }

        DialogoAnularPeticion dialogo = new DialogoAnularPeticion(null, true);
        dialogo.setVisible(true);

        String motivo = dialogo.getMotivoSeleccionado();

        if (motivo == null) {
            return;
        }

        Connection con = null;

        try {
            con = DB.getConnection();
            con.setAutoCommit(false);

            PeticionDAO peticionDAO = new PeticionDAO();
            PeticionPruebaDAO ppDAO = new PeticionPruebaDAO();

            boolean peticionOk = peticionDAO.actualizarEstado(
                    con,
                    peticionActual.getIdPeticion(),
                    EnumEstadoPeticion.ANULADA
            );

            if (!peticionOk) {
                throw new Exception("No se pudo anular la petición.");
            }

            boolean pruebasOk = ppDAO.anularPorPeticion(
                    con,
                    peticionActual.getIdPeticion(),
                    motivo
            );

            if (!pruebasOk) {
                throw new Exception("No se pudieron anular las pruebas.");
            }

            con.commit();

            JOptionPane.showMessageDialog(this, "Petición anulada correctamente.");

            cargarPeticion(peticionActual.getIdPeticion());

    } catch (Exception e) {
        e.printStackTrace();

        if (con != null) {
            try {
                con.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(this, "Error al anular la petición.");

    } finally {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    }//GEN-LAST:event_btnAnularMouseClicked

    private void btnInformarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnInformarMouseClicked
        if (!btnInformar.isEnabled()) {
            return;
        }
        
        if (peticionActual == null) {
            return;
        }

        if (peticionActual.getEstado() != EnumEstadoPeticion.VALIDADA) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden informar peticiones validadas.");
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que deseas informar esta petición?",
            "Confirmar informe",
            JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean generado = GeneradorInformePDF.generarInforme(peticionActual.getIdPeticion());

        if (!generado) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el informe");
            return;
        }

        try {
            String ruta = "informes/peticion_" + peticionActual.getIdPeticion() + ".pdf";
            java.awt.Desktop.getDesktop().open(new java.io.File(ruta));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection con = DB.getConnection()) {

            PeticionDAO dao = new PeticionDAO();

            boolean ok = dao.actualizarEstado(
                con,
                peticionActual.getIdPeticion(),
                EnumEstadoPeticion.INFORMADA
            );

            if (ok) {
                JOptionPane.showMessageDialog(this, "Petición informada correctamente.");
                cargarPeticion(peticionActual.getIdPeticion());
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo informar la petición.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al informar la petición.");
        }
    }//GEN-LAST:event_btnInformarMouseClicked

    private void btnVerInformeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerInformeMouseClicked
        if (!btnVerInforme.isEnabled()) {
            return;
        }
        
        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
            return;
        }

        generarYAbrirInforme(peticionActual.getIdPeticion());
    }//GEN-LAST:event_btnVerInformeMouseClicked

    private void btnVerInformeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerInformeMouseEntered
        btnVerInforme.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnVerInformeMouseEntered

    private void btnValidarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnValidarMouseEntered
        btnValidar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnValidarMouseEntered

    private void btnInformarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnInformarMouseEntered
        btnInformar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnInformarMouseEntered

    private void btnAnularMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAnularMouseEntered
        btnAnular.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnAnularMouseEntered

    private void btnGuardarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseEntered
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnGuardarMouseEntered

    private void btnSalirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSalirMouseEntered
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_btnSalirMouseEntered


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btnAnular;
    private javax.swing.JLabel btnGuardar;
    private javax.swing.JLabel btnInformar;
    private javax.swing.JLabel btnSalir;
    private javax.swing.JLabel btnValidar;
    private javax.swing.JLabel btnVerInforme;
    private javax.swing.ButtonGroup buttonGroupPrioridad;
    private javax.swing.JPanel datosPaciente;
    private javax.swing.JPanel datosPeticion;
    private javax.swing.JPanel detallesPruebas;
    private javax.swing.JPanel formularioPaciente;
    private javax.swing.JPanel formularioPeticion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelBtnAnular;
    private javax.swing.JPanel panelBtnGuardar;
    private javax.swing.JPanel panelBtnInformar;
    private javax.swing.JPanel panelBtnInformar1;
    private javax.swing.JPanel panelBtnSalir;
    private javax.swing.JPanel panelBtnValidar;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelDatosPaciente;
    private javax.swing.JPanel panelDatosPeticion;
    private javax.swing.JPanel panelDatosPruebas;
    private javax.swing.JPanel panelInferior;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JTable tblPruebas;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtCip;
    private javax.swing.JTextField txtEstado;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTipoMuestra;
    // End of variables declaration//GEN-END:variables
}
