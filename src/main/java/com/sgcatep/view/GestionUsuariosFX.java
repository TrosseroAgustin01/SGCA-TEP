package com.sgcatep.view;

import com.sgcatep.controller.UsuarioController;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosFX {

    private static final String NUEVO = "— Nuevo usuario —";

    private final UsuarioController controller = new UsuarioController();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Area> areas = new ArrayList<>();

    private final ComboBox<String> cmbUsuario = new ComboBox<>();
    private final Label lblEstado = new Label();
    private final TextField txtDni = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtApellido = new TextField();
    private final TextField txtEmail = new TextField();
    private final PasswordField pwdAlta = new PasswordField();
    private final ComboBox<String> cmbArea = new ComboBox<>();
    private final Label lblAdvertencia = new Label(
            "Los roles actuales no cambian al mover de área. Revíselos en CU-02.");
    private final Label lblMensaje = new Label();
    private final Button btnEstado = new Button();
    private final Button btnGuardar = new Button();

    private final PasswordField pwdActual = new PasswordField();
    private final PasswordField pwdNueva = new PasswordField();
    private final PasswordField pwdConfirma = new PasswordField();
    private final Label lblClave = new Label();

    private Usuario seleccionado;
    private Usuario adminLogueado;

    public void show(Stage stage, Usuario adminLogueado) {
        this.adminLogueado = adminLogueado;

        Label lblTitulo = new Label("SGCA-TEP");
        lblTitulo.getStyleClass().add("header-titulo");

        Label lblSub = new Label("Tribunal Electoral Provincial de Santa Fe");
        lblSub.getStyleClass().add("header-subtitulo");

        VBox textoHeader = new VBox(2, lblTitulo, lblSub);

        Label icono = new Label("⊛");
        icono.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        HBox header = new HBox(12, icono, textoHeader);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefWidth(Double.MAX_VALUE);

        Label cuTag = new Label("CU-01 · GESTIÓN DE USUARIOS");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Administrar usuarios");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Alta, modificación y estado de las cuentas.");
        cardSub.getStyleClass().add("card-subtitulo");

        txtDni.setPromptText("7 a 10 dígitos, sin puntos");
        txtEmail.setPromptText("usuario@tep.gov.ar");
        pwdAlta.setPromptText("Mínimo 4 caracteres");
        pwdActual.setPromptText("Contraseña actual");
        pwdNueva.setPromptText("Nueva contraseña");
        pwdConfirma.setPromptText("Repetir nueva contraseña");

        for (Control c : List.of(cmbUsuario, cmbArea, txtDni, txtNombre, txtApellido, txtEmail,
                pwdAlta, pwdActual, pwdNueva, pwdConfirma)) {
            c.setMaxWidth(Double.MAX_VALUE);
        }
        cmbUsuario.getStyleClass().add("combo-box");
        cmbArea.getStyleClass().add("combo-box");

        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        lblAdvertencia.setWrapText(true);
        lblAdvertencia.setStyle("-fx-text-fill: #e65100; -fx-font-size: 12px;");
        lblAdvertencia.setVisible(false);
        lblAdvertencia.setManaged(false);

        lblMensaje.setWrapText(true);
        lblClave.setWrapText(true);

        btnEstado.getStyleClass().add("btn-secundario");
        btnEstado.setPrefHeight(46);
        btnEstado.setMinWidth(Region.USE_PREF_SIZE);
        btnEstado.setDisable(true);
        btnEstado.setOnAction(e -> cambiarEstado());

        btnGuardar.getStyleClass().add("btn-primario");
        btnGuardar.setPrefHeight(46);
        btnGuardar.setMinWidth(Region.USE_PREF_SIZE);
        btnGuardar.setOnAction(e -> guardar());

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(46);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);
        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, adminLogueado));

        HBox botonesBox = new HBox(16, btnVolver ,btnEstado, btnGuardar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnClave = new Button("CAMBIAR CONTRASEÑA");
        btnClave.getStyleClass().add("btn-primario");
        btnClave.setPrefHeight(46);
        btnClave.setMinWidth(Region.USE_PREF_SIZE);
        btnClave.setOnAction(e -> cambiarClave());

        HBox claveBox = new HBox(btnClave);
        claveBox.setAlignment(Pos.CENTER_RIGHT);

        cmbUsuario.getSelectionModel().selectedIndexProperty()
                .addListener((obs, viejo, nuevo) -> seleccionar(nuevo.intValue()));
        cmbArea.getSelectionModel().selectedIndexProperty()
                .addListener((obs, viejo, nuevo) -> advertirArea(nuevo.intValue()));

        try {
            areas.addAll(controller.listarAreas());
        } catch (SQLException ex) {
            mostrar(lblMensaje, new UsuarioController.Resultado(false,
                    "Error cargando áreas: " + ex.getMessage()));
        }
        for (Area a : areas) {
            cmbArea.getItems().add(a.getNombre());
        }

        recargar(null);

        VBox card = new VBox(18,
                cuTag, cardTitulo, cardSub,
                titulo("Usuario"), cmbUsuario, lblEstado,
                titulo("DNI"), txtDni,
                titulo("Nombre"), txtNombre,
                titulo("Apellido"), txtApellido,
                titulo("Email"), txtEmail,
                titulo("Contraseña inicial (el usuario la cambia al ingresar)"), pwdAlta,
                titulo("Área de trabajo"), cmbArea,
                lblAdvertencia,
                lblMensaje,
                botonesBox,
                new Separator(),
                titulo("Cambiar mi contraseña"),
                pwdActual, pwdNueva, pwdConfirma,
                claveBox, lblClave);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(28));
        card.setAlignment(Pos.CENTER_LEFT);

        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        StackPane centro = new StackPane(card);
        centro.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centro);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #eef2f7; -fx-background: #eef2f7;");

        Escala.ajustarCard(card, scroll);

        VBox root = new VBox(header, scroll, footerBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 640, 720);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Gestionar usuarios");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private void recargar(String dni) {
        try {
            usuarios.clear();
            usuarios.addAll(controller.listarUsuarios());
        } catch (SQLException ex) {
            mostrar(lblMensaje, new UsuarioController.Resultado(false,
                    "Error cargando usuarios: " + ex.getMessage()));
            return;
        }

        List<String> items = new ArrayList<>();
        items.add(NUEVO);
        int idx = 0;
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            items.add("%s, %s (DNI %s)%s".formatted(u.getApellido(), u.getNombre(), u.getDni(),
                    u.isActivo() ? "" : " - inactivo"));
            if (u.getDni().equals(dni)) {
                idx = i + 1;
            }
        }
        cmbUsuario.getItems().setAll(items);
        cmbUsuario.getSelectionModel().select(idx);
        seleccionar(idx);
    }

    private void seleccionar(int indice) {
        if (indice < 0) {
            return;
        }
        if (indice == 0) {
            seleccionado = null;
            limpiarFormulario();
            return;
        }

        seleccionado = usuarios.get(indice - 1);
        txtDni.setText(seleccionado.getDni());
        txtDni.setDisable(true);
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtEmail.setText(seleccionado.getEmail());
        pwdAlta.clear();
        pwdAlta.setDisable(true);

        int ia = indiceArea(seleccionado.getArea().getIdArea());
        if (ia < 0) {
            cmbArea.getSelectionModel().clearSelection();
        } else {
            cmbArea.getSelectionModel().select(ia);
        }

        lblEstado.setText(seleccionado.isActivo() ? "Estado: activo" : "Estado: inactivo");
        btnEstado.setText(seleccionado.isActivo() ? "DESACTIVAR" : "ACTIVAR");
        btnEstado.setDisable(false);
        btnGuardar.setText("GUARDAR CAMBIOS");
        lblAdvertencia.setVisible(false);
        lblAdvertencia.setManaged(false);
    }

    private void limpiarFormulario() {
        txtDni.clear();
        txtDni.setDisable(false);
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        pwdAlta.setText("desa");
        pwdAlta.setDisable(false);
        cmbArea.getSelectionModel().clearSelection();
        lblEstado.setText("");
        btnEstado.setText("DESACTIVAR");
        btnEstado.setDisable(true);
        btnGuardar.setText("CREAR USUARIO");
        lblAdvertencia.setVisible(false);
        lblAdvertencia.setManaged(false);
    }

    private void advertirArea(int indice) {
        boolean cambio = seleccionado != null && indice >= 0
                && areas.get(indice).getIdArea() != seleccionado.getArea().getIdArea();
        lblAdvertencia.setVisible(cambio);
        lblAdvertencia.setManaged(cambio);
    }

    private int indiceArea(int idArea) {
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).getIdArea() == idArea) {
                return i;
            }
        }
        return -1;
    }

    private void guardar() {
        int ia = cmbArea.getSelectionModel().getSelectedIndex();
        if (ia < 0) {
            mostrar(lblMensaje, new UsuarioController.Resultado(false, "Seleccione un área."));
            return;
        }
        Area area = areas.get(ia);

        UsuarioController.Resultado r;
        String dni;
        if (seleccionado == null) {
            dni = txtDni.getText().trim();
            r = controller.alta(adminLogueado, dni, txtNombre.getText().trim(),
                    txtApellido.getText().trim(), txtEmail.getText().trim(), pwdAlta.getText(), area);
        } else {
            dni = seleccionado.getDni();
            r = controller.modificar(adminLogueado, seleccionado.getIdUsuario(),
                    txtNombre.getText().trim(), txtApellido.getText().trim(),
                    txtEmail.getText().trim(), area);
        }
        mostrar(lblMensaje, r);
        if (r.ok()) {
            recargar(dni);
        }
    }

    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        UsuarioController.Resultado r = controller.cambiarEstado(
                adminLogueado, seleccionado.getIdUsuario(), !seleccionado.isActivo());
        mostrar(lblMensaje, r);
        if (r.ok()) {
            recargar(seleccionado.getDni());
        }
    }

    private void cambiarClave() {
        if (!pwdNueva.getText().equals(pwdConfirma.getText())) {
            mostrar(lblClave, new UsuarioController.Resultado(false,
                    "La nueva contraseña no coincide con la repetición."));
            return;
        }
        UsuarioController.Resultado r = controller.cambiarContrasena(
                adminLogueado, pwdActual.getText(), pwdNueva.getText());
        mostrar(lblClave, r);
        if (r.ok()) {
            pwdActual.clear();
            pwdNueva.clear();
            pwdConfirma.clear();
        }
    }

    private void mostrar(Label lbl, UsuarioController.Resultado r) {
        lbl.getStyleClass().setAll(r.ok() ? "msg-exito" : "msg-error");
        lbl.setText(r.mensaje());
    }

    private Label titulo(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }
}