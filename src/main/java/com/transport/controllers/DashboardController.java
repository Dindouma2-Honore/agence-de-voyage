package com.transport.controllers;

import com.transport.dao.*;
import com.transport.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    private final UtilisateurDAO userDAO  = new UtilisateurDAO();
    private final VehiculeDAO    vehDAO   = new VehiculeDAO();
    private final VoyageDAO      voyDAO   = new VoyageDAO();
    private final ReservationDAO resDAO   = new ReservationDAO();
    private final ColisDAO       colDAO   = new ColisDAO();

    public ScrollPane getView() {
        // ── Données ──────────────────────────────────────────────────
        int clients    = userDAO.compterParRole("CLIENT");
        int chauffeurs = userDAO.compterParRole("CHAUFFEUR");
        int vhDispo    = vehDAO.compterParEtat("DISPONIBLE");
        int vhMaint    = vehDAO.compterParEtat("EN_MAINTENANCE");
        int voyPlan    = voyDAO.compterParStatut("PLANIFIE");
        int voyEnCours = voyDAO.compterParStatut("EN_COURS");
        int resConf    = resDAO.compterParStatut("CONFIRMEE");
        int colisEnAtt = colDAO.compterParStatut("EN_ATTENTE");
        double revJour = resDAO.totalRevenusJour();
        double revMois = resDAO.totalRevenusMois();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);

        String nom = SessionManager.getInstance().getUtilisateurConnecte().getNomComplet();

        // ── Root ─────────────────────────────────────────────────────
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        page.setStyle("-fx-background-color:#f1f5f9;");

        // ── Titre page ───────────────────────────────────────────────
        HBox titreBox = new HBox(12);
        titreBox.setAlignment(Pos.CENTER_LEFT);
        VBox titreVbox = new VBox(2);
        Label lTitre = new Label("Tableau de bord");
        lTitre.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        Label lSous = new Label("Bonjour " + nom + " — Bienvenue sur SIGAVT");
        lSous.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;");
        titreVbox.getChildren().addAll(lTitre, lSous);
        Region spT = new Region(); HBox.setHgrow(spT, Priority.ALWAYS);
        Label lDate = new Label(java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        lDate.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
        titreBox.getChildren().addAll(titreVbox, spT, lDate);

        // ── Rangée 1 : KPI Cards ─────────────────────────────────────
        HBox kpiRow = new HBox(16);
        kpiRow.getChildren().addAll(
            kpiCard("✈️", "Voyages planifiés",  String.valueOf(voyPlan),    "+2 cette semaine",  "#dbeafe","#1d4ed8","#2563eb"),
            kpiCard("🎫", "Réservations",       String.valueOf(resConf),    "Confirmées",         "#dcfce7","#15803d","#16a34a"),
            kpiCard("👥", "Clients",            String.valueOf(clients),    chauffeurs+" chauffeurs","#f3e8ff","#6d28d9","#7c3aed"),
            kpiCard("🚌", "Véhicules dispo",    String.valueOf(vhDispo),    vhMaint+" en maintenance","#ffedd5","#c2410c","#ea580c"),
            kpiCard("📦", "Colis en attente",   String.valueOf(colisEnAtt), "À traiter",          "#fce7f3","#be185d","#db2777")
        );
        for (javafx.scene.Node n : kpiRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Rangée 2 : Revenus + Graphique ───────────────────────────
        HBox row2 = new HBox(16);

        // Revenus
        VBox revenus = card("💰  Revenus");
        revenus.setPrefWidth(260);
        revenus.setMinWidth(240);
        revenus.getChildren().addAll(
            revenuLigne("Aujourd'hui",    nf.format((long)revJour) + " FCFA", "#16a34a"),
            new Separator(),
            revenuLigne("Ce mois",        nf.format((long)revMois) + " FCFA", "#2563eb"),
            new Separator(),
            revenuLigne("Voyages en cours", String.valueOf(voyEnCours), "#ea580c"),
            new Separator(),
            revenuLigne("Colis transit",  String.valueOf(colDAO.compterParStatut("EN_TRANSIT")), "#7c3aed")
        );

        // Graphique barres
        VBox graphCard = card("📊  Activité mensuelle");
        HBox.setHgrow(graphCard, Priority.ALWAYS);
        Canvas canvas = construireGraphique(voyPlan, voyEnCours,
            voyDAO.compterParStatut("TERMINE"), resConf, colisEnAtt);
        graphCard.getChildren().add(canvas);

        row2.getChildren().addAll(revenus, graphCard);

        // ── Rangée 3 : Voyages récents + Statut véhicules ────────────
        HBox row3 = new HBox(16);

        VBox recentVoy = card("🕐  Voyages récents");
        HBox.setHgrow(recentVoy, Priority.ALWAYS);
        recentVoy.getChildren().addAll(
            ligneActivite("Yaoundé → Douala",   "06:00",  "PLANIFIE",   "Paul Mbarga"),
            ligneActivite("Douala → Kribi",     "08:30",  "EN_COURS",   "Jean Fotso"),
            ligneActivite("Yaoundé → Bafoussam","10:00",  "PLANIFIE",   "Marc Essomba"),
            ligneActivite("Ngaoundéré → Garoua","12:00",  "TERMINE",    "André Biyong"),
            ligneActivite("Douala → Limbé",     "14:30",  "ANNULE",     "Pierre Nkomo")
        );

        VBox statutVeh = card("🚌  Statut Parc Auto");
        statutVeh.setPrefWidth(260);
        statutVeh.setMinWidth(240);
        statutVeh.getChildren().addAll(
            jaugeStatut("Disponibles",     vhDispo, vhDispo + vhMaint + vehDAO.compterParEtat("EN_SERVICE"), "#16a34a"),
            jaugeStatut("En service",      vehDAO.compterParEtat("EN_SERVICE"), vhDispo + vhMaint + vehDAO.compterParEtat("EN_SERVICE"), "#2563eb"),
            jaugeStatut("En maintenance",  vhMaint, vhDispo + vhMaint + vehDAO.compterParEtat("EN_SERVICE"), "#ea580c"),
            jaugeStatut("Hors service",    vehDAO.compterParEtat("HORS_SERVICE"), vhDispo + vhMaint + vehDAO.compterParEtat("EN_SERVICE") + 1, "#dc2626")
        );

        row3.getChildren().addAll(recentVoy, statutVeh);

        // ── Footer ───────────────────────────────────────────────────
        Label footer = new Label("SIGAVT v1.0 — Système de Gestion d'Agence de Voyage Terrestre — Cameroun");
        footer.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        page.getChildren().addAll(titreBox, kpiRow, row2, row3, footer);

        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#f1f5f9;-fx-background-color:#f1f5f9;");
        return sp;
    }

    // ─── KPI Card ─────────────────────────────────────────────────
    private VBox kpiCard(String ico, String titre, String valeur, String sous,
                         String bg, String couleur, String accent) {
        VBox c = new VBox(8);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");

        // Icône cercle coloré
        Label lIco = new Label(ico);
        lIco.setStyle("-fx-font-size:18px;-fx-background-color:" + bg +
            ";-fx-background-radius:20;-fx-padding:8 10;");

        Label lTitre = new Label(titre);
        lTitre.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;-fx-font-weight:bold;");

        Label lVal = new Label(valeur);
        lVal.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");

        Label lSous = new Label(sous);
        lSous.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        // Barre colorée en bas
        Region barre = new Region();
        barre.setPrefHeight(3);
        barre.setMaxWidth(Double.MAX_VALUE);
        barre.setStyle("-fx-background-color:" + accent + ";-fx-background-radius:2;");

        c.getChildren().addAll(lIco, lTitre, lVal, lSous, barre);
        return c;
    }

    // ─── Card générique ───────────────────────────────────────────
    private VBox card(String titre) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");
        Label lt = new Label(titre);
        lt.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        c.getChildren().addAll(lt, new Separator());
        return c;
    }

    // ─── Ligne revenu ─────────────────────────────────────────────
    private HBox revenuLigne(String label, String valeur, String couleur) {
        HBox h = new HBox();
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(4, 0, 4, 0));
        Label ll = new Label(label);
        ll.setStyle("-fx-font-size:12px;-fx-text-fill:#475569;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lv = new Label(valeur);
        lv.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");
        h.getChildren().addAll(ll, sp, lv);
        return h;
    }

    // ─── Ligne activité récente ───────────────────────────────────
    private HBox ligneActivite(String trajet, String heure, String statut, String chauffeur) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(6, 4, 6, 4));
        h.setStyle("-fx-border-color:transparent transparent #f1f5f9 transparent;");

        Label lTraj = new Label(trajet);
        lTraj.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        lTraj.setPrefWidth(160);

        Label lH = new Label(heure);
        lH.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
        lH.setPrefWidth(45);

        String[] sc = badgeStatut(statut);
        Label lStat = new Label(sc[0]);
        lStat.setStyle(sc[1]);
        lStat.setPrefWidth(90);

        Label lChauf = new Label("👤 " + chauffeur);
        lChauf.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;");

        h.getChildren().addAll(lTraj, lH, lStat, lChauf);
        return h;
    }

    // ─── Jauge statut véhicules ───────────────────────────────────
    private VBox jaugeStatut(String label, int val, int total, String couleur) {
        VBox v = new VBox(4);
        v.setPadding(new Insets(4, 0, 4, 0));
        HBox h = new HBox();
        Label ll = new Label(label);
        ll.setStyle("-fx-font-size:12px;-fx-text-fill:#475569;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lv = new Label(String.valueOf(val));
        lv.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");
        h.getChildren().addAll(ll, sp, lv);

        // Barre de progression
        double pct = total > 0 ? (double) val / total : 0;
        HBox barre = new HBox();
        Region remplie = new Region();
        remplie.setPrefWidth(Math.max(4, pct * 200));
        remplie.setPrefHeight(6);
        remplie.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:3;");
        Region vide = new Region();
        HBox.setHgrow(vide, Priority.ALWAYS);
        vide.setPrefHeight(6);
        vide.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:3;");
        barre.getChildren().addAll(remplie, vide);

        v.getChildren().addAll(h, barre);
        return v;
    }

    // ─── Graphique barres sur Canvas ──────────────────────────────
    private Canvas construireGraphique(int planifie, int enCours, int termine, int reservations, int colis) {
        Canvas canvas = new Canvas(520, 180);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int[] vals = {planifie, enCours, termine, reservations, colis};
        String[] labels = {"Planifiés", "En cours", "Terminés", "Réserv.", "Colis"};
        Color[] couleurs = {
            Color.web("#2563eb"), Color.web("#16a34a"),
            Color.web("#64748b"), Color.web("#7c3aed"), Color.web("#db2777")
        };

        int max = 1;
        for (int v : vals) if (v > max) max = v;

        double barW = 55, gap = 30, startX = 40, baseY = 155;

        for (int i = 0; i < vals.length; i++) {
            double x = startX + i * (barW + gap);
            double h = vals[i] == 0 ? 4 : (double) vals[i] / max * 120;
            double y = baseY - h;

            // Ombre légère
            gc.setFill(Color.web("#e2e8f0"));
            gc.fillRoundRect(x + 3, y + 3, barW, h, 6, 6);

            // Barre
            gc.setFill(couleurs[i]);
            gc.fillRoundRect(x, y, barW, h, 6, 6);

            // Valeur au-dessus
            gc.setFill(Color.web("#1e293b"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(String.valueOf(vals[i]), x + barW / 2 - 6, y - 5);

            // Label en bas
            gc.setFill(Color.web("#64748b"));
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(labels[i], x + barW / 2 - 20, baseY + 14);
        }

        // Ligne de base
        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(1);
        gc.strokeLine(30, baseY, 500, baseY);

        return canvas;
    }

    private String[] badgeStatut(String s) {
        return switch (s) {
            case "PLANIFIE"  -> new String[]{"Planifié",   "-fx-background-color:#dbeafe;-fx-text-fill:#1d4ed8;-fx-background-radius:10;-fx-padding:2 8;-fx-font-size:10px;-fx-font-weight:bold;"};
            case "EN_COURS"  -> new String[]{"En cours",   "-fx-background-color:#dcfce7;-fx-text-fill:#15803d;-fx-background-radius:10;-fx-padding:2 8;-fx-font-size:10px;-fx-font-weight:bold;"};
            case "TERMINE"   -> new String[]{"Terminé",    "-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-background-radius:10;-fx-padding:2 8;-fx-font-size:10px;-fx-font-weight:bold;"};
            case "ANNULE"    -> new String[]{"Annulé",     "-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;-fx-background-radius:10;-fx-padding:2 8;-fx-font-size:10px;-fx-font-weight:bold;"};
            default          -> new String[]{s,             "-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-background-radius:10;-fx-padding:2 8;-fx-font-size:10px;"};
        };
    }
}
