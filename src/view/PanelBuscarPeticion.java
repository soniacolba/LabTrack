/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;


import dao.PacienteDAO;
import dao.PeticionDAO;
import dao.PeticionPruebaDAO;
import dao.PruebaDAO;
import dao.TipoMuestraDAO;
import db.DB;
import java.awt.CardLayout;
import java.awt.Color;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.EnumEstadoPeticionPrueba;
import model.EnumPrioridad;
import model.Paciente;
import model.Peticion;
import model.PeticionPrueba;
import model.Prueba;
import model.TipoMuestra;
import model.EnumEstadoPeticion;


/**
 *
 * @author leia6
 */
public class PanelBuscarPeticion extends javax.swing.JPanel {

    private JPanel panelPrincipal;
    private PanelGestionPeticion pnlGestionPeticion;

    private DefaultListModel<Prueba> modeloDisponibles = new DefaultListModel<>();
    private DefaultListModel<Prueba> modeloAnadidas = new DefaultListModel<>();
    
    private List<Prueba> listadoPruebas = new ArrayList<>();
    
    private int indiceTipoMuestra = 0;
    private boolean limpiar = false;
    private Peticion peticionActual;



    public PanelBuscarPeticion(JPanel panelPrincipal, PanelGestionPeticion pnlGestionPeticion) {
        this.panelPrincipal = panelPrincipal;
        this.pnlGestionPeticion = pnlGestionPeticion;
        initComponents();
        cargarTiposMuestra();
        lstPruebasDisponibles.setModel(modeloDisponibles);
        lstPruebasAnadidas.setModel(modeloAnadidas);
        habilitarDetallesPeticion(false);

    }

    private void cargarTiposMuestra() {

        TipoMuestraDAO dao = new TipoMuestraDAO();
        List<TipoMuestra> lista = dao.listarTodos();

        comboTipoMuestra.removeAllItems();
        comboTipoMuestra.addItem(new TipoMuestra(0, "--Selecciona tipo de muestra--"));

        for (TipoMuestra t : lista) {
            comboTipoMuestra.addItem(t);
        }
    }

    private void cargarPruebasDisponibles() {
        modeloDisponibles.clear();

        TipoMuestra tipo = (TipoMuestra) comboTipoMuestra.getSelectedItem();

        if (tipo == null || tipo.getId() == 0) {
            return;
        }

        PruebaDAO dao = new PruebaDAO();
        List<Prueba> lista = dao.listarPorTipoMuestra(tipo.getId());

        listadoPruebas = new ArrayList<>(lista);

        for (Prueba p : lista) {
            if (!modeloAnadidas.contains(p)) {
                modeloDisponibles.addElement(p);
            }
        }
    }
    

    private void habilitarDetallesPeticion(boolean habilitar) {
        comboTipoMuestra.setEnabled(habilitar);
        rBPrioridadNormal.setEnabled(habilitar);
        rBPrioridadUrgente.setEnabled(habilitar);
        txtBuscarPrueba.setEnabled(habilitar);
        lstPruebasDisponibles.setEnabled(habilitar);
        lstPruebasAnadidas.setEnabled(habilitar);
        btnAgregar.setEnabled(habilitar);
        btnQuitar.setEnabled(habilitar);
        btnGuardar.setEnabled(habilitar);
    }

    private void filtrarPruebas() {

        String texto = txtBuscarPrueba.getText().toLowerCase().trim();

        if (texto.equals("buscar prueba...")) {
            texto = "";
        }
        modeloDisponibles.clear();

        for (Prueba p : listadoPruebas) {
            if (!modeloAnadidas.contains(p) && p.getNombre().toLowerCase().contains(texto)) {
                modeloDisponibles.addElement(p);
            }
        }
    }

    private void limpiarFormulario() {

        limpiar = true;

        peticionActual = null;
        
        txtBuscarId.setText("");
        txtId.setText("");
        txtFecha.setText("");
        txtEstado.setText("");

        txtCip.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtFechaNacimiento.setText("");

        comboTipoMuestra.setSelectedIndex(0);
        rBPrioridadNormal.setSelected(true);

        txtBuscarPrueba.setText("Buscar prueba...");
        txtBuscarPrueba.setForeground(Color.GRAY);

        modeloDisponibles.clear();
        modeloAnadidas.clear();
        listadoPruebas.clear();

        indiceTipoMuestra = 0;

        habilitarDetallesPeticion(false);

        limpiar = false;
    }

    public void ordenAlfabeticoListas(DefaultListModel<Prueba> modelo, Prueba prueba) {
        String nombrePrueba = prueba.getNombre().toLowerCase();

        int i = 0;
        while (i < modelo.size() && modelo.getElementAt(i).getNombre().toLowerCase().compareTo(nombrePrueba) < 0) {
            i++;
        }

        modelo.add(i, prueba);
    }

    private void seleccionarTipoMuestraPorId(int idTipoMuestra) {
        for (int i = 0; i < comboTipoMuestra.getItemCount(); i++) {
            TipoMuestra tm = comboTipoMuestra.getItemAt(i);
            if (tm.getId() == idTipoMuestra) {
                comboTipoMuestra.setSelectedIndex(i);
                break;
            }
        }
    }

    private void cargarPeticion(int idPeticion) {
        PeticionDAO peticionDAO = new PeticionDAO();
        PacienteDAO pacienteDAO = new PacienteDAO();
        PeticionPruebaDAO peticionPruebaDAO = new PeticionPruebaDAO();

        Peticion peticion = peticionDAO.buscarPorId(idPeticion);

        if (peticion == null) {
            JOptionPane.showMessageDialog(this, "No existe ninguna petición con ese ID.");
            limpiarFormulario();
            return;
        }

        limpiar = true;

        this.peticionActual = peticion;

        boolean editable = peticion.getEstado() == EnumEstadoPeticion.PENDIENTE;
        habilitarDetallesPeticion(editable);
        btnAnular.setEnabled(peticion.getEstado() == EnumEstadoPeticion.PENDIENTE || peticion.getEstado() == EnumEstadoPeticion.REALIZADA);

        txtId.setText(String.valueOf(peticion.getIdPeticion()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        txtFecha.setText(peticion.getFechaRegistro().format(formatter));
        txtEstado.setText(peticion.getEstado().name());

        if (peticion.getPrioridad() == EnumPrioridad.URGENTE) {
            rBPrioridadUrgente.setSelected(true);
        } else {
            rBPrioridadNormal.setSelected(true);
        }

        Paciente paciente = pacienteDAO.buscarPorCip(peticion.getCipPaciente());
        if (paciente != null) {
            txtApellidos.setText(paciente.getApellidos());
            txtNombre.setText(paciente.getNombre());
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            txtFechaNacimiento.setText(paciente.getFechaNacimiento().format(formatter2));
            txtCip.setText(paciente.getCip());
        }

        seleccionarTipoMuestraPorId(peticion.getIdTipoMuestra());

        modeloAnadidas.clear();
        List<Prueba> pruebasPeticion = peticionPruebaDAO.listarPruebasDePeticion(idPeticion);

        for (Prueba p : pruebasPeticion) {
            modeloAnadidas.addElement(p);
        }

        cargarPruebasDisponibles();

        indiceTipoMuestra = comboTipoMuestra.getSelectedIndex();
        limpiar = false;

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
        panelBusqueda = new javax.swing.JPanel();
        datosPaciente1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        panelBtnBuscar1 = new javax.swing.JPanel();
        btnBuscar1 = new javax.swing.JLabel();
        txtBuscarId = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        formularioPaciente1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtFecha = new javax.swing.JTextField();
        txtEstado = new javax.swing.JTextField();
        panelBtnAnular1 = new javax.swing.JPanel();
        btnAnular1 = new javax.swing.JLabel();
        panelBtnAnular = new javax.swing.JPanel();
        btnAnular = new javax.swing.JLabel();
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
        detallesPeticion = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        formularioPeticion = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        comboTipoMuestra = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstPruebasDisponibles = new javax.swing.JList<>();
        txtBuscarPrueba = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstPruebasAnadidas = new javax.swing.JList<>();
        jLabel13 = new javax.swing.JLabel();
        rBPrioridadNormal = new javax.swing.JRadioButton();
        rBPrioridadUrgente = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        panelBtnAgregar = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JLabel();
        panelBtnQuitar = new javax.swing.JPanel();
        btnQuitar = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        panelInferior = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        panelBtnCancelar = new javax.swing.JPanel();
        btnCancelar = new javax.swing.JLabel();
        panelBtnGuardar = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JLabel();

        jLabel15.setText("jLabel15");

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Buscar Petición");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelSuperior.add(jLabel1, java.awt.BorderLayout.CENTER);

        add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        panelCentral.setBackground(new java.awt.Color(255, 255, 255));
        panelCentral.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 1));
        panelCentral.setMinimumSize(new java.awt.Dimension(0, 0));
        panelCentral.setPreferredSize(new java.awt.Dimension(0, 0));
        panelCentral.setLayout(new javax.swing.BoxLayout(panelCentral, javax.swing.BoxLayout.Y_AXIS));

        panelBusqueda.setBackground(new java.awt.Color(243, 245, 249));
        panelBusqueda.setMaximumSize(new java.awt.Dimension(2147483647, 280));
        panelBusqueda.setMinimumSize(new java.awt.Dimension(1220, 140));
        panelBusqueda.setName(""); // NOI18N
        panelBusqueda.setPreferredSize(new java.awt.Dimension(1220, 140));
        panelBusqueda.setLayout(new java.awt.BorderLayout());

        datosPaciente1.setBackground(new java.awt.Color(215, 232, 247));
        datosPaciente1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        datosPaciente1.setMinimumSize(new java.awt.Dimension(124, 70));
        datosPaciente1.setLayout(new java.awt.BorderLayout());

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Datos de la petición");
        datosPaciente1.add(jLabel8, java.awt.BorderLayout.WEST);

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

        datosPaciente1.add(jPanel7, java.awt.BorderLayout.LINE_END);

        panelBusqueda.add(datosPaciente1, java.awt.BorderLayout.NORTH);

        formularioPaciente1.setMaximumSize(new java.awt.Dimension(1206, 160));
        formularioPaciente1.setMinimumSize(new java.awt.Dimension(1206, 160));
        formularioPaciente1.setOpaque(false);
        formularioPaciente1.setPreferredSize(new java.awt.Dimension(1206, 160));
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

        panelBtnAnular1.setBackground(new java.awt.Color(75, 113, 167));

        btnAnular1.setBackground(new java.awt.Color(255, 255, 255));
        btnAnular1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnAnular1.setForeground(new java.awt.Color(255, 255, 255));
        btnAnular1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnAnular1.setText("Gestionar petición");
        btnAnular1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAnular1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnAnular1Layout = new javax.swing.GroupLayout(panelBtnAnular1);
        panelBtnAnular1.setLayout(panelBtnAnular1Layout);
        panelBtnAnular1Layout.setHorizontalGroup(
            panelBtnAnular1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
            .addGroup(panelBtnAnular1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnAnular1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
        );
        panelBtnAnular1Layout.setVerticalGroup(
            panelBtnAnular1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
            .addGroup(panelBtnAnular1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnAnular1, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
        );

        formularioPaciente1.add(panelBtnAnular1, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 30, 150, 30));

        panelBtnAnular.setBackground(new java.awt.Color(75, 113, 167));

        btnAnular.setBackground(new java.awt.Color(255, 255, 255));
        btnAnular.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnAnular.setForeground(new java.awt.Color(255, 255, 255));
        btnAnular.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnAnular.setText("Anular petición");
        btnAnular.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAnularMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnAnularLayout = new javax.swing.GroupLayout(panelBtnAnular);
        panelBtnAnular.setLayout(panelBtnAnularLayout);
        panelBtnAnularLayout.setHorizontalGroup(
            panelBtnAnularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnAnular, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        panelBtnAnularLayout.setVerticalGroup(
            panelBtnAnularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnAnular, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        formularioPaciente1.add(panelBtnAnular, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 30, 150, 30));

        panelBusqueda.add(formularioPaciente1, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelBusqueda);

        panelDatosPaciente.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPaciente.setMaximumSize(new java.awt.Dimension(2147483647, 170));
        panelDatosPaciente.setMinimumSize(new java.awt.Dimension(0, 0));
        panelDatosPaciente.setName(""); // NOI18N
        panelDatosPaciente.setPreferredSize(new java.awt.Dimension(956, 170));
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

        formularioPaciente.setMaximumSize(new java.awt.Dimension(1306, 90));
        formularioPaciente.setMinimumSize(new java.awt.Dimension(1306, 90));
        formularioPaciente.setOpaque(false);
        formularioPaciente.setPreferredSize(new java.awt.Dimension(1306, 90));
        formularioPaciente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setText("CIP");
        formularioPaciente.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 30, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("Nombre");
        formularioPaciente.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(364, 31, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Fecha de nacimiento");
        formularioPaciente.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(718, 31, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Apellidos");
        formularioPaciente.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 31, -1, -1));

        txtApellidos.setEditable(false);
        txtApellidos.setBackground(new java.awt.Color(255, 255, 255));
        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 336, 29));

        txtNombre.setEditable(false);
        txtNombre.setBackground(new java.awt.Color(255, 255, 255));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(364, 59, 336, 29));

        txtFechaNacimiento.setEditable(false);
        txtFechaNacimiento.setBackground(new java.awt.Color(255, 255, 255));
        txtFechaNacimiento.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtFechaNacimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(718, 59, 237, 29));

        txtCip.setEditable(false);
        txtCip.setBackground(new java.awt.Color(255, 255, 255));
        txtCip.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        formularioPaciente.add(txtCip, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 60, 300, 29));

        panelDatosPaciente.add(formularioPaciente, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelDatosPaciente);

        panelDatosPeticion.setBackground(new java.awt.Color(243, 245, 249));
        panelDatosPeticion.setMinimumSize(new java.awt.Dimension(0, 0));
        panelDatosPeticion.setPreferredSize(new java.awt.Dimension(0, 0));
        panelDatosPeticion.setRequestFocusEnabled(false);
        panelDatosPeticion.setLayout(new java.awt.BorderLayout());

        detallesPeticion.setBackground(new java.awt.Color(215, 232, 247));
        detallesPeticion.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detallesPeticion.setMinimumSize(new java.awt.Dimension(124, 70));
        detallesPeticion.setPreferredSize(new java.awt.Dimension(381, 51));
        detallesPeticion.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Detalles de la petición");
        detallesPeticion.add(jLabel7, java.awt.BorderLayout.WEST);

        panelDatosPeticion.add(detallesPeticion, java.awt.BorderLayout.NORTH);

        formularioPeticion.setMaximumSize(new java.awt.Dimension(2147483647, 0));
        formularioPeticion.setMinimumSize(new java.awt.Dimension(1006, 480));
        formularioPeticion.setName(""); // NOI18N
        formularioPeticion.setOpaque(false);
        formularioPeticion.setPreferredSize(new java.awt.Dimension(1006, 480));
        formularioPeticion.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel11.setText("Pruebas añadidas:");
        formularioPeticion.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 110, -1, -1));

        comboTipoMuestra.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        comboTipoMuestra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboTipoMuestraActionPerformed(evt);
            }
        });
        formularioPeticion.add(comboTipoMuestra, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 410, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel12.setText("Prioridad:");
        formularioPeticion.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, -1, -1));

        lstPruebasDisponibles.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lstPruebasDisponibles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstPruebasDisponiblesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstPruebasDisponibles);

        formularioPeticion.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 460, 200));

        txtBuscarPrueba.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtBuscarPrueba.setForeground(new java.awt.Color(204, 204, 204));
        txtBuscarPrueba.setText("Buscar prueba...");
        txtBuscarPrueba.setToolTipText("");
        txtBuscarPrueba.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtBuscarPruebaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBuscarPruebaFocusLost(evt);
            }
        });
        txtBuscarPrueba.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarPruebaKeyReleased(evt);
            }
        });
        formularioPeticion.add(txtBuscarPrueba, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 460, 30));

        lstPruebasAnadidas.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lstPruebasAnadidas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstPruebasAnadidasMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(lstPruebasAnadidas);

        formularioPeticion.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 140, 420, 220));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel13.setText("Tipo de muestra:");
        formularioPeticion.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        buttonGroupPrioridad.add(rBPrioridadNormal);
        rBPrioridadNormal.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        rBPrioridadNormal.setSelected(true);
        rBPrioridadNormal.setText("Normal");
        rBPrioridadNormal.setOpaque(false);
        formularioPeticion.add(rBPrioridadNormal, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 50, -1, -1));

        buttonGroupPrioridad.add(rBPrioridadUrgente);
        rBPrioridadUrgente.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        rBPrioridadUrgente.setText("Urgente");
        rBPrioridadUrgente.setOpaque(false);
        rBPrioridadUrgente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rBPrioridadUrgenteActionPerformed(evt);
            }
        });
        formularioPeticion.add(rBPrioridadUrgente, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 50, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel14.setText("Pruebas disponibles:");
        formularioPeticion.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        panelBtnAgregar.setBackground(new java.awt.Color(255, 255, 255));
        panelBtnAgregar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        panelBtnAgregar.setName(""); // NOI18N

        btnAgregar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/arrow_right.png"))); // NOI18N
        btnAgregar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAgregarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnAgregarLayout = new javax.swing.GroupLayout(panelBtnAgregar);
        panelBtnAgregar.setLayout(panelBtnAgregarLayout);
        panelBtnAgregarLayout.setHorizontalGroup(
            panelBtnAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnAgregarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnAgregarLayout.setVerticalGroup(
            panelBtnAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnAgregarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        formularioPeticion.add(panelBtnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 210, -1, -1));

        panelBtnQuitar.setBackground(new java.awt.Color(255, 255, 255));
        panelBtnQuitar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        panelBtnQuitar.setName(""); // NOI18N

        btnQuitar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnQuitar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/arrow_left.png"))); // NOI18N
        btnQuitar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnQuitarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnQuitarLayout = new javax.swing.GroupLayout(panelBtnQuitar);
        panelBtnQuitar.setLayout(panelBtnQuitarLayout);
        panelBtnQuitarLayout.setHorizontalGroup(
            panelBtnQuitarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnQuitarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnQuitar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnQuitarLayout.setVerticalGroup(
            panelBtnQuitarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnQuitarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnQuitar))
        );

        formularioPeticion.add(panelBtnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 250, -1, -1));

        jSeparator2.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        formularioPeticion.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 100, -1, 260));

        panelDatosPeticion.add(formularioPeticion, java.awt.BorderLayout.LINE_START);

        panelCentral.add(panelDatosPeticion);

        add(panelCentral, java.awt.BorderLayout.CENTER);

        panelInferior.setBackground(new java.awt.Color(255, 255, 255));
        panelInferior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 20));
        panelInferior.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        panelInferior.add(filler1);

        panelBtnCancelar.setBackground(new java.awt.Color(243, 245, 249));

        btnCancelar.setBackground(new java.awt.Color(255, 255, 255));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnCancelar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnCancelar.setText("Cancelar");
        btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelarMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBtnCancelarLayout = new javax.swing.GroupLayout(panelBtnCancelar);
        panelBtnCancelar.setLayout(panelBtnCancelarLayout);
        panelBtnCancelarLayout.setHorizontalGroup(
            panelBtnCancelarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnCancelarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBtnCancelarLayout.setVerticalGroup(
            panelBtnCancelarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBtnCancelarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelInferior.add(panelBtnCancelar);

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

    private void btnGuardarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseClicked
        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
            return;
        }

        if (peticionActual.getEstado() != EnumEstadoPeticion.PENDIENTE) {
            JOptionPane.showMessageDialog(this, "Solo se pueden modificar peticiones en estado PENDIENTE.");
            return;
        }

        TipoMuestra tipoMuestra = (TipoMuestra) comboTipoMuestra.getSelectedItem();
        if (tipoMuestra == null || tipoMuestra.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un tipo de muestra.");
            return;
        }

        if (modeloAnadidas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes añadir al menos una prueba.");
            return;
        }

        EnumPrioridad prioridad = rBPrioridadUrgente.isSelected()
                ? EnumPrioridad.URGENTE
                : EnumPrioridad.NORMAL;

        Peticion peticionEditada = new Peticion(
                peticionActual.getIdPeticion(),
                peticionActual.getFechaRegistro(),
                prioridad,
                peticionActual.getEstado(),
                peticionActual.getCipPaciente(),
                peticionActual.getIdUsuario(),
                tipoMuestra.getId()
        );

        Connection con = null;

        try {
            con = DB.getConnection();
            con.setAutoCommit(false);

            PeticionDAO peticionDAO = new PeticionDAO();
            PeticionPruebaDAO peticionPruebaDAO = new PeticionPruebaDAO();

            boolean actualizada = peticionDAO.actualizar(con, peticionEditada);
            if (!actualizada) {
                throw new Exception("No se pudo actualizar la petición.");
            }

            boolean borradas = peticionPruebaDAO.borrarPorPeticion(con, peticionActual.getIdPeticion());
            if (!borradas) {
                throw new Exception("No se pudieron borrar las pruebas antiguas.");
            }

            for (int i = 0; i < modeloAnadidas.size(); i++) {
                Prueba prueba = modeloAnadidas.getElementAt(i);

                PeticionPrueba pp = new PeticionPrueba(
                        0,
                        EnumEstadoPeticionPrueba.PENDIENTE,
                        null,
                        peticionActual.getIdPeticion(),
                        prueba.getId()
                );

                boolean insertada = peticionPruebaDAO.insertar(con, pp);
                if (!insertada) {
                    throw new Exception("No se pudo insertar una prueba.");
                }
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Petición actualizada correctamente.");

            limpiarFormulario();

            CardLayout cl = (CardLayout) panelPrincipal.getLayout();
            cl.show(panelPrincipal, "Pantalla Inicio");

        } catch (Exception e) {
            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(this, "Error al actualizar la petición.");

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

    private void btnCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelarMouseClicked
        if (peticionActual != null
                && peticionActual.getEstado() != EnumEstadoPeticion.PENDIENTE) {

            limpiarFormulario();

            CardLayout cl = (CardLayout) panelPrincipal.getLayout();
            cl.show(panelPrincipal, "Pantalla Inicio");
            return;
        }

        boolean hayDatos = !txtCip.getText().trim().isEmpty()
                || !txtNombre.getText().trim().isEmpty()
                || !txtApellidos.getText().trim().isEmpty()
                || !txtFechaNacimiento.getText().trim().isEmpty()
                || comboTipoMuestra.getSelectedIndex() > 0
                || !modeloAnadidas.isEmpty();

        if (hayDatos) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "Se perderán los datos introducidos. ¿Deseas cancelar?",
                    "Confirmar cancelación",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        limpiarFormulario();

        CardLayout cl = (CardLayout) panelPrincipal.getLayout();
        cl.show(panelPrincipal, "Pantalla Inicio");
    }//GEN-LAST:event_btnCancelarMouseClicked

    private void rBPrioridadUrgenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rBPrioridadUrgenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rBPrioridadUrgenteActionPerformed

    private void comboTipoMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboTipoMuestraActionPerformed

        if (limpiar) {
            return;
        }

        if (!modeloAnadidas.isEmpty()) {

            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "Se eliminarán las pruebas añadidas. ¿Deseas continuar?",
                    "Cambiar tipo de muestra",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcion == JOptionPane.NO_OPTION) {
                limpiar = true;
                comboTipoMuestra.setSelectedIndex(indiceTipoMuestra);
                limpiar = false;
                return;
            }

            modeloAnadidas.clear();
        }

        indiceTipoMuestra = comboTipoMuestra.getSelectedIndex();
        cargarPruebasDisponibles();
    }//GEN-LAST:event_comboTipoMuestraActionPerformed

    private void txtBuscarPruebaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBuscarPruebaFocusGained
        if (txtBuscarPrueba.getText().equals("Buscar prueba...")) {
            txtBuscarPrueba.setText("");
            txtBuscarPrueba.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtBuscarPruebaFocusGained

    private void txtBuscarPruebaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBuscarPruebaFocusLost
        if (txtBuscarPrueba.getText().trim().isEmpty()) {
            txtBuscarPrueba.setText("Buscar prueba...");
            txtBuscarPrueba.setForeground(Color.GRAY);
        }
    }//GEN-LAST:event_txtBuscarPruebaFocusLost

    private void btnAgregarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAgregarMouseClicked
        Prueba pruebaSeleccionada = lstPruebasDisponibles.getSelectedValue();
        
        if (pruebaSeleccionada != null) {
            modeloDisponibles.removeElement(pruebaSeleccionada);
            ordenAlfabeticoListas(modeloAnadidas, pruebaSeleccionada);
        }
    }//GEN-LAST:event_btnAgregarMouseClicked

    private void btnQuitarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnQuitarMouseClicked
        Prueba pruebaSeleccionada = lstPruebasAnadidas.getSelectedValue();

        if (pruebaSeleccionada != null) {
            modeloAnadidas.removeElement(pruebaSeleccionada);
            ordenAlfabeticoListas(modeloDisponibles, pruebaSeleccionada);
        }
    }//GEN-LAST:event_btnQuitarMouseClicked

    private void lstPruebasDisponiblesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstPruebasDisponiblesMouseClicked
        if (evt.getClickCount() == 2) {
            Prueba pruebaSeleccionada = lstPruebasDisponibles.getSelectedValue();

            if (pruebaSeleccionada != null) {
                modeloDisponibles.removeElement(pruebaSeleccionada);
                ordenAlfabeticoListas(modeloAnadidas, pruebaSeleccionada);
            }
        }
    }//GEN-LAST:event_lstPruebasDisponiblesMouseClicked

    private void lstPruebasAnadidasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstPruebasAnadidasMouseClicked
        if (evt.getClickCount() == 2) {
            Prueba pruebaSeleccionada = lstPruebasAnadidas.getSelectedValue();

            if (pruebaSeleccionada != null) {
                modeloAnadidas.removeElement(pruebaSeleccionada);
                ordenAlfabeticoListas(modeloDisponibles, pruebaSeleccionada);
            }
        }
    }//GEN-LAST:event_lstPruebasAnadidasMouseClicked

    private void txtBuscarPruebaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarPruebaKeyReleased
        filtrarPruebas();
    }//GEN-LAST:event_txtBuscarPruebaKeyReleased

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

    private void txtFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFechaActionPerformed

    private void btnAnularMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAnularMouseClicked
         if (peticionActual == null) {
        JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
        return;
    }

    if (peticionActual.getEstado() != EnumEstadoPeticion.PENDIENTE && peticionActual.getEstado() != EnumEstadoPeticion.REALIZADA) {
        JOptionPane.showMessageDialog(this,
                "Solo se pueden anular peticiones en estado PENDIENTE.");
        return;
    }

    int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que deseas anular esta petición?",
            "Confirmar anulación",
            JOptionPane.YES_NO_OPTION
    );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    Connection con = null;

    try {
        con = DB.getConnection();
        con.setAutoCommit(false);

        PeticionDAO peticionDAO = new PeticionDAO();

        boolean actualizada = peticionDAO.actualizarEstado(
                con,
                peticionActual.getIdPeticion(),
                EnumEstadoPeticion.ANULADA
        );

        if (!actualizada) {
            throw new Exception("No se pudo anular la petición.");
        }

        con.commit();

        JOptionPane.showMessageDialog(this, "Petición anulada correctamente.");

        limpiarFormulario();

        CardLayout cl = (CardLayout) panelPrincipal.getLayout();
        cl.show(panelPrincipal, "Pantalla Inicio");

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

    private void btnAnular1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAnular1MouseClicked
        if (peticionActual == null) {
            JOptionPane.showMessageDialog(this, "Primero debes buscar una petición.");
            return;
        }

        pnlGestionPeticion.cargarPeticion(peticionActual.getIdPeticion());

        CardLayout cl = (CardLayout) panelPrincipal.getLayout();
        cl.show(panelPrincipal, "Gestion peticion");
        limpiarFormulario();
    }//GEN-LAST:event_btnAnular1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btnAgregar;
    private javax.swing.JLabel btnAnular;
    private javax.swing.JLabel btnAnular1;
    private javax.swing.JLabel btnBuscar1;
    private javax.swing.JLabel btnCancelar;
    private javax.swing.JLabel btnGuardar;
    private javax.swing.JLabel btnQuitar;
    private javax.swing.ButtonGroup buttonGroupPrioridad;
    private javax.swing.JComboBox<TipoMuestra> comboTipoMuestra;
    private javax.swing.JPanel datosPaciente;
    private javax.swing.JPanel datosPaciente1;
    private javax.swing.JPanel detallesPeticion;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel formularioPaciente;
    private javax.swing.JPanel formularioPaciente1;
    private javax.swing.JPanel formularioPeticion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JList<Prueba> lstPruebasAnadidas;
    private javax.swing.JList<Prueba> lstPruebasDisponibles;
    private javax.swing.JPanel panelBtnAgregar;
    private javax.swing.JPanel panelBtnAnular;
    private javax.swing.JPanel panelBtnAnular1;
    private javax.swing.JPanel panelBtnBuscar1;
    private javax.swing.JPanel panelBtnCancelar;
    private javax.swing.JPanel panelBtnGuardar;
    private javax.swing.JPanel panelBtnQuitar;
    private javax.swing.JPanel panelBusqueda;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelDatosPaciente;
    private javax.swing.JPanel panelDatosPeticion;
    private javax.swing.JPanel panelInferior;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JRadioButton rBPrioridadNormal;
    private javax.swing.JRadioButton rBPrioridadUrgente;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtBuscarId;
    private javax.swing.JTextField txtBuscarPrueba;
    private javax.swing.JTextField txtCip;
    private javax.swing.JTextField txtEstado;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
