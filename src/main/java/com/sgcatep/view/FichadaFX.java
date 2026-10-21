package com.sgcatep.view;

import com.sgcatep.controller.FichadaController;
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

import java.sql.SQLException;

public class FichadaFX {

    private final FichadaController controller = new FichadaController();

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

        Label cuTag = new Label("CU-04");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Registrar fichada de presencia");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Seleccione el tipo de acceso y la modalidad de trabajo.");
        cardSub.getStyleClass().add("card-subtitulo");

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 0, 4, 0));

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

        Label lblTipo = new Label("Tipo de acceso:");
        lblTipo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("ENTRADA", "SALIDA");
        cmbTipo.setValue("ENTRADA");
        cmbTipo.setPrefWidth(320);
        cmbTipo.getStyleClass().add("combo-box");

        Label lblModalidad = new Label("Modalidad:");
        lblModalidad.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbModalidad = new ComboBox<>();
        cmbModalidad.getItems().addAll("PRESENCIAL", "REMOTA");
        cmbModalidad.setValue("PRESENCIAL");
        cmbModalidad.setPrefWidth(320);
        cmbModalidad.getStyleClass().add("combo-box");

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

        Button btnCancelar = new Button("CANCELAR");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setPrefHeight(44);
        btnCancelar.setMinWidth(Region.USE_PREF_SIZE);

        Button btnRegistrar = new Button("REGISTRAR FICHADA");
        btnRegistrar.getStyleClass().add("btn-primario");
        btnRegistrar.setPrefHeight(44);
        btnRegistrar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(14, btnCancelar, btnRegistrar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnRegistrar.setOnAction(e -> {
            try {
                FichadaController.Resultado r = controller.registrar(
                        usuario, cmbTipo.getValue(), cmbModalidad.getValue());
                lblMensaje.getStyleClass().setAll(r.registrada() ? "msg-exito" : "msg-error");
                lblMensaje.setText(r.mensaje());
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Error: " + ex.getMessage());
            }
        });

        btnCancelar.setOnAction(e -> new MenuPrincipalFX().show(stage, usuario));

        VBox card = new VBox(15,
                cuTag, cardTitulo, cardSub, sep1,
                infoBox, sep2,
                lblTipo, cmbTipo,
                lblModalidad, cmbModalidad,
                lblAviso,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
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

        VBox raiz = new VBox(header, scroll, footerBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(raiz, 560, 620);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Registrar Fichada");
        stage.setScene(scene);
        stage.setResizable(true);
    }
}