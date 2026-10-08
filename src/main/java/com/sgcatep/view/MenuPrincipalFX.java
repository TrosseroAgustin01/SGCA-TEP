package com.sgcatep.view;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalFX {

    public void show(Stage stage, Usuario usuario) {
        Label titulo = new Label("Menú Principal");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        String rolesTexto = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin rol");

        Label lblUsuario = new Label("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());
        lblUsuario.setFont(Font.font("Arial", 14));

        Label lblArea = new Label("Área: " + usuario.getArea().getNombre());
        lblArea.setFont(Font.font("Arial", 14));

        Label lblRol = new Label("Rol: " + rolesTexto);
        lblRol.setFont(Font.font("Arial", 14));

        Button btnFichada = new Button("Registrar Fichada");
        btnFichada.setPrefWidth(300);
        btnFichada.setPrefHeight(40);
        btnFichada.setStyle("-fx-font-size: 14px;");

        Button btnAsignarRoles = new Button("Asignar Roles y Permisos");
        btnAsignarRoles.setPrefWidth(300);
        btnAsignarRoles.setPrefHeight(40);
        btnAsignarRoles.setStyle("-fx-font-size: 14px;");

        Button btnCerrarSesion = new Button("Cerrar Sesión");
        btnCerrarSesion.setPrefWidth(300);
        btnCerrarSesion.setPrefHeight(40);
        btnCerrarSesion.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px;");

        // Solo Administradores pueden asignar roles
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("Administrador"));
        btnAsignarRoles.setDisable(!esAdmin);

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

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titulo, lblUsuario, lblArea, lblRol,
                btnFichada, btnAsignarRoles, btnCerrarSesion);

        Scene scene = new Scene(layout, 500, 450);
        stage.setTitle("SGCA-TEP - Menú Principal");
        stage.setScene(scene);
    }
}