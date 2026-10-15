package com.sgcatep.view;

import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.SistemaRepository;
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
import java.util.List;
import java.util.Map;

public class SolicitarAccesoFX {

    private final SistemaRepository sistemaRepository = new SistemaRepository();
    private final GestorAcceso gestorAcceso = new GestorAcceso();

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

        Label cuTag = new Label("CU-05");
        cuTag.getStyleClass().add("cu-tag");

        Label cardTitulo = new Label("Solicitar acceso a sistema");
        cardTitulo.getStyleClass().add("card-titulo");
        cardTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label cardSub = new Label("El acceso se autoriza si se cumplen tres validaciones en orden.");
        cardSub.getStyleClass().add("card-subtitulo");
        cardSub.setWrapText(true);

        Label lblPasos = new Label("1) Usuario activo    2) Permisos sobre el sistema    3) Presencia válida hoy");
        lblPasos.setStyle("-fx-font-size: 12px; -fx-text-fill: #1b3a5c; "
                + "-fx-background-color: #e3f2fd; -fx-padding: 8px 12px; -fx-background-radius: 4px;");
        lblPasos.setWrapText(true);

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 0, 4, 0));

        Label lblUsuario = new Label("Usuario solicitante: " + usuario.getNombre() + " " + usuario.getApellido());
        lblUsuario.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label lblSistema = new Label("Sistema al que se solicita acceso:");
        lblSistema.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<String> cmbSistema = new ComboBox<>();
        cmbSistema.setPromptText("Seleccione un sistema");
        cmbSistema.getStyleClass().add("combo-box");

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        try {
            List<Sistema> sistemas = sistemaRepository.listarActivos();
            for (Sistema s : sistemas) {
                cmbSistema.getItems().add(s.getIdSistema() + " - " + s.getNombre());
            }
        } catch (SQLException ex) {
            lblMensaje.getStyleClass().setAll("msg-error");
            lblMensaje.setText("Error cargando sistemas: " + ex.getMessage());
        }

        Button btnVolver = new Button("VOLVER");
        btnVolver.getStyleClass().add("btn-secundario");
        btnVolver.setPrefHeight(40);
        btnVolver.setMinWidth(Region.USE_PREF_SIZE);

        Button btnSolicitar = new Button("SOLICITAR ACCESO");
        btnSolicitar.getStyleClass().add("btn-primario");
        btnSolicitar.setPrefHeight(40);
        btnSolicitar.setMinWidth(Region.USE_PREF_SIZE);

        HBox botonesBox = new HBox(12, btnVolver, btnSolicitar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        btnSolicitar.setOnAction(e -> {
            if (cmbSistema.getValue() == null) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Debe seleccionar un sistema.");
                return;
            }
            try {
                int idSistema = extraerId(cmbSistema.getValue());
                Sistema sistema = sistemaRepository.buscarPorId(idSistema);

                Map<String, Object> resultado = gestorAcceso.solicitarAcceso(usuario, sistema);
                boolean autorizado = (boolean) resultado.get("autorizado");
                String motivo = (String) resultado.get("motivo");

                lblMensaje.getStyleClass().setAll(autorizado ? "msg-exito" : "msg-error");
                lblMensaje.setText((autorizado ? "✓ ACCESO AUTORIZADO" : "✗ ACCESO DENEGADO")
                        + " — " + motivo + "\nSistema: " + sistema.getNombre());
            } catch (SQLException ex) {
                lblMensaje.getStyleClass().setAll("msg-error");
                lblMensaje.setText("Error de base de datos: " + ex.getMessage());
            }
        });

        btnVolver.setOnAction(e -> {
            MenuPrincipalFX menu = new MenuPrincipalFX();
            menu.show(stage, usuario);
        });

        VBox card = new VBox(12,
                cuTag, cardTitulo, cardSub, lblPasos, sep1,
                lblUsuario,
                lblSistema, cmbSistema,
                lblMensaje,
                botonesBox
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(560);
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

        Scene scene = new Scene(root, 640, 620);
        var cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SGCA-TEP - Solicitar Acceso");
        stage.setScene(scene);
        stage.setResizable(true);
    }

    private int extraerId(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0].trim());
    }
}