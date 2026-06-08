package com.transport.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Helpers UI réutilisables pour tous les contrôleurs.
 */
public class UIHelper {

    /** En-tête de module (titre + boutons) */
    public static HBox enteteModule(String titre, javafx.scene.Node... actions) {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(0, 0, 18, 0));
        Label l = new Label(titre);
        l.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        h.getChildren().add(l);
        h.getChildren().add(sp);
        for (javafx.scene.Node n : actions) h.getChildren().add(n);
        return h;
    }

    /** Carte blanche avec ombre */
    public static VBox card() {
        VBox c = new VBox(0);
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");
        return c;
    }

    /** Bouton principal bleu */
    public static Button btnPrimary(String texte) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 16;-fx-font-size:12px;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:#1d4ed8;-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 16;-fx-font-size:12px;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 16;-fx-font-size:12px;"));
        return b;
    }

    /** Bouton secondaire gris */
    public static Button btnSecondary(String texte) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 14;-fx-font-size:12px;" +
            "-fx-border-color:#e2e8f0;-fx-border-radius:8;");
        return b;
    }

    /** Petit bouton coloré pour les tables */
    public static Button btnTable(String texte, String bg) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:6;" +
            "-fx-cursor:hand;-fx-font-size:11px;-fx-padding:4 10;");
        return b;
    }

    /** Label champ de formulaire */
    public static Label champLabel(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        return l;
    }

    /** Champ texte stylé */
    public static TextField champTexte(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#cbd5e1;" +
            "-fx-padding:7 10;-fx-font-size:12px;-fx-background-color:white;");
        tf.setPrefWidth(260);
        return tf;
    }

    /** ComboBox stylée */
    public static <T> ComboBox<T> champCombo() {
        ComboBox<T> cb = new ComboBox<>();
        cb.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#cbd5e1;" +
            "-fx-background-color:white;");
        cb.setPrefWidth(260);
        return cb;
    }

    /** Badge statut coloré */
    public static Label badge(String texte, String bg, String fg) {
        Label l = new Label(texte);
        l.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg +
            ";-fx-background-radius:12;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        return l;
    }

    /** Séparateur fin */
    public static Separator sep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:#f1f5f9;");
        return s;
    }

    /** Groupe formulaire label + champ */
    public static VBox groupe(String label, javafx.scene.Node champ) {
        VBox v = new VBox(5, champLabel(label), champ);
        return v;
    }
}
