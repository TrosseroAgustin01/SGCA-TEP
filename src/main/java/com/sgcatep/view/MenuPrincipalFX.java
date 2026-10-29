package com.sgcatep.view;

import com.sgcatep.ConexionDB;
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

public class MenuPrincipalFX {

    public void show(Stage stage, Usuario usuario) {

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

        String rolesTexto = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin rol asignado");

        Label etiquetaPerfil = new Label("PERFIL");
        etiquetaPerfil.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888; -fx-font-weight: bold;");

        Label lblUsuario = new Label(usuario.getNombre() + " " + usuario.getApellido());
        lblUsuario.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1b3a5c;");
        lblUsuario.setWrapText(true);

        Label lblArea = new Label("Área: " + usuario.getArea().getNombre());
        lblArea.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        lblArea.setWrapText(true);

        Label lblRol = new Label("Rol: " + rolesTexto);
        lblRol.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        lblRol.setWrapText(true);

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        Button btnCerrarSesion = boton("Cerrar Sesión", "btn-peligro");
        btnCerrarSesion.setOnAction(e -> {
            ConexionDB.closeConnection();
            new LoginFX().show(stage);
        });

        VBox perfil = new VBox(12, etiquetaPerfil, lblUsuario, lblArea, lblRol, espacio, btnCerrarSesion);
        perfil.getStyleClass().add("card");
        perfil.setPadding(new Insets(24));
        perfil.setPrefWidth(240);
        perfil.setMinWidth(220);

        Label cardTitulo = new Label("Menú Principal");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label cardSub = new Label("Seleccione una operación para continuar.");
        cardSub.getStyleClass().add("card-subtitulo");

        Button btnFichada = boton("Registrar Fichada", "btn-primario");
        btnFichada.setOnAction(e -> new FichadaFX().show(stage, usuario));

        Button btnSolicitarAcceso = boton("Solicitar Acceso a Sistema", "btn-primario");
        btnSolicitarAcceso.setOnAction(e -> new SolicitarAccesoFX().show(stage, usuario));

        Button btnConsulta = boton("Consultar fichadas", "btn-primario");
        btnConsulta.setOnAction(e -> new ConsultaFichadasFX().show(stage, usuario));

        Button btnAuditoria = boton("Consultar auditoría", "btn-primario");
        btnAuditoria.setOnAction(e -> new ConsultaAuditoriaFX().show(stage, usuario));

        Button btnAsignarRoles = boton("Asignar Roles y Permisos", "btn-primario");
        btnAsignarRoles.setOnAction(e -> new AsignarRolesFX().show(stage, usuario));
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("Administrador"));
        btnAsignarRoles.setDisable(!esAdmin);
        if (!esAdmin) {
            btnAsignarRoles.setTooltip(new Tooltip("Requiere rol Administrador"));
        }

        Button btnUsuarios = boton("Gestionar usuarios", "btn-primario");
        btnUsuarios.setOnAction(e -> new GestionUsuariosFX().show(stage, usuario));
        btnUsuarios.setDisable(!esAdmin);

        VBox acciones = new VBox(24,
                cardTitulo, cardSub,
                seccion("Presencia y acceso", grilla(btnFichada, btnSolicitarAcceso)),
                seccion("Consultas", grilla(btnConsulta, btnAuditoria)),
                seccion("Administración", grilla(btnUsuarios ,btnAsignarRoles)));
        acciones.getStyleClass().add("card");
        acciones.setPadding(new Insets(24));
        HBox.setHgrow(acciones, Priority.ALWAYS);

        HBox contenido = new HBox(24, perfil, acciones);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setMaxWidth(Region.USE_PREF_SIZE);

        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        StackPane centro = new StackPane(contenido);
        centro.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centro);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #eef2f7; -fx-background: #eef2f7;");

        contenido.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(1040, Math.max(760, scroll.getWidth() * 0.85)), scroll.widthProperty()));

        VBox root = new VBox(header, scroll, footerBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 640);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Menú Principal");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private Button boton(String texto, String estilo) {
        Button b = new Button(texto);
        b.getStyleClass().add(estilo);
        b.setPrefHeight(44);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private GridPane grilla(Button... botones) {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        for (int c = 0; c < 2; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < botones.length; i++) {
            if (botones.length == 1) {
                g.add(botones[i], 0, 0, 2, 1);
            } else {
                g.add(botones[i], i % 2, i / 2);
            }
        }
        return g;
    }

    private VBox seccion(String titulo, GridPane contenido) {
        Label lbl = new Label(titulo.toUpperCase());
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1565c0;");
        return new VBox(10, lbl, contenido);
    }
}