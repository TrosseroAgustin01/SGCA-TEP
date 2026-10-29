package com.sgcatep.view;

import com.sgcatep.controller.AutorizacionController;
import com.sgcatep.model.domain.AutorizacionRemota;
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

public class AutorizacionRemotaFX {

    private final AutorizacionController controller = new AutorizacionController();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<AutorizacionRemota> actuales = new ArrayList<>();

    private final ComboBox<String> cmbDestino = new ComboBox<>();
    private final ListView<String> lista = new ListView<>();
    private final DatePicker dpInicio = new DatePicker();
    private final DatePicker dpFin = new DatePicker();
    private final TextField txtMotivo = new TextField();
    private final Label lblMensaje = new Label();

    private Usuario admin;

    public void show(Stage stage, Usuario adminLogueado) {
        this.admin = adminLogueado;

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

        Label cuTag = new Label("CU-03 · AUTORIZACIÓN DE TRABAJO REMOTO");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Autorizaciones de trabajo remoto");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Una autorización solo habilita fichar en modalidad REMOTA durante su período.");
        cardSub.getStyleClass().add("card-subtitulo");
        cardSub.setWrapText(true);

        cmbDestino.setPromptText("Seleccione un usuario");
        cmbDestino.setMaxWidth(Double.MAX_VALUE);
        cmbDestino.getStyleClass().add("combo-box");
        cmbDestino.getSelectionModel().selectedIndexProperty()
                .addListener((obs, viejo, nuevo) -> cargarAutorizaciones());

        lista.setPrefHeight(150);
        lista.setPlaceholder(new Label("Sin autorizaciones"));

        dpInicio.setPromptText("Desde");
        dpInicio.setMaxWidth(Double.MAX_VALUE);
        dpFin.setPromptText("Hasta");
        dpFin.setMaxWidth(Double.MAX_VALUE);
        txtMotivo.setPromptText("Motivo del trabajo remoto");
        txtMotivo.setMaxWidth(Double.MAX_VALUE);

        lblMensaje.setWrapText(true);

        try {
            usuarios.addAll(controller.listarUsuarios());
        } catch (SQLException ex) {
            mostrar(false, "Error cargando usuarios: " + ex.getMessage());
        }
        for (Usuario u : usuarios) {
            cmbDestino.getItems().add(u.getIdUsuario() + " - " + u.getApellido() + ", " + u.getNombre());
        }

        Button btnDesactivar = new Button("DESACTIVAR SELECCIONADA");
        btnDesactivar.getStyleClass().add("btn-secundario");
        btnDesactivar.setPrefHeight(46);
        btnDesactivar.setMinWidth(Region.USE_PREF_SIZE);
        btnDesactivar.setOnAction(e -> desactivar());

        HBox desactivarBox = new HBox(btnDesactivar);
        desactivarBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAlta = new Button("REGISTRAR AUTORIZACIÓN");
        btnAlta.getStyleClass().add("btn-primario");
        btnAlta.setPrefHeight(46);
        btnAlta.setMinWidth(Region.USE_PREF_SIZE);
        btnAlta.setOnAction(e -> alta());

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(46);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);
        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, admin));

        HBox botonesBox = new HBox(16, btnVolver, btnAlta);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(18,
                cuTag, cardTitulo, cardSub,
                titulo("Usuario"), cmbDestino,
                titulo("Autorizaciones del usuario"), lista, desactivarBox,
                new Separator(),
                titulo("Nueva autorización"),
                dpInicio, dpFin, txtMotivo,
                lblMensaje, botonesBox);
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

        stage.setTitle("SGCA-TEP - Autorizaciones remotas");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private void cargarAutorizaciones() {
        actuales.clear();
        lista.getItems().clear();
        int i = cmbDestino.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            return;
        }
        try {
            actuales.addAll(controller.listarDe(usuarios.get(i).getIdUsuario()));
            for (AutorizacionRemota a : actuales) {
                lista.getItems().add(controller.linea(a));
            }
        } catch (SQLException ex) {
            mostrar(false, "Error cargando autorizaciones: " + ex.getMessage());
        }
    }

    private void alta() {
        int i = cmbDestino.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            mostrar(false, "Seleccione un usuario destino.");
            return;
        }
        AutorizacionController.Resultado r = controller.alta(admin,
                usuarios.get(i).getIdUsuario(), dpInicio.getValue(), dpFin.getValue(),
                txtMotivo.getText());
        mostrar(r.ok(), r.mensaje());
        if (r.ok()) {
            dpInicio.setValue(null);
            dpFin.setValue(null);
            txtMotivo.clear();
            cargarAutorizaciones();
        }
    }

    private void desactivar() {
        int i = lista.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            mostrar(false, "Seleccione una autorización de la lista.");
            return;
        }
        AutorizacionController.Resultado r = controller.desactivar(admin, actuales.get(i).getIdAutorizacion());
        mostrar(r.ok(), r.mensaje());
        if (r.ok()) {
            cargarAutorizaciones();
        }
    }

    private void mostrar(boolean ok, String texto) {
        lblMensaje.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
        lblMensaje.setText(texto);
    }

    private Label titulo(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }
}