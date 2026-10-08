package com.sgcatep.view;

import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorFichada;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class FichadaFX {

    private final GestorFichada gestorFichada = new GestorFichada();

    public void show(Stage stage, Usuario usuario) {
        Label titulo = new Label("Registrar Fichada");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label lblUsuario = new Label("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());
        lblUsuario.setFont(Font.font("Arial", 14));

        Label lblArea = new Label("Área: " + usuario.getArea().getNombre());
        lblArea.setFont(Font.font("Arial", 14));

        Label lblTipo = new Label("Tipo de fichada:");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("ENTRADA", "SALIDA");
        cmbTipo.setValue("ENTRADA");
        cmbTipo.setPrefWidth(250);

        Label lblModalidad = new Label("Modalidad:");
        ComboBox<String> cmbModalidad = new ComboBox<>();
        cmbModalidad.getItems().addAll("PRESENCIAL", "REMOTA");
        cmbModalidad.setValue("PRESENCIAL");
        cmbModalidad.setPrefWidth(250);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        Button btnRegistrar = new Button("Registrar Fichada");
        btnRegistrar.setPrefWidth(250);
        btnRegistrar.setPrefHeight(40);
        btnRegistrar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");

        Button btnVolver = new Button("Volver al Menú");
        btnVolver.setPrefWidth(250);

        btnRegistrar.setOnAction(e -> {
            try {
                Fichada.Tipo tipo = Fichada.Tipo.valueOf(cmbTipo.getValue());
                Fichada.Modalidad modalidad = Fichada.Modalidad.valueOf(cmbModalidad.getValue());

                Fichada fichada = gestorFichada.registrarFichada(usuario, tipo, modalidad);

                if (fichada != null) {
                    String hora = fichada.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    lblMensaje.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                    lblMensaje.setText("✓ Fichada registrada correctamente\n" +
                            "Tipo: " + tipo + " | Modalidad: " + modalidad + "\n" +
                            "Hora: " + hora);
                } else {
                    lblMensaje.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    if (modalidad == Fichada.Modalidad.REMOTA) {
                        lblMensaje.setText("✗ No posee autorización remota vigente");
                    } else {
                        lblMensaje.setText("✗ No se pudo registrar la fichada");
                    }
                }
            } catch (SQLException ex) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("Error: " + ex.getMessage());
            }
        });

        btnVolver.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, usuario);
        });

        VBox layout = new VBox(12);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(titulo, lblUsuario, lblArea,
                lblTipo, cmbTipo, lblModalidad, cmbModalidad,
                lblMensaje, btnRegistrar, btnVolver);

        Scene scene = new Scene(layout, 500, 520);
        stage.setTitle("SGCA-TEP - Registrar Fichada");
        stage.setScene(scene);
    }
}