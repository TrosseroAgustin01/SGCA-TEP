package com.sgcatep.view;

import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorFichada;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class FichadaFX {

    private final GestorFichada gestorFichada = new GestorFichada();

    public void show(Stage stage, Usuario usuario) {

        // header
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

        // contenido
        Label cuTag = new Label("CU-04");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Registrar fichada de presencia");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Seleccione el tipo de acceso y la modalidad de trabajo.");
        cardSub.getStyleClass().add("card-subtitulo");

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 0, 4, 0));

        // grilla con los datos del usuario
        Label hdrUsuario = new Label("USUARIO");
        hdrUsuario.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888; -fx-font-weight: bold;");
        Label valUsuario = new Label(usuario.getNombre() + " " + usuario.getApellido());
        valUsuario.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label hdrArea = new Label("ÁREA");
        hdrArea.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888; -fx-font-weight: bold;");
        Label valArea = new Label(usuario.getArea().getNombre());
        valArea.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox colUsuario = new VBox(3, hdrUsuario, valUsuario);
        VBox colArea = new VBox(3, hdrArea, valArea);

        HBox infoBox = new HBox(30, colUsuario, colArea);
        infoBox.setStyle("-fx-background-color: #f5f7fa; -fx-padding: 12px; -fx-background-radius: 4px;");

        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(4, 0, 4, 0));

        // entrada/salida
        Label lblTipo = new Label("Tipo de acceso:");
        lblTipo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("ENTRADA", "SALIDA");
        cmbTipo.setValue("ENTRADA");
        cmbTipo.setPrefWidth(320);
        cmbTipo.getStyleClass().add("combo-box");

        // presencial/remoto
        Label lblModalidad = new Label("Modalidad:");
        lblModalidad.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbModalidad = new ComboBox<>();
        cmbModalidad.getItems().addAll("PRESENCIAL", "REMOTA");
        cmbModalidad.setValue("PRESENCIAL");
        cmbModalidad.setPrefWidth(320);
        cmbModalidad.getStyleClass().add("combo-box");

        // alerta de que revisamos si cuenta con autorizacion para trabajar remoto
        Label lblAviso = new Label("La autorización de trabajo remoto se validará automáticamente.");
        lblAviso.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; "
                + "-fx-background-color: #e3f2fd; -fx-padding: 8px 12px; "
                + "-fx-background-radius: 4px;");
        lblAviso.setWrapText(true);
        lblAviso.setVisible(false);
        lblAviso.setManaged(false);

        cmbModalidad.valueProperty().addListener((obs, anterior, nuevo) -> {
            boolean esRemota = "REMOTA".equals(nuevo);
            lblAviso.setVisible(esRemota);
            lblAviso.setManaged(esRemota);
        });

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(320);

        // botones
        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(40);

        Button btnRegistrar = new Button("REGISTRAR FICHADA");
        btnRegistrar.getStyleClass().add("btn-primario");
        btnRegistrar.setPrefHeight(40);

        HBox botonesBox = new HBox(12, btnCancelar, btnRegistrar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnRegistrar.setOnAction(e -> {
            try {
                Fichada.Tipo tipo = Fichada.Tipo.valueOf(cmbTipo.getValue());
                Fichada.Modalidad modalidad = Fichada.Modalidad.valueOf(cmbModalidad.getValue());

                Fichada fichada = gestorFichada.registrarFichada(usuario, tipo, modalidad);

                if (fichada != null) {
                    String hora = fichada.getFechaHora()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    lblMensaje.getStyleClass().setAll("msg-exito");
                    lblMensaje.setText("✓ Fichada registrada — Tipo: " + tipo
                            + " | Modalidad: " + modalidad + " | Hora: " + hora);
                } else {
                    lblMensaje.getStyleClass().setAll("msg-error");
                    lblMensaje.setText("✗ " + (modalidad == Fichada.Modalidad.REMOTA
                            ? "No posee autorización remota vigente."
                            : "No se pudo registrar la fichada."));
                }
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Error: " + ex.getMessage());
            }
        });

        btnCancelar.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, usuario);
        });

        VBox card = new VBox(12,
                cuTag, cardTitulo, cardSub, sep1,
                infoBox, sep2,
                lblTipo, cmbTipo,
                lblModalidad, cmbModalidad,
                lblAviso,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER_LEFT);

        // pie
        Label footer = new Label("SGCA-TEP · Sistema de Gestión y Control de Acceso Integrado");
        footer.getStyleClass().add("footer-texto");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12));

        // estilos
        StackPane centro = new StackPane(card);
        centro.setPadding(new Insets(30));
        VBox.setVgrow(centro, Priority.ALWAYS);

        VBox root = new VBox(header, centro, footerBox);

        Scene scene = new Scene(root, 520, 600);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Registrar Fichada");
        stage.setScene(scene);
    }
}