package com.sgcatep.view;

import com.sgcatep.exception.GestionUsuarioException;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorUsuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MiClaveFX {

    private final GestorUsuario gestor = new GestorUsuario();

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

        Label cuTag = new Label("CU-01 · MI CONTRASEÑA");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Cambiar mi contraseña");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Solo puede cambiar su propia contraseña. Requiere la actual.");
        cardSub.getStyleClass().add("card-subtitulo");
        cardSub.setWrapText(true);

        PasswordField pwdActual = new PasswordField();
        pwdActual.setPromptText("Contraseña actual");
        pwdActual.setMaxWidth(Double.MAX_VALUE);

        PasswordField pwdNueva = new PasswordField();
        pwdNueva.setPromptText("Nueva contraseña (mínimo 4 caracteres)");
        pwdNueva.setMaxWidth(Double.MAX_VALUE);

        PasswordField pwdConfirma = new PasswordField();
        pwdConfirma.setPromptText("Repetir nueva contraseña");
        pwdConfirma.setMaxWidth(Double.MAX_VALUE);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(46);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);

        Button btnGuardar = new Button("CAMBIAR CONTRASEÑA");
        btnGuardar.getStyleClass().add("btn-primario");
        btnGuardar.setPrefHeight(46);
        btnGuardar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(16, btnVolver, btnGuardar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, usuario));

        btnGuardar.setOnAction(e -> {
            if (!pwdNueva.getText().equals(pwdConfirma.getText())) {
                mostrar(lblMensaje, false, "La nueva contraseña no coincide con la repetición.");
                return;
            }
            try {
                gestor.cambiarContrasena(usuario, pwdActual.getText(), pwdNueva.getText());
                mostrar(lblMensaje, true, "Contraseña cambiada correctamente.");
                pwdActual.clear();
                pwdNueva.clear();
                pwdConfirma.clear();
            } catch (GestionUsuarioException ex) {
                mostrar(lblMensaje, false, ex.getMessage());
            } catch (SQLException ex) {
                mostrar(lblMensaje, false, "Error de base de datos: " + ex.getMessage());
            }
        });

        VBox card = new VBox(18,
                cuTag, cardTitulo, cardSub,
                pwdActual, pwdNueva, pwdConfirma,
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

        Scene scene = new Scene(root, 560, 560);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Mi contraseña");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private void mostrar(Label lbl, boolean ok, String texto) {
        lbl.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
        lbl.setText(texto);
    }
}