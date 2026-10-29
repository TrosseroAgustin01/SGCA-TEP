package com.sgcatep.view;

import com.sgcatep.model.service.GestorUsuario;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.exception.GestionUsuarioException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CambioClaveFX {

    public void show(Stage stage, String dni, String claveActual) {

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

        Label cuTag = new Label("CU-01 · PRIMER INGRESO");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Cambio de contraseña obligatorio");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Su contraseña inicial debe reemplazarse antes de usar el sistema.");
        cardSub.getStyleClass().add("card-subtitulo");
        cardSub.setWrapText(true);

        PasswordField pwdNueva = new PasswordField();
        pwdNueva.setPromptText("Nueva contraseña (mínimo 4 caracteres)");
        pwdNueva.setMaxWidth(Double.MAX_VALUE);

        PasswordField pwdConfirma = new PasswordField();
        pwdConfirma.setPromptText("Repetir nueva contraseña");
        pwdConfirma.setMaxWidth(Double.MAX_VALUE);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(46);
        btnCancelar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnGuardar = new Button("GUARDAR Y CONTINUAR");
        btnGuardar.getStyleClass().add("btn-primario");
        btnGuardar.setPrefHeight(46);
        btnGuardar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(16, btnCancelar, btnGuardar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnCancelar.setOnAction(e -> new LoginFX().show(stage));

        btnGuardar.setOnAction(e -> {
            String nueva = pwdNueva.getText();
            if (!nueva.equals(pwdConfirma.getText())) {
                mostrar(lblMensaje, false, "La nueva contraseña no coincide con la repetición.");
                return;
            }
            try {
                Usuario usuario = new UsuarioRepository().autenticar(dni, claveActual);
                if (usuario == null) {
                    mostrar(lblMensaje, false, "No se pudo verificar la sesión. Vuelva a ingresar.");
                    return;
                }
                new GestorUsuario().cambiarContrasena(usuario, claveActual, nueva);
                mostrar(lblMensaje, true, "Contraseña actualizada. Ingrese nuevamente.");
                new LoginFX().show(stage);
            } catch (GestionUsuarioException ex) {
                mostrar(lblMensaje, false, ex.getMessage());
            } catch (SQLException ex) {
                mostrar(lblMensaje, false, "Error de base de datos: " + ex.getMessage());
            }
        });

        VBox card = new VBox(18,
                cuTag, cardTitulo, cardSub,
                pwdNueva, pwdConfirma,
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

        Scene scene = new Scene(root, 560, 520);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Cambio de contraseña");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private void mostrar(Label lbl, boolean ok, String texto) {
        lbl.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
        lbl.setText(texto);
    }
}