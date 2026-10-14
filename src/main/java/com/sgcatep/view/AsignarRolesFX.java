package com.sgcatep.view;

import com.sgcatep.exception.AsignacionRolException;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.repository.RolRepository;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.model.service.GestorAcceso;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AsignarRolesFX {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();
    private final AreaRepository areaRepository = new AreaRepository();
    private final GestorAcceso gestorAcceso = new GestorAcceso();

    public void show(Stage stage, Usuario adminLogueado) {

        // header
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

        // contenido
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

        try {
            List<Usuario> usuarios = usuarioRepository.listarTodos();
            for (Usuario u : usuarios) {
                cmbUsuario.getItems().add(u.getIdUsuario()
                        + " - " + u.getNombre() + " " + u.getApellido());
            }
            List<Area> areas = areaRepository.listarActivas();
            for (Area a : areas) {
                cmbArea.getItems().add(a.getIdArea() + " - " + a.getNombre());
            }
            List<Rol> roles = rolRepository.listarActivos();
            for (Rol r : roles) {
                cmbRol.getItems().add(r.getIdRol() + " - " + r.getNombre());
            }
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando datos: " + ex.getMessage());
        }

        cmbUsuario.valueProperty().addListener((obs, anterior, nuevo) ->
                cargarRolesActuales(listaRolesActuales, nuevo, lblMensaje));

        // botones
        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnAsignar = new Button("GUARDAR ASIGNACIÓN");
        btnAsignar.getStyleClass().add("btn-primario");
        btnAsignar.setPrefHeight(40);
        btnAsignar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnQuitar = new Button("QUITAR ROL");
        btnQuitar.getStyleClass().add("btn-secundario");
        btnQuitar.setPrefHeight(40);
        btnQuitar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(12, btnCancelar, btnQuitar, btnAsignar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnAsignar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null
                    || cmbArea.getValue() == null
                    || cmbRol.getValue() == null) {
                mostrarResultado(lblMensaje, false, "Debe seleccionar usuario, área y rol.");
                return;
            }
            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea    = extraerId(cmbArea.getValue());
                int idRol     = extraerId(cmbRol.getValue());

                Usuario usuarioSel = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSel         = rolRepository.buscarPorId(idRol);

                gestorAcceso.asignarRol(adminLogueado, usuarioSel, rolSel, idArea);

                mostrarResultado(lblMensaje, true, "✓ Rol '" + rolSel.getNombre()
                        + "' asignado a " + usuarioSel.getNombre() + " correctamente.");
                cargarRolesActuales(listaRolesActuales, cmbUsuario.getValue(), lblMensaje);
            } catch (AsignacionRolException ex) {
                mostrarResultado(lblMensaje, false, "✗ " + ex.getMessage());
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) {
                    mostrarResultado(lblMensaje, false,
                            "✗ El usuario ya tiene ese rol en esa área.");
                } else {
                    mostrarResultado(lblMensaje, false,
                            "Error de base de datos: " + ex.getMessage());
                }
            }
        });

        btnQuitar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null
                    || cmbArea.getValue() == null
                    || cmbRol.getValue() == null) {
                mostrarResultado(lblMensaje, false, "Debe seleccionar usuario, área y rol.");
                return;
            }
            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea    = extraerId(cmbArea.getValue());
                int idRol     = extraerId(cmbRol.getValue());

                Usuario usuarioSel = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSel         = rolRepository.buscarPorId(idRol);

                gestorAcceso.quitarRol(adminLogueado, usuarioSel, rolSel, idArea);

                mostrarResultado(lblMensaje, true, "✓ Rol '" + rolSel.getNombre()
                        + "' quitado correctamente.");
                cargarRolesActuales(listaRolesActuales, cmbUsuario.getValue(), lblMensaje);
            } catch (AsignacionRolException ex) {
                mostrarResultado(lblMensaje, false, "✗ " + ex.getMessage());
            } catch (SQLException ex) {
                mostrarResultado(lblMensaje, false, "Error de base de datos: " + ex.getMessage());
            }
        });

        btnCancelar.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, adminLogueado);
        });

        VBox card = new VBox(12,
                cuTag, cardTitulo, cardSub, lblAdmin, sep1,
                lblUsuarioSel, cmbUsuario,
                lblRolesActuales, listaRolesActuales,
                lblAreaSel, cmbArea,
                lblRolSel, cmbRol,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(560);
        card.setAlignment(Pos.CENTER_LEFT);

        // pie
        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        // estilos similares a las interfaces
        StackPane centro = new StackPane(card);
        centro.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centro);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #eef2f7; -fx-background: #eef2f7;");

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

    private void cargarRolesActuales(ListView<String> lista, String seleccion, Label lblMensaje) {
        lista.getItems().clear();
        if (seleccion == null) {
            return;
        }
        try {
            int idUsuario = extraerId(seleccion);
            lista.getItems().addAll(usuarioRepository.listarRolesConArea(idUsuario));
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando roles del usuario: " + ex.getMessage());
        }
    }

    private int extraerId(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0].trim());
    }

    private void mostrarResultado(Label lbl, boolean exito, String texto) {
        lbl.getStyleClass().setAll(exito ? "msg-exito" : "msg-error");
        lbl.setText(texto);
    }
}