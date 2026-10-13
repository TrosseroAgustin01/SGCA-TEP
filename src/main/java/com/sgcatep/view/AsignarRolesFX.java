package com.sgcatep.view;

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

        // ── HEADER ──────────────────────────────────────
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

        // ── CARD ─────────────────────────────────────────
        Label cuTag = new Label("CU-02 · VISTA ADMINISTRADOR");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Asignar roles y permisos");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Configure el área, rol y permisos asociados al usuario.");
        cardSub.getStyleClass().add("card-subtitulo");

        Label lblAdmin = new Label("Administrador: "
                + adminLogueado.getNombre() + " " + adminLogueado.getApellido());
        lblAdmin.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 0, 4, 0));

        // Combos
        Label lblUsuarioSel = new Label("Seleccionar usuario:");
        lblUsuarioSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbUsuario = new ComboBox<>();
        cmbUsuario.setPrefWidth(380);
        cmbUsuario.setPromptText("Seleccione un usuario");
        cmbUsuario.getStyleClass().add("combo-box");

        Label lblAreaSel = new Label("Área de trabajo:");
        lblAreaSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbArea = new ComboBox<>();
        cmbArea.setPrefWidth(380);
        cmbArea.setPromptText("Seleccione un área");
        cmbArea.getStyleClass().add("combo-box");

        Label lblRolSel = new Label("Rol:");
        lblRolSel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.setPrefWidth(380);
        cmbRol.setPromptText("Seleccione un rol");
        cmbRol.getStyleClass().add("combo-box");

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(380);

        // Cargar datos
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

        // Botones
        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(40);

        Button btnAsignar = new Button("GUARDAR ASIGNACIÓN");
        btnAsignar.getStyleClass().add("btn-primario");
        btnAsignar.setPrefHeight(40);

        Button btnQuitar = new Button("QUITAR ROL");
        btnQuitar.getStyleClass().add("btn-secundario");
        btnQuitar.setPrefHeight(40);

        HBox botonesBox = new HBox(12, btnCancelar, btnQuitar, btnAsignar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnAsignar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null
                    || cmbArea.getValue() == null
                    || cmbRol.getValue() == null) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Debe seleccionar usuario, área y rol.");
                return;
            }
            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea    = extraerId(cmbArea.getValue());
                int idRol     = extraerId(cmbRol.getValue());

                Usuario usuarioSel = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSel         = rolRepository.buscarPorId(idRol);

                boolean ok = gestorAcceso.asignarRol(adminLogueado, usuarioSel, rolSel, idArea, null);
                lblMensaje.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
                lblMensaje.setText(ok
                        ? "✓ Rol '" + rolSel.getNombre() + "' asignado a "
                        + usuarioSel.getNombre() + " correctamente."
                        : "✗ No se pudo asignar. El usuario puede estar inactivo o "
                        + "ya tener ese rol en esa área.");
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText(ex.getMessage().contains("Duplicate")
                        ? "✗ El usuario ya tiene ese rol en esa área."
                        : "Error: " + ex.getMessage());
            }
        });

        btnQuitar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null
                    || cmbArea.getValue() == null
                    || cmbRol.getValue() == null) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Debe seleccionar usuario, área y rol.");
                return;
            }
            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea    = extraerId(cmbArea.getValue());
                int idRol     = extraerId(cmbRol.getValue());

                Usuario usuarioSel = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSel         = rolRepository.buscarPorId(idRol);

                boolean ok = gestorAcceso.quitarRol(usuarioSel, rolSel, idArea);
                lblMensaje.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
                lblMensaje.setText(ok
                        ? "✓ Rol '" + rolSel.getNombre() + "' quitado correctamente."
                        : "✗ El usuario no tenía ese rol en esa área.");
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Error: " + ex.getMessage());
            }
        });

        btnCancelar.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, adminLogueado);
        });

        VBox card = new VBox(12,
                cuTag, cardTitulo, cardSub, lblAdmin, sep1,
                lblUsuarioSel, cmbUsuario,
                lblAreaSel, cmbArea,
                lblRolSel, cmbRol,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(460);
        card.setAlignment(Pos.CENTER_LEFT);

        // ── FOOTER ───────────────────────────────────────
        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        // ── ENSAMBLADO ───────────────────────────────────
        StackPane centro = new StackPane(card);
        centro.setPadding(new Insets(30));
        VBox.setVgrow(centro, Priority.ALWAYS);

        VBox root = new VBox(header, centro, footerBox);

        Scene scene = new Scene(root, 540, 620);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Asignar Roles");
        stage.setScene(scene);
    }

    private int extraerId(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0].trim());
    }
}