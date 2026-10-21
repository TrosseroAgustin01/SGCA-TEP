package com.sgcatep.view;

import com.sgcatep.controller.AsignacionController;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Usuario;
import javafx.beans.binding.Bindings;
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

public class AsignarRolesFX {

    private final AsignacionController controller = new AsignacionController();

    public void show(Stage stage, Usuario adminLogueado) {

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

        Label cuTag = new Label("CU-02 · VISTA ADMINISTRADOR");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Asignar roles a usuarios");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Asigne o quite roles a un usuario en un área de trabajo.");
        cardSub.getStyleClass().add("card-subtitulo");

        Label lblAdmin = new Label("Administrador: "
                + adminLogueado.getNombre() + " " + adminLogueado.getApellido());
        lblAdmin.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 0, 4, 0));

        Label lblUsuarioSel = new Label("Seleccionar usuario:");
        lblUsuarioSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbUsuario = new ComboBox<>();
        cmbUsuario.setPromptText("Seleccione un usuario");
        cmbUsuario.getStyleClass().add("combo-box");

        Label lblRolesActuales = new Label("Roles actuales del usuario (rol — área):");
        lblRolesActuales.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ListView<String> listaRolesActuales = new ListView<>();
        listaRolesActuales.setPrefHeight(110);
        listaRolesActuales.setPlaceholder(new Label("Sin roles asignados"));

        Label lblSistemasAcceso = new Label("Sistemas accesibles por el usuario:");
        lblSistemasAcceso.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ListView<String> listaSistemasAcceso = new ListView<>();
        listaSistemasAcceso.setPrefHeight(90);
        listaSistemasAcceso.setPlaceholder(new Label("Sin sistemas accesibles"));

        Label lblAreaSel = new Label("Área de trabajo:");
        lblAreaSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbArea = new ComboBox<>();
        cmbArea.setPromptText("Seleccione un área");
        cmbArea.getStyleClass().add("combo-box");

        Label lblRolSel = new Label("Rol:");
        lblRolSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.setPromptText("Seleccione un rol");
        cmbRol.getStyleClass().add("combo-box");

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        List<Usuario> usuarios = new ArrayList<>();
        List<Area> areas = new ArrayList<>();
        List<Rol> roles = new ArrayList<>();

        try {
            usuarios.addAll(controller.listarUsuarios());
            areas.addAll(controller.listarAreas());
            roles.addAll(controller.listarRoles());
        } catch (SQLException ex) {
            mostrarResultado(lblMensaje, false, "Error cargando datos: " + ex.getMessage());
        }

        for (Usuario u : usuarios) {
            cmbUsuario.getItems().add(u.getIdUsuario() + " - " + u.getNombre() + " " + u.getApellido());
        }
        for (Area a : areas) {
            cmbArea.getItems().add(a.getIdArea() + " - " + a.getNombre());
        }
        for (Rol r : roles) {
            cmbRol.getItems().add(r.getIdRol() + " - " + r.getNombre());
        }

        cmbUsuario.getSelectionModel().selectedIndexProperty().addListener((obs, viejo, nuevo) ->
                cargarUsuario(nuevo.intValue(), usuarios, listaRolesActuales, listaSistemasAcceso, lblMensaje));

        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(44);
        btnCancelar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnAsignar = new Button("GUARDAR ASIGNACIÓN");
        btnAsignar.getStyleClass().add("btn-primario");
        btnAsignar.setPrefHeight(44);
        btnAsignar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnQuitar = new Button("QUITAR ROL");
        btnQuitar.getStyleClass().add("btn-secundario");
        btnQuitar.setPrefHeight(44);
        btnQuitar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(14, btnCancelar, btnQuitar, btnAsignar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnAsignar.setOnAction(e -> {
            int iu = cmbUsuario.getSelectionModel().getSelectedIndex();
            int ia = cmbArea.getSelectionModel().getSelectedIndex();
            int ir = cmbRol.getSelectionModel().getSelectedIndex();
            if (iu < 0 || ia < 0 || ir < 0) {
                mostrarResultado(lblMensaje, false, "Debe seleccionar usuario, área y rol.");
                return;
            }
            AsignacionController.Resultado r = controller.asignar(adminLogueado,
                    usuarios.get(iu).getIdUsuario(), roles.get(ir).getIdRol(), areas.get(ia).getIdArea());
            mostrarResultado(lblMensaje, r.ok(), r.mensaje());
            if (r.ok()) {
                cargarUsuario(iu, usuarios, listaRolesActuales, listaSistemasAcceso, lblMensaje);
            }
        });

        btnQuitar.setOnAction(e -> {
            int iu = cmbUsuario.getSelectionModel().getSelectedIndex();
            int ia = cmbArea.getSelectionModel().getSelectedIndex();
            int ir = cmbRol.getSelectionModel().getSelectedIndex();
            if (iu < 0 || ia < 0 || ir < 0) {
                mostrarResultado(lblMensaje, false, "Debe seleccionar usuario, área y rol.");
                return;
            }
            AsignacionController.Resultado r = controller.quitar(adminLogueado,
                    usuarios.get(iu).getIdUsuario(), roles.get(ir).getIdRol(), areas.get(ia).getIdArea());
            mostrarResultado(lblMensaje, r.ok(), r.mensaje());
            if (r.ok()) {
                cargarUsuario(iu, usuarios, listaRolesActuales, listaSistemasAcceso, lblMensaje);
            }
        });

        btnCancelar.setOnAction(e -> new MenuPrincipalFX().show(stage, adminLogueado));

        VBox card = new VBox(15,
                cuTag, cardTitulo, cardSub, lblAdmin, sep1,
                lblUsuarioSel, cmbUsuario,
                lblRolesActuales, listaRolesActuales,
                lblSistemasAcceso, listaSistemasAcceso,
                lblAreaSel, cmbArea,
                lblRolSel, cmbRol,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
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

        stage.setTitle("SGCA-TEP - Asignar Roles");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.sizeToScene();
    }

    private void cargarUsuario(int indice, List<Usuario> usuarios,
                               ListView<String> roles, ListView<String> sistemas, Label lblMensaje) {
        roles.getItems().clear();
        sistemas.getItems().clear();
        if (indice < 0) {
            return;
        }
        try {
            int idUsuario = usuarios.get(indice).getIdUsuario();
            roles.getItems().addAll(controller.rolesActuales(idUsuario));
            sistemas.getItems().addAll(controller.sistemasAccesibles(idUsuario));
        } catch (SQLException ex) {
            mostrarResultado(lblMensaje, false, "Error cargando datos del usuario: " + ex.getMessage());
        }
    }

    private void mostrarResultado(Label lbl, boolean exito, String texto) {
        lbl.getStyleClass().setAll(exito ? "msg-exito" : "msg-error");
        lbl.setText(texto);
    }
}