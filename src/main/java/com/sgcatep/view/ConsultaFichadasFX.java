package com.sgcatep.view;

import com.sgcatep.controller.ConsultaController;
import com.sgcatep.model.domain.Fichada;
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

public class ConsultaFichadasFX {

    private static final int TAMANIO = 10;
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

        Label cuTag = new Label("CU-06 · CONSULTA DE FICHADAS");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Fichadas registradas");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label lblAlcance = new Label();
        lblAlcance.setStyle("-fx-font-size: 12px; -fx-text-fill: #1b3a5c; "
                + "-fx-background-color: #e3f2fd; -fx-padding: 8px 12px; -fx-background-radius: 4px;");
        lblAlcance.setWrapText(true);

        List<Fichada> base = new ArrayList<>();
        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        try {
            ConsultaController.Vista v = controller.fichadasVisibles(usuario);
            lblAlcance.setText(v.alcance());
            base.addAll(v.fichadas());
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando fichadas: " + ex.getMessage());
        }

        Label lblArea = new Label("Área del usuario:");
        lblArea.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<String> cmbArea = new ComboBox<>();
        cmbArea.getItems().add(TODAS);
        base.stream().map(f -> f.getUsuario().getArea().getNombre())
                .distinct().sorted().forEach(cmbArea.getItems()::add);
        cmbArea.setValue(TODAS);
        cmbArea.setPrefWidth(320);
        cmbArea.getStyleClass().add("combo-box");

        HBox filtros = new HBox(16, lblArea, cmbArea);
        filtros.setAlignment(Pos.CENTER_LEFT);

        Label lblCantidad = new Label();
        lblCantidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Pagination paginacion = new Pagination(1);
        paginacion.setMaxPageIndicatorCount(5);
        paginacion.setPrefHeight(340);

        Runnable refrescar = () -> {
            List<Fichada> filtradas = base.stream()
                    .filter(f -> TODAS.equals(cmbArea.getValue())
                            || f.getUsuario().getArea().getNombre().equals(cmbArea.getValue()))
                    .toList();
            int paginas = Math.max(1, (int) Math.ceil(filtradas.size() / (double) TAMANIO));
            paginacion.setPageCount(paginas);
            paginacion.setPageFactory(p -> crearPagina(filtradas, p));
            paginacion.setCurrentPageIndex(0);
            lblCantidad.setText(filtradas.size() + " registros");
        };

        cmbArea.valueProperty().addListener((obs, anterior, nuevo) -> refrescar.run());

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(46);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(16, btnVolver);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnVolver.setOnAction(e -> new MenuPrincipalFX().show(stage, usuario));

        VBox card = new VBox(18,
                cuTag, cardTitulo, lblAlcance, filtros, lblCantidad,
                paginacion, lblMensaje, botonesBox);
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

        VBox root = new VBox(header, scroll, footerBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        escena = new Scene(root, 640, 660);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) escena.getStylesheets().add(cssUrl.toExternalForm());

        card.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(560, scroll.getWidth() * 0.6), scroll.widthProperty()));
        paginacion.prefHeightProperty().bind(escena.heightProperty().multiply(0.5));

        refrescar.run();

        stage.setTitle("SGCA-TEP - Consulta de Fichadas");
        stage.setScene(escena);
        stage.setResizable(true);
    }

    private ListView<String> crearPagina(List<Fichada> lista, int pagina) {
        ListView<String> vista = new ListView<>();
        vista.setPlaceholder(new Label("Sin fichadas para mostrar"));
        vista.prefHeightProperty().bind(escena.heightProperty().multiply(0.45));
        lista.stream().skip((long) pagina * TAMANIO).limit(TAMANIO)
                .map(ConsultaController::linea)
                .forEach(vista.getItems()::add);
        return vista;
    }
}