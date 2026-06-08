package com.transport.controllers;

import com.transport.dao.VehiculeDAO;
import com.transport.models.Vehicule;
import com.transport.utils.UIHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class VehiculeController {

    private final VehiculeDAO dao = new VehiculeDAO();
    private BorderPane root;
    private TableView<Vehicule> table;
    private ObservableList<Vehicule> donnees;
    private TextField tfRecherche;

    public VehiculeController() { construireVue(); chargerDonnees(); }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f5f9;");
        root.setPadding(new Insets(24));

        // ── En-tête ──────────────────────────────────────────────────
        tfRecherche = UIHelper.champTexte("🔍  Rechercher...");
        tfRecherche.setPrefWidth(220);
        tfRecherche.textProperty().addListener((obs, o, n) -> filtrer(n));

        ComboBox<String> cbType = new ComboBox<>(
            FXCollections.observableArrayList("TOUS", "VIP", "CLASSIQUE"));
        cbType.setValue("TOUS");
        cbType.setStyle("-fx-background-radius:8;-fx-border-color:#cbd5e1;-fx-background-color:white;");
        cbType.setOnAction(e -> {
            if ("TOUS".equals(cbType.getValue())) chargerDonnees();
            else donnees.setAll(dao.listerTous().stream()
                .filter(v -> cbType.getValue().equals(v.getTypeBus())).toList());
        });

        Button btnAdd = UIHelper.btnPrimary("➕  Ajouter véhicule");
        btnAdd.setOnAction(e -> ouvrirFormulaire(null));
        Button btnRef = UIHelper.btnSecondary("🔄  Actualiser");
        btnRef.setOnAction(e -> chargerDonnees());

        HBox entete = UIHelper.enteteModule("🚌  Parc Automobile",
            tfRecherche, new Label("Type :"), cbType, btnAdd, btnRef);

        // ── Statistiques rapides ──────────────────────────────────────
        HBox statsBar = new HBox(12);
        statsBar.getChildren().addAll(
            miniStat("✅ Disponibles",    String.valueOf(dao.compterParEtat("DISPONIBLE")),    "#16a34a","#dcfce7"),
            miniStat("🚀 En service",     String.valueOf(dao.compterParEtat("EN_SERVICE")),    "#2563eb","#dbeafe"),
            miniStat("🔧 Maintenance",    String.valueOf(dao.compterParEtat("EN_MAINTENANCE")),"#ea580c","#ffedd5"),
            miniStat("🚫 Hors service",   String.valueOf(dao.compterParEtat("HORS_SERVICE")), "#dc2626","#fee2e2")
        );
        statsBar.setPadding(new Insets(0, 0, 16, 0));

        // ── Table ─────────────────────────────────────────────────────
        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);
        table.setStyle("-fx-background-color:white;-fx-background-radius:12;");

        table.getColumns().addAll(
            colStr("Immatriculation", "immatriculation", 140),
            colStr("Marque",          "marque",          110),
            colStr("Modèle",          "modele",          110),
            colInt("Capacité",        "capacite",         75),
            colInt("Année",           "anneeFabrication", 70),
            colInt("Km",              "kilometrage",      90),
            colType(),
            colPrix("Prix Classique", "prixClassique", 125),
            colPrix("Prix VIP",       "prixVip",       110),
            colEtat(),
            colActions()
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox tableCard = UIHelper.card();
        tableCard.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(0, entete, statsBar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        root.setCenter(content);
    }

    // ── Colonnes ─────────────────────────────────────────────────────
    private <T> TableColumn<Vehicule, T> colStr(String t, String p, int w) {
        TableColumn<Vehicule, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private <T> TableColumn<Vehicule, T> colInt(String t, String p, int w) { return colStr(t, p, w); }

    private TableColumn<Vehicule, String> colType() {
        TableColumn<Vehicule, String> c = new TableColumn<>("Type");
        c.setPrefWidth(105);
        c.setCellValueFactory(new PropertyValueFactory<>("typeBus"));
        c.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); return; }
                setGraphic(UIHelper.badge(
                    "VIP".equals(t) ? "⭐ VIP" : "🚌 Classique",
                    "VIP".equals(t) ? "#f3e8ff" : "#dbeafe",
                    "VIP".equals(t) ? "#7c3aed" : "#1d4ed8"));
            }
        });
        return c;
    }

    private TableColumn<Vehicule, Double> colPrix(String titre, String prop, int w) {
        TableColumn<Vehicule, Double> c = new TableColumn<>(titre);
        c.setPrefWidth(w);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null || p == 0) { setText("—"); setStyle("-fx-text-fill:#94a3b8;"); return; }
                setText(String.format("%,.0f FCFA", p));
                setStyle("-fx-font-weight:bold;-fx-text-fill:#1e293b;");
            }
        });
        return c;
    }

    private TableColumn<Vehicule, String> colEtat() {
        TableColumn<Vehicule, String> c = new TableColumn<>("État");
        c.setPrefWidth(125);
        c.setCellValueFactory(new PropertyValueFactory<>("etat"));
        c.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) { setGraphic(null); return; }
                String[] bc = switch (e) {
                    case "DISPONIBLE"     -> new String[]{"✅ Disponible",    "#dcfce7","#15803d"};
                    case "EN_SERVICE"     -> new String[]{"🚀 En service",    "#dbeafe","#1d4ed8"};
                    case "EN_MAINTENANCE" -> new String[]{"🔧 Maintenance",   "#ffedd5","#c2410c"};
                    default               -> new String[]{"🚫 Hors service",  "#fee2e2","#dc2626"};
                };
                setGraphic(UIHelper.badge(bc[0], bc[1], bc[2]));
            }
        });
        return c;
    }

    private TableColumn<Vehicule, Void> colActions() {
        TableColumn<Vehicule, Void> c = new TableColumn<>("Actions");
        c.setPrefWidth(220);
        c.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit  = UIHelper.btnTable("✏️ Modifier",    "#2563eb");
            final Button bDispo = UIHelper.btnTable("✅",             "#16a34a");
            final Button bMaint = UIHelper.btnTable("🔧",             "#ea580c");
            final Button bHS    = UIHelper.btnTable("🚫",             "#dc2626");
            final HBox box = new HBox(4, bEdit, bDispo, bMaint, bHS);
            { box.setAlignment(Pos.CENTER);
              bEdit.setOnAction(e  -> ouvrirFormulaire(donnees.get(getIndex())));
              bDispo.setOnAction(e -> { dao.changerEtat(donnees.get(getIndex()).getId(),"DISPONIBLE");     chargerDonnees(); });
              bMaint.setOnAction(e -> { dao.changerEtat(donnees.get(getIndex()).getId(),"EN_MAINTENANCE"); chargerDonnees(); });
              bHS.setOnAction(e    -> { dao.changerEtat(donnees.get(getIndex()).getId(),"HORS_SERVICE");   chargerDonnees(); }); }
            @Override protected void updateItem(Void x, boolean empty) {
                super.updateItem(x, empty); setGraphic(empty ? null : box); }
        });
        return c;
    }

    private void chargerDonnees() { donnees.setAll(dao.listerTous()); }

    private void filtrer(String t) {
        if (t == null || t.isBlank()) { chargerDonnees(); return; }
        String q = t.toLowerCase();
        donnees.setAll(dao.listerTous().stream()
            .filter(v -> v.getImmatriculation().toLowerCase().contains(q)
                      || v.getMarque().toLowerCase().contains(q)
                      || v.getModele().toLowerCase().contains(q)).toList());
    }

    private void ouvrirFormulaire(Vehicule vehicule) {
        Dialog<Vehicule> dlg = new Dialog<>();
        dlg.setTitle(vehicule == null ? "Ajouter un véhicule" : "Modifier le véhicule");
        dlg.getDialogPane().setStyle("-fx-background-color:white;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(12); form.setPadding(new Insets(20));

        TextField tfImmat  = UIHelper.champTexte("LT-XXXX-CM");
        TextField tfMarque = UIHelper.champTexte("Mercedes, Toyota...");
        TextField tfModele = UIHelper.champTexte("Sprinter, Coaster...");
        TextField tfCap    = UIHelper.champTexte("Nombre de places");
        TextField tfAnnee  = UIHelper.champTexte("2020");
        TextField tfKm     = UIHelper.champTexte("0");

        ComboBox<String> cbType = new ComboBox<>(
            FXCollections.observableArrayList("CLASSIQUE","VIP"));
        cbType.setValue("CLASSIQUE");
        cbType.setPrefWidth(260);

        TextField tfPrixC = UIHelper.champTexte("Prix classique FCFA");
        TextField tfPrixV = UIHelper.champTexte("Prix VIP FCFA (0 si classique)");

        ComboBox<String> cbEtat = new ComboBox<>(FXCollections.observableArrayList(
            "DISPONIBLE","EN_SERVICE","EN_MAINTENANCE","HORS_SERVICE"));
        cbEtat.setValue("DISPONIBLE"); cbEtat.setPrefWidth(260);

        Label lblVipInfo = new Label("ℹ️  Laissez 0 si bus purement classique");
        lblVipInfo.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;");

        cbType.setOnAction(e -> {
            boolean vip = "VIP".equals(cbType.getValue());
            tfPrixV.setDisable(!vip);
            lblVipInfo.setText(vip ? "✅  Bus VIP — renseignez les deux prix" : "ℹ️  Laissez 0 si bus purement classique");
        });

        if (vehicule != null) {
            tfImmat.setText(vehicule.getImmatriculation());
            tfMarque.setText(vehicule.getMarque());
            tfModele.setText(vehicule.getModele());
            tfCap.setText(String.valueOf(vehicule.getCapacite()));
            tfAnnee.setText(String.valueOf(vehicule.getAnneeFabrication()));
            tfKm.setText(String.valueOf(vehicule.getKilometrage()));
            cbType.setValue(vehicule.getTypeBus() != null ? vehicule.getTypeBus() : "CLASSIQUE");
            tfPrixC.setText(String.valueOf((int)vehicule.getPrixClassique()));
            tfPrixV.setText(String.valueOf((int)vehicule.getPrixVip()));
            cbEtat.setValue(vehicule.getEtat());
        }
        tfPrixV.setDisable(!"VIP".equals(cbType.getValue()));

        int r = 0;
        form.add(UIHelper.champLabel("Immatriculation *"), 0,r); form.add(tfImmat,  1,r++);
        form.add(UIHelper.champLabel("Marque *"),          0,r); form.add(tfMarque, 1,r++);
        form.add(UIHelper.champLabel("Modèle *"),          0,r); form.add(tfModele, 1,r++);
        form.add(UIHelper.champLabel("Capacité *"),        0,r); form.add(tfCap,    1,r++);
        form.add(UIHelper.champLabel("Année fabrication"), 0,r); form.add(tfAnnee,  1,r++);
        form.add(UIHelper.champLabel("Kilométrage"),       0,r); form.add(tfKm,     1,r++);
        form.add(UIHelper.champLabel("Type de bus *"),     0,r); form.add(cbType,   1,r++);
        form.add(UIHelper.champLabel("Prix Classique *"),  0,r); form.add(tfPrixC,  1,r++);
        form.add(UIHelper.champLabel("Prix VIP"),          0,r); form.add(tfPrixV,  1,r++);
        form.add(new Label(""),                            0,r); form.add(lblVipInfo,1,r++);
        form.add(UIHelper.champLabel("État"),              0,r); form.add(cbEtat,   1,r++);

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().setPrefWidth(480);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                Vehicule v = vehicule != null ? vehicule : new Vehicule();
                v.setImmatriculation(tfImmat.getText().trim().toUpperCase());
                v.setMarque(tfMarque.getText().trim());
                v.setModele(tfModele.getText().trim());
                v.setCapacite(Integer.parseInt(tfCap.getText().trim()));
                v.setAnneeFabrication(tfAnnee.getText().isBlank() ? 0 : Integer.parseInt(tfAnnee.getText().trim()));
                v.setKilometrage(tfKm.getText().isBlank() ? 0 : Integer.parseInt(tfKm.getText().trim()));
                v.setTypeBus(cbType.getValue());
                v.setPrixClassique(tfPrixC.getText().isBlank() ? 0 : Double.parseDouble(tfPrixC.getText().trim()));
                v.setPrixVip(tfPrixV.getText().isBlank() ? 0 : Double.parseDouble(tfPrixV.getText().trim()));
                v.setEtat(cbEtat.getValue());
                return v;
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Vérifiez les champs numériques.", ButtonType.OK).showAndWait();
                return null;
            }
        });

        dlg.showAndWait().ifPresent(v -> {
            boolean ok = vehicule == null ? dao.ajouter(v) : dao.modifier(v);
            if (ok) { chargerDonnees(); info(vehicule == null ? "✅  Véhicule ajouté !" : "✅  Modifié !"); }
            else new Alert(Alert.AlertType.ERROR, "Opération échouée.", ButtonType.OK).showAndWait();
        });
    }

    private HBox miniStat(String label, String val, String fg, String bg) {
        HBox h = new HBox(10);
        h.setPadding(new Insets(10, 16, 10, 16));
        h.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:8;");
        h.setAlignment(Pos.CENTER_LEFT);
        Label lv = new Label(val);
        lv.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + fg + ";");
        Label ll = new Label(label);
        ll.setStyle("-fx-font-size:11px;-fx-text-fill:" + fg + ";");
        h.getChildren().addAll(lv, ll);
        return h;
    }

    private void info(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    public BorderPane getView() { return root; }
}
