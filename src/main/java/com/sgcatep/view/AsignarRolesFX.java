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
import javafx.scene.layout.VBox;
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
        Label titulo = new Label("Asignar Roles y Permisos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label lblAdmin = new Label("Administrador: " + adminLogueado.getNombre() + " " + adminLogueado.getApellido());
        lblAdmin.setFont(Font.font("Arial", 12));

        Label lblUsuario = new Label("Seleccionar Usuario:");
        ComboBox<String> cmbUsuario = new ComboBox<>();
        cmbUsuario.setPrefWidth(300);

        Label lblArea = new Label("Seleccionar Área:");
        ComboBox<String> cmbArea = new ComboBox<>();
        cmbArea.setPrefWidth(300);

        Label lblRol = new Label("Seleccionar Rol:");
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.setPrefWidth(300);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        Button btnAsignar = new Button("Asignar Rol");
        btnAsignar.setPrefWidth(300);
        btnAsignar.setPrefHeight(40);
        btnAsignar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");

        Button btnQuitar = new Button("Quitar Rol");
        btnQuitar.setPrefWidth(300);
        btnQuitar.setPrefHeight(40);
        btnQuitar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px;");

        Button btnVolver = new Button("Volver al Menú");
        btnVolver.setPrefWidth(300);

        try {
            List<Usuario> usuarios = usuarioRepository.listarTodos();
            for (Usuario u : usuarios) {
                cmbUsuario.getItems().add(u.getIdUsuario() + " - " + u.getNombre() + " " + u.getApellido());
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
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Error cargando datos: " + ex.getMessage());
        }

        btnAsignar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null || cmbArea.getValue() == null || cmbRol.getValue() == null) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("Debe seleccionar usuario, área y rol");
                return;
            }

            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea = extraerId(cmbArea.getValue());
                int idRol = extraerId(cmbRol.getValue());

                Usuario usuarioSeleccionado = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSeleccionado = rolRepository.buscarPorId(idRol);

                boolean asignado = gestorAcceso.asignarRol(usuarioSeleccionado, rolSeleccionado, idArea);

                if (asignado) {
                    lblMensaje.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                    lblMensaje.setText("✓ Rol asignado correctamente\n" +
                            "Usuario: " + usuarioSeleccionado.getNombre() + "\n" +
                            "Rol: " + rolSeleccionado.getNombre());
                } else {
                    lblMensaje.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    lblMensaje.setText("✗ No se pudo asignar el rol\n" +
                            "Verifique que el usuario esté activo y el rol exista");
                }
            } catch (SQLException ex) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                if (ex.getMessage().contains("Duplicate")) {
                    lblMensaje.setText("✗ El usuario ya tiene ese rol en esa área");
                } else {
                    lblMensaje.setText("Error: " + ex.getMessage());
                }
            }
        });

        btnQuitar.setOnAction(e -> {
            if (cmbUsuario.getValue() == null || cmbArea.getValue() == null || cmbRol.getValue() == null) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("Debe seleccionar usuario, área y rol");
                return;
            }

            try {
                int idUsuario = extraerId(cmbUsuario.getValue());
                int idArea = extraerId(cmbArea.getValue());
                int idRol = extraerId(cmbRol.getValue());

                Usuario usuarioSeleccionado = usuarioRepository.buscarPorId(idUsuario);
                Rol rolSeleccionado = rolRepository.buscarPorId(idRol);

                boolean quitado = gestorAcceso.quitarRol(usuarioSeleccionado, rolSeleccionado, idArea);

                if (quitado) {
                    lblMensaje.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                    lblMensaje.setText("✓ Rol quitado correctamente");
                } else {
                    lblMensaje.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    lblMensaje.setText("✗ El usuario no tenía ese rol en esa área");
                }
            } catch (SQLException ex) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("Error: " + ex.getMessage());
            }
        });

        btnVolver.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, adminLogueado);
        });

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));
        layout.getChildren().addAll(titulo, lblAdmin,
                lblUsuario, cmbUsuario,
                lblArea, cmbArea,
                lblRol, cmbRol,
                lblMensaje, btnAsignar, btnQuitar, btnVolver);

        Scene scene = new Scene(layout, 500, 600);
        stage.setTitle("SGCA-TEP - Asignar Roles");
        stage.setScene(scene);
    }

    private int extraerId(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0].trim());
    }
}