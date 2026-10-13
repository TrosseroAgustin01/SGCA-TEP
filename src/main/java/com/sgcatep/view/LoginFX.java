package com.sgcatep.view;

import com.sgcatep.model.domain.Usuario;
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

public class LoginFX {

    private final GestorAcceso gestorAcceso = new GestorAcceso();
    private Stage stage;

    public void show(Stage primaryStage) {
        this.stage = primaryStage;

        // ── HEADER ──────────────────────────────────────
        Label lblTitulo = new Label("SGCA-TEP");
        lblTitulo.getStyleClass().add("header-titulo");

        Label lblSub = new Label("Tribunal Electoral Provincial de Santa Fe");
        lblSub.getStyleClass().add("header-subtitulo");

        VBox textoHeader = new VBox(2, lblTitulo, lblSub);

        // Ícono simple con texto
        Label icono = new Label("⊛");
        icono.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        HBox header = new HBox(12, icono, textoHeader);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefWidth(Double.MAX_VALUE);

        // ── CARD ─────────────────────────────────────────
        Label cuTag = new Label("CU-05");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Ingreso al sistema");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label cardSub = new Label("Ingrese sus credenciales para continuar.");
        cardSub.getStyleClass().add("card-subtitulo");

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        Label lblDni = new Label("DNI:");
        TextField txtDni = new TextField();
        txtDni.setPromptText("Ingrese su DNI (sin puntos ni guiones)");
        txtDni.setPrefWidth(320);

        Label lblPass = new Label("Contraseña:");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Ingrese su contraseña");
        txtPass.setPrefWidth(320);

        Label lblMensaje = new Label();
        lblMensaje.getStyleClass().add("msg-error");
        lblMensaje.setWrapText(true);

        Button btnIngresar = new Button("INGRESAR AL SISTEMA");
        btnIngresar.getStyleClass().add("btn-primario");
        btnIngresar.setPrefWidth(320);
        btnIngresar.setPrefHeight(42);

        Button btnSalir = new Button("Salir");
        btnSalir.getStyleClass().add("btn-secundario");
        btnSalir.setPrefWidth(320);

        btnIngresar.setOnAction(e -> intentarLogin(txtDni, txtPass, lblMensaje));
        txtPass.setOnAction(e -> btnIngresar.fire());
        btnSalir.setOnAction(e -> { stage.close(); System.exit(0); });

        VBox card = new VBox(12,
                cuTag, cardTitulo, cardSub, sep,
                lblDni, txtDni,
                lblPass, txtPass,
                lblMensaje,
                btnIngresar, btnSalir
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(380);
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

        Scene scene = new Scene(root, 500, 560);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("SGCA-TEP - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void intentarLogin(TextField txtDni, PasswordField txtPass, Label lblMensaje) {
        String dni = txtDni.getText().trim();
        String pass = txtPass.getText().trim();

        if (dni.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Debe completar todos los campos.");
            return;
        }
        try {
            Usuario usuario = gestorAcceso.login(dni, pass);
            if (usuario != null) {
                MenuPrincipalFX menu = new MenuPrincipalFX();
                menu.show(stage, usuario);
            } else {
                lblMensaje.setText("DNI o contraseña incorrectos.");
                txtPass.clear();
            }
        } catch (SQLException ex) {
            lblMensaje.setText("Error de conexión: " + ex.getMessage());
        }
    }
}