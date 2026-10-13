package com.sgcatep.view;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalFX {

    public void show(Stage stage, Usuario usuario) {

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
        String rolesTexto = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin rol asignado");

        Label cardTitulo = new Label("Menú Principal");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label lblUsuario = new Label("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());
        lblUsuario.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label lblArea = new Label("Área: " + usuario.getArea().getNombre());
        lblArea.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label lblRol = new Label("Rol: " + rolesTexto);
        lblRol.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 8, 0));

        Button btnFichada = new Button("Registrar Fichada");
        btnFichada.getStyleClass().add("btn-primario");
        btnFichada.setPrefWidth(320);
        btnFichada.setPrefHeight(42);

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("Administrador"));

        Button btnAsignarRoles = new Button("Asignar Roles y Permisos");
        btnAsignarRoles.getStyleClass().add("btn-primario");
        btnAsignarRoles.setPrefWidth(320);
        btnAsignarRoles.setPrefHeight(42);
        btnAsignarRoles.setDisable(!esAdmin);

        Button btnCerrarSesion = new Button("Cerrar Sesión");
        btnCerrarSesion.getStyleClass().add("btn-peligro");
        btnCerrarSesion.setPrefWidth(320);
        btnCerrarSesion.setPrefHeight(42);

        btnFichada.setOnAction(e -> {
            FichadaFX fichada = new FichadaFX();
            fichada.show(stage, usuario);
        });

        btnAsignarRoles.setOnAction(e -> {
            AsignarRolesFX asignar = new AsignarRolesFX();
            asignar.show(stage, usuario);
        });

        btnCerrarSesion.setOnAction(e -> {
            ConexionDB.closeConnection();
            LoginFX login = new LoginFX();
            login.show(stage);
        });

        VBox card = new VBox(12,
                cardTitulo,
                lblUsuario, lblArea, lblRol,
                sep,
                btnFichada, btnAsignarRoles, btnCerrarSesion
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER_LEFT);

        // ── FOOTER ───────────────────────────────────────
        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        // ── ENSAMBLADO ───────────────────────────────────
        StackPane centro = new StackPane(card);
        centro.setPadding(new Insets(40));
        VBox.setVgrow(centro, Priority.ALWAYS);

        VBox root = new VBox(header, centro, footerBox);

        Scene scene = new Scene(root, 500, 480);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Menú Principal");
        stage.setScene(scene);
    }
}