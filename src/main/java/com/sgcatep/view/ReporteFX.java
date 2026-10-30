package com.sgcatep.view;

import com.sgcatep.controller.ReporteController;
import com.sgcatep.model.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ReporteFX {

    private final ReporteController controller = new ReporteController();
    private Usuario usuario;

    public void show(Stage stage, Usuario usuarioLogueado) {
        this.usuario = usuarioLogueado;

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

        Label cuTag = new Label("CU-07 · REPORTES");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Generar reporte");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("Exporta a CSV (separador ;) lo que su alcance le permite ver.");
        cardSub.getStyleClass().add("card-subtitulo");
        cardSub.setWrapText(true);

        Label lblTipo = new Label("Tipo de reporte:");
        lblTipo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("FICHADAS", "ACCESOS");
        cmbTipo.setValue("FICHADAS");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.getStyleClass().add("combo-box");

        Label lblPeriodo = new Label("Período:");
        lblPeriodo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        DatePicker dpDesde = new DatePicker();
        dpDesde.setPromptText("Desde");
        dpDesde.setMaxWidth(Double.MAX_VALUE);
        DatePicker dpHasta = new DatePicker();
        dpHasta.setPromptText("Hasta");
        dpHasta.setMaxWidth(Double.MAX_VALUE);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(46);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);
        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, usuario));

        Button btnGenerar = new Button("ELEGIR CARPETA Y GENERAR");
        btnGenerar.getStyleClass().add("btn-primario");
        btnGenerar.setPrefHeight(46);
        btnGenerar.setMinWidth(Region.USE_PREF_SIZE);
        btnGenerar.setOnAction(e -> generar(stage, cmbTipo.getValue(),
                dpDesde.getValue(), dpHasta.getValue(), lblMensaje));

        HBox botonesBox = new HBox(16, btnVolver, btnGenerar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(18,
                cuTag, cardTitulo, cardSub,
                lblTipo, cmbTipo,
                lblPeriodo, dpDesde, dpHasta,
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

        Scene scene = new Scene(root, 640, 600);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Generar reportes");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private void generar(Stage stage, String tipo, java.time.LocalDate desde,
                         java.time.LocalDate hasta, Label lblMensaje) {
        if (desde == null || hasta == null) {
            mostrar(lblMensaje, false, "Indique el período completo.");
            return;
        }
        DirectoryChooser selector = new DirectoryChooser();
        selector.setTitle("Carpeta de destino");
        File carpeta = selector.showDialog(stage);
        if (carpeta == null) {
            mostrar(lblMensaje, false, "Operación cancelada.");
            return;
        }
        ReporteController.Resultado r = controller.generar(usuario, tipo, desde, hasta, carpeta);
        mostrar(lblMensaje, r.ok(), r.mensaje());
    }

    private void mostrar(Label lbl, boolean ok, String texto) {
        lbl.getStyleClass().setAll(ok ? "msg-exito" : "msg-error");
        lbl.setText(texto);
    }
}