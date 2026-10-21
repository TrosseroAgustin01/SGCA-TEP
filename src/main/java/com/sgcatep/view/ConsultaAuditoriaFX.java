package com.sgcatep.view;

import com.sgcatep.controller.ConsultaController;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsultaAuditoriaFX {

    private static final String TODAS = "Todas";

    private final ConsultaController controller = new ConsultaController();
    private Scene escena;

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

        Label cuTag = new Label("CU-06 · AUDITORÍA");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Registros de auditoría");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label lblAlcance = new Label();
        lblAlcance.setStyle("-fx-font-size: 12px; -fx-text-fill: #1b3a5c; "
                + "-fx-background-color: #e3f2fd; -fx-padding: 8px 12px; -fx-background-radius: 4px;");
        lblAlcance.setWrapText(true);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        List<Area> areas = new ArrayList<>();
        try {
            areas.addAll(controller.areas());
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando áreas: " + ex.getMessage());
        }

        Label lblArea = new Label("Área del usuario:");
        lblArea.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<String> cmbArea = new ComboBox<>();
        cmbArea.getItems().add(TODAS);
        for (Area a : areas) {
            cmbArea.getItems().add(a.getNombre());
        }
        cmbArea.setValue(TODAS);
        cmbArea.setPrefWidth(320);
        cmbArea.getStyleClass().add("combo-box");

        HBox filtros = new HBox(14, lblArea, cmbArea);
        filtros.setAlignment(Pos.CENTER_LEFT);

        Label lblCantidad = new Label();
        lblCantidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Pagination paginacion = new Pagination(1);
        paginacion.setMaxPageIndicatorCount(5);
        paginacion.setPrefHeight(340);

        Runnable refrescar = () -> {
            int ia = cmbArea.getSelectionModel().getSelectedIndex();
            Integer idArea = ia <= 0 ? null : areas.get(ia - 1).getIdArea();
            try {
                ConsultaController.PaginaAuditoria inicio = controller.auditoria(usuario, idArea, 0);
                lblAlcance.setText(inicio.alcance());
                lblCantidad.setText(inicio.total() + " registros");
                paginacion.setPageCount(
                        Math.max(1, (int) Math.ceil(inicio.total() / (double) ConsultaController.TAMANIO)));
                paginacion.setPageFactory(p -> crearPagina(usuario, idArea, p, lblMensaje));
                paginacion.setCurrentPageIndex(0);
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("");
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Error cargando registros: " + ex.getMessage());
            }
        };

        cmbArea.getSelectionModel().selectedIndexProperty().addListener((obs, a, b) -> refrescar.run());

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(44);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(16, btnVolver);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, usuario));

        VBox card = new VBox(15,
                cuTag, cardTitulo, lblAlcance, filtros, lblCantidad,
                paginacion, lblMensaje, botonesBox);
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

        VBox root = new VBox(header, scroll, footerBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        escena = new Scene(root, 640, 660);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) escena.getStylesheets().add(cssUrl.toExternalForm());

        Escala.ajustarCard(card, scroll);
        paginacion.prefHeightProperty().bind(escena.heightProperty().multiply(0.42));

        refrescar.run();

        stage.setTitle("SGCA-TEP - Registros de auditoría");
        stage.setScene(escena);
        stage.setResizable(true);
    }

    private ListView<String> crearPagina(Usuario usuario, Integer idArea, int pagina, Label lblMensaje) {
        ListView<String> lista = new ListView<>();
        lista.setPlaceholder(new Label("Sin registros para mostrar"));
        lista.prefHeightProperty().bind(escena.heightProperty().multiply(0.38));
        try {
            lista.getItems().addAll(controller.auditoria(usuario, idArea, pagina).lineas());
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando página: " + ex.getMessage());
        }
        return lista;
    }
}