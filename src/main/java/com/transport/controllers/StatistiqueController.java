package com.transport.controllers;

import com.transport.dao.*;
import com.transport.utils.DatabaseConnection;
import com.transport.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

public class StatistiqueController {

    private final ReservationDAO resDAO  = new ReservationDAO();
    private final VoyageDAO      voyDAO  = new VoyageDAO();
    private final VehiculeDAO    vehDAO  = new VehiculeDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();
    private final ColisDAO       colDAO  = new ColisDAO();

    public ScrollPane getView() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        page.setStyle("-fx-background-color:#f1f5f9;");

        HBox entete = UIHelper.enteteModule("📈  Statistiques & Rapports");

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        double revJour  = resDAO.totalRevenusJour();
        double revMois  = resDAO.totalRevenusMois();
        double revTotal = totalRevenus();

        // ── KPI revenus ──────────────────────────────────────────────
        HBox kpiRev = new HBox(16);
        kpiRev.getChildren().addAll(
            kpiStat("💵 Aujourd'hui",  nf.format((long)revJour)  + " FCFA", "#16a34a","#dcfce7"),
            kpiStat("📅 Ce mois",      nf.format((long)revMois)  + " FCFA", "#2563eb","#dbeafe"),
            kpiStat("💰 Total général",nf.format((long)revTotal) + " FCFA", "#7c3aed","#f3e8ff")
        );
        for (javafx.scene.Node n : kpiRev.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Rangée graphiques ────────────────────────────────────────
        HBox rowGraphs = new HBox(16);

        // Graphique voyages
        VBox cardVoy = cardTitre("✈️  Voyages par statut");
        HBox.setHgrow(cardVoy, Priority.ALWAYS);
        int vPlan = voyDAO.compterParStatut("PLANIFIE");
        int vEnC  = voyDAO.compterParStatut("EN_COURS");
        int vTer  = voyDAO.compterParStatut("TERMINE");
        int vAnn  = voyDAO.compterParStatut("ANNULE");
        cardVoy.getChildren().add(barChart(
            new String[]{"Planifiés","En cours","Terminés","Annulés"},
            new int[]{vPlan, vEnC, vTer, vAnn},
            new String[]{"#2563eb","#16a34a","#64748b","#dc2626"}, 380, 160));

        // Graphique réservations
        VBox cardRes = cardTitre("🎫  Réservations par statut");
        HBox.setHgrow(cardRes, Priority.ALWAYS);
        int rConf = resDAO.compterParStatut("CONFIRMEE");
        int rAtt  = resDAO.compterParStatut("EN_ATTENTE");
        int rAnn  = resDAO.compterParStatut("ANNULEE");
        cardRes.getChildren().add(barChart(
            new String[]{"Confirmées","En attente","Annulées"},
            new int[]{rConf, rAtt, rAnn},
            new String[]{"#16a34a","#ea580c","#dc2626"}, 280, 160));

        rowGraphs.getChildren().addAll(cardVoy, cardRes);

        // ── Rangée tableaux détails ──────────────────────────────────
        HBox rowDetails = new HBox(16);

        // Réservations
        VBox statRes = cardTitre("🎫  Réservations");
        HBox.setHgrow(statRes, Priority.ALWAYS);
        statRes.getChildren().addAll(
            ligneStat("✅ Confirmées",   String.valueOf(rConf), "#16a34a"),
            UIHelper.sep(),
            ligneStat("⏳ En attente",   String.valueOf(rAtt),  "#ea580c"),
            UIHelper.sep(),
            ligneStat("✖ Annulées",     String.valueOf(rAnn),  "#dc2626"),
            UIHelper.sep(),
            ligneStat("✔ Utilisées",    String.valueOf(resDAO.compterParStatut("UTILISEE")), "#64748b")
        );

        // Voyages
        VBox statVoy = cardTitre("✈️  Voyages");
        HBox.setHgrow(statVoy, Priority.ALWAYS);
        statVoy.getChildren().addAll(
            ligneStat("📌 Planifiés",   String.valueOf(vPlan), "#2563eb"),
            UIHelper.sep(),
            ligneStat("🚀 En cours",   String.valueOf(vEnC),  "#16a34a"),
            UIHelper.sep(),
            ligneStat("✅ Terminés",    String.valueOf(vTer),  "#64748b"),
            UIHelper.sep(),
            ligneStat("✖ Annulés",     String.valueOf(vAnn),  "#dc2626")
        );

        // Véhicules
        VBox statVeh = cardTitre("🚌  Parc Auto");
        HBox.setHgrow(statVeh, Priority.ALWAYS);
        statVeh.getChildren().addAll(
            ligneStat("✅ Disponibles",    String.valueOf(vehDAO.compterParEtat("DISPONIBLE")),     "#16a34a"),
            UIHelper.sep(),
            ligneStat("🚀 En service",    String.valueOf(vehDAO.compterParEtat("EN_SERVICE")),     "#2563eb"),
            UIHelper.sep(),
            ligneStat("🔧 Maintenance",   String.valueOf(vehDAO.compterParEtat("EN_MAINTENANCE")), "#ea580c"),
            UIHelper.sep(),
            ligneStat("🚫 Hors service",  String.valueOf(vehDAO.compterParEtat("HORS_SERVICE")),   "#dc2626")
        );

        // Colis
        VBox statColis = cardTitre("📦  Colis");
        HBox.setHgrow(statColis, Priority.ALWAYS);
        statColis.getChildren().addAll(
            ligneStat("⏳ En attente",  String.valueOf(colDAO.compterParStatut("EN_ATTENTE")),  "#ea580c"),
            UIHelper.sep(),
            ligneStat("🚚 En transit", String.valueOf(colDAO.compterParStatut("EN_TRANSIT")),  "#2563eb"),
            UIHelper.sep(),
            ligneStat("✅ Livrés",      String.valueOf(colDAO.compterParStatut("LIVRE")),       "#16a34a"),
            UIHelper.sep(),
            ligneStat("↩️ Retournés", String.valueOf(colDAO.compterParStatut("RETOURNE")),    "#7c3aed")
        );

        rowDetails.getChildren().addAll(statRes, statVoy, statVeh, statColis);

        // ── Top destinations ─────────────────────────────────────────
        VBox topCard = cardTitre("🏆  Top 5 Destinations (par nombre de voyages)");
        topCard.getChildren().addAll(topDestinations());

        // ── Utilisateurs ─────────────────────────────────────────────
        VBox usersCard = cardTitre("👥  Utilisateurs enregistrés");
        HBox usersRow = new HBox(16);
        usersRow.getChildren().addAll(
            miniUser("👑 Admins",     String.valueOf(userDAO.compterParRole("ADMIN")),    "#7c3aed","#f3e8ff"),
            miniUser("🧑 Agents",    String.valueOf(userDAO.compterParRole("AGENT")),    "#2563eb","#dbeafe"),
            miniUser("🚌 Chauffeurs",String.valueOf(userDAO.compterParRole("CHAUFFEUR")),"#16a34a","#dcfce7"),
            miniUser("👤 Clients",   String.valueOf(userDAO.compterParRole("CLIENT")),   "#ea580c","#ffedd5")
        );
        for (javafx.scene.Node n : usersRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        usersCard.getChildren().add(usersRow);

        page.getChildren().addAll(entete, kpiRev, rowGraphs, rowDetails, topCard, usersCard);

        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#f1f5f9;-fx-background-color:#f1f5f9;");
        return sp;
    }

    private Canvas barChart(String[] labels, int[] vals, String[] colors, int width, int height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int max = 1; for (int v : vals) if (v > max) max = v;
        double barW = (double)(width - 60) / vals.length - 12;
        double startX = 30, baseY = height - 25;
        for (int i = 0; i < vals.length; i++) {
            double x = startX + i * (barW + 12);
            double h = vals[i] == 0 ? 4 : (double) vals[i] / max * (height - 50);
            double y = baseY - h;
            gc.setFill(Color.web(colors[i] + "33"));
            gc.fillRoundRect(x+2, y+2, barW, h, 6, 6);
            gc.setFill(Color.web(colors[i]));
            gc.fillRoundRect(x, y, barW, h, 6, 6);
            gc.setFill(Color.web("#1e293b"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(String.valueOf(vals[i]), x + barW/2 - 5, y - 4);
            gc.setFill(Color.web("#64748b"));
            gc.setFont(Font.font("Arial", 10));
            String lbl = labels[i].length() > 8 ? labels[i].substring(0,7)+"." : labels[i];
            gc.fillText(lbl, x + barW/2 - lbl.length()*3, baseY + 14);
        }
        gc.setStroke(Color.web("#e2e8f0")); gc.setLineWidth(1);
        gc.strokeLine(20, baseY, width - 10, baseY);
        return canvas;
    }

    private List<javafx.scene.Node> topDestinations() {
        List<javafx.scene.Node> rows = new ArrayList<>();
        String sql = "SELECT CONCAT(d.ville_depart,' → ',d.ville_arrivee), COUNT(v.id) AS nb " +
            "FROM voyages v JOIN destinations d ON v.destination_id=d.id GROUP BY d.id ORDER BY nb DESC LIMIT 5";
        try {
            Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            int rang = 1;
            String[] medals = {"🥇","🥈","🥉","4️⃣","5️⃣"};
            while (rs.next()) {
                rows.add(ligneStat(medals[rang-1] + "  " + rs.getString(1), rs.getString(2) + " voyage(s)", "#2563eb"));
                rows.add(UIHelper.sep());
                rang++;
            }
        } catch (SQLException e) { System.err.println("[Stat] topDest : " + e.getMessage()); }
        if (rows.isEmpty()) rows.add(ligneStat("Aucune donnée disponible","","#94a3b8"));
        return rows;
    }

    private double totalRevenus() {
        try {
            Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(montant),0) FROM paiements WHERE statut='PAYE'");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[Stat] totalRevenus : " + e.getMessage()); }
        return 0;
    }

    private VBox cardTitre(String titre) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");
        Label lt = new Label(titre);
        lt.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        c.getChildren().addAll(lt, UIHelper.sep());
        return c;
    }

    private HBox ligneStat(String label, String valeur, String couleur) {
        HBox h = new HBox(); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(4,0,4,0));
        Label ll = new Label(label); ll.setStyle("-fx-font-size:12px;-fx-text-fill:#475569;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lv = new Label(valeur); lv.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+couleur+";");
        h.getChildren().addAll(ll, sp, lv); return h;
    }

    private VBox kpiStat(String titre, String valeur, String fg, String bg) {
        VBox c = new VBox(6); c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");
        Label icoBg = new Label("●"); icoBg.setStyle("-fx-text-fill:"+fg+";-fx-font-size:10px;");
        Label lt = new Label(titre); lt.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;-fx-font-weight:bold;");
        Label lv = new Label(valeur); lv.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:"+fg+";");
        Region barre = new Region(); barre.setPrefHeight(3); barre.setMaxWidth(Double.MAX_VALUE);
        barre.setStyle("-fx-background-color:"+fg+";-fx-background-radius:2;");
        c.getChildren().addAll(lt, lv, barre); return c;
    }

    private HBox miniUser(String label, String val, String fg, String bg) {
        HBox h = new HBox(10); h.setPadding(new Insets(14,18,14,18));
        h.setStyle("-fx-background-color:"+bg+";-fx-background-radius:8;"); h.setAlignment(Pos.CENTER_LEFT);
        Label lv = new Label(val); lv.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"+fg+";");
        Label ll = new Label(label); ll.setStyle("-fx-font-size:11px;-fx-text-fill:"+fg+";");
        VBox v = new VBox(2, lv, ll); h.getChildren().add(v); return h;
    }
}
