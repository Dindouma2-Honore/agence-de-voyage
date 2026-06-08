package com.transport.controllers;

import com.transport.dao.ReservationDAO;
import com.transport.utils.DatabaseConnection;
import com.transport.utils.UIHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class PaiementController {

    private final ReservationDAO resDAO = new ReservationDAO();
    private BorderPane root;

    public PaiementController() { construireVue(); }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f5f9;");
        root.setPadding(new Insets(24));

        Button btnRef = UIHelper.btnSecondary("🔄  Actualiser");
        btnRef.setOnAction(e -> construireVue());
        HBox entete = UIHelper.enteteModule("💰  Gestion des Paiements", btnRef);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        double revJour  = resDAO.totalRevenusJour();
        double revMois  = resDAO.totalRevenusMois();
        double revTotal = totalRevenus();

        // KPI revenus
        HBox kpiRow = new HBox(16);
        kpiRow.setPadding(new Insets(0, 0, 16, 0));
        kpiRow.getChildren().addAll(
            kpiPaie("💵  Revenus aujourd'hui", nf.format((long)revJour) + " FCFA",  "#16a34a","#dcfce7"),
            kpiPaie("📅  Revenus ce mois",     nf.format((long)revMois) + " FCFA",  "#2563eb","#dbeafe"),
            kpiPaie("💰  Revenus totaux",       nf.format((long)revTotal) + " FCFA", "#7c3aed","#f3e8ff"),
            kpiPaie("🎫  Réservations payées",  String.valueOf(compterPayees()),      "#ea580c","#ffedd5")
        );
        for (javafx.scene.Node n : kpiRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Table paiements
        TableView<ObservableList<String>> table = new TableView<>();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String[] cols = {"Référence","N° Ticket","Client","Montant","Mode","Statut","Date"};
        int[]    widths = {140, 130, 160, 130, 120, 100, 150};

        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
            if (i == 3) { // Montant
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null) { setText(null); return; }
                        setText(s); setStyle("-fx-font-weight:bold;-fx-text-fill:#16a34a;");
                    }
                });
            }
            if (i == 5) { // Statut
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null) { setGraphic(null); return; }
                        String[] bc = switch (s) {
                            case "PAYE"     -> new String[]{"✅ Payé",      "#dcfce7","#15803d"};
                            case "REMBOURSE"-> new String[]{"↩️ Remboursé", "#ffedd5","#c2410c"};
                            default         -> new String[]{"⏳ " + s,      "#f1f5f9","#475569"};
                        };
                        setGraphic(UIHelper.badge(bc[0], bc[1], bc[2]));
                    }
                });
            }
            table.getColumns().add(col);
        }

        try {
            String sql = "SELECT p.reference, r.numero_ticket, CONCAT(u.prenom,' ',u.nom), " +
                "p.montant, p.mode_paiement, p.statut, DATE_FORMAT(p.date_paiement,'%d/%m/%Y %H:%i') " +
                "FROM paiements p " +
                "JOIN reservations r ON p.reservation_id = r.id " +
                "JOIN utilisateurs u ON r.client_id = u.id " +
                "ORDER BY p.date_paiement DESC";
            Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString(1));
                row.add(rs.getString(2));
                row.add(rs.getString(3));
                row.add(String.format("%,.0f FCFA", rs.getDouble(4)));
                row.add(rs.getString(5));
                row.add(rs.getString(6));
                row.add(rs.getString(7) != null ? rs.getString(7) : "");
                data.add(row);
            }
        } catch (SQLException e) { System.err.println("[PaiementController] " + e.getMessage()); }

        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox tableCard = UIHelper.card();
        tableCard.setPadding(new Insets(16));
        Label lTitre = new Label("📋  Historique des paiements");
        lTitre.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        tableCard.getChildren().addAll(lTitre, UIHelper.sep(), table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(0, entete, kpiRow, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        root.setCenter(content);
    }

    private HBox kpiPaie(String titre, String valeur, String fg, String bg) {
        HBox c = new HBox(12);
        c.setPadding(new Insets(16));
        c.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,2);");
        c.setAlignment(Pos.CENTER_LEFT);
        Region bord = new Region();
        bord.setPrefWidth(4); bord.setPrefHeight(48);
        bord.setStyle("-fx-background-color:" + fg + ";-fx-background-radius:2;");
        VBox txt = new VBox(4);
        Label lt = new Label(titre);
        lt.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;-fx-font-weight:bold;");
        Label lv = new Label(valeur);
        lv.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + fg + ";");
        txt.getChildren().addAll(lt, lv);
        c.getChildren().addAll(bord, txt);
        return c;
    }

    private double totalRevenus() {
        try {
            Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(montant),0) FROM paiements WHERE statut='PAYE'");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[PaiementController] totalRevenus : " + e.getMessage()); }
        return 0;
    }

    private int compterPayees() {
        try {
            Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM paiements WHERE statut='PAYE'");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[PaiementController] compterPayees : " + e.getMessage()); }
        return 0;
    }

    public BorderPane getView() { return root; }
}
