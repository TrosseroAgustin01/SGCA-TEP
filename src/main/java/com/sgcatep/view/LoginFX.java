package com.sgcatep.view;

import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorAcceso;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginFX {

    private final GestorAcceso gestorAcceso = new GestorAcceso();
    private Stage stage;

    public void show(Stage primaryStage) {
        this.stage = primaryStage;

        Label titulo = new Label("SGCA-TEP");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitulo = new Label("Sistema de Gestión y Control de Acceso");
        subtitulo.setFont(Font.font("Arial", 14));

        Label lblDni = new Label("DNI:");
        TextField txtDni = new TextField();
        txtDni.setPromptText("Ingrese su DNI (sin puntos ni guiones)");
        txtDni.setMaxWidth(250);

        Label lblPassword = new Label("Contraseña:");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Ingrese su contraseña");
        txtPassword.setMaxWidth(250);

        Label lblMensaje = new Label();
        lblMensaje.setStyle("-fx-text-fill: red;");

        Button btnIngresar = new Button("Ingresar");
        btnIngresar.setPrefWidth(250);
        btnIngresar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");

        Button btnSalir = new Button("Salir");
        btnSalir.setPrefWidth(250);

        btnIngresar.setOnAction(e -> {
            String dni = txtDni.getText().trim();
            String password = txtPassword.getText().trim();

            if (dni.isEmpty() || password.isEmpty()) {
                lblMensaje.setText("Debe completar todos los campos");
                return;
            }

            try {
                Usuario usuario = gestorAcceso.login(dni, password);
                if (usuario != null) {
                    lblMensaje.setStyle("-fx-text-fill: green;");
                    lblMensaje.setText("Bienvenido " + usuario.getNombre() + " " + usuario.getApellido());

                    MenuPrincipalFX menu = new MenuPrincipalFX();
                    menu.show(stage, usuario);
                } else {
                    lblMensaje.setStyle("-fx-text-fill: red;");
                    lblMensaje.setText("DNI o contraseña incorrectos");
                    txtPassword.clear();
                }
            } catch (SQLException ex) {
                lblMensaje.setText("Error de conexión: " + ex.getMessage());
            }
        });

        txtPassword.setOnAction(e -> btnIngresar.fire());

        btnSalir.setOnAction(e -> {
            stage.close();
            System.exit(0);
        });

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titulo, subtitulo, lblDni, txtDni, lblPassword, txtPassword,
                lblMensaje, btnIngresar, btnSalir);

        Scene scene = new Scene(layout, 450, 500);
        stage.setTitle("SGCA-TEP - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}