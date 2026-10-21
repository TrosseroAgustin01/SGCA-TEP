package com.sgcatep.view;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

final class Escala {

    private Escala() {
    }

    static void ajustarCard(Region card, ScrollPane scroll) {
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(720, Math.max(520, scroll.getWidth() * 0.4)), scroll.widthProperty()));
    }
}