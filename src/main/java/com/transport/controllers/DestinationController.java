package com.transport.controllers;

import com.transport.dao.DestinationDAO;
import com.transport.models.Destination;
import com.transport.utils.UIHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class DestinationController {

    private final DestinationDAO dao = new DestinationDAO();
    private BorderPane root;
    private TableView<Destination> table;
    private ObservableList<Destination> donnees;

    public DestinationController() { construireVue(); chargerDonnees(); }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f5f9;");
        root.setPadding(new Insets(24));

        Button btnAdd = UIHelper.btnPrimary("➕  Nouvelle destination");
        btnAdd.setOnAction(e -> ouvrirFormulaire());
        Button btnRef = UIHelper.btnSecondary("🔄  Actualiser");
        btnRef.setOnAction(e -> chargerDonnees());

        HBox entete = UIHelper.enteteModule("🗺️  Gestion des Destinations", btnAdd, btnRef);

        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);

        TableColumn<Destination, String> cDep  = col("Ville Départ",  "villeDepart",   160);
        TableColumn<Destination, String> cArr  = col("Ville Arrivée", "villeArrivee",  160);
        TableColumn<Destination, Double> cDist = colDbl("Distance (km)", "distanceKm", 120);
        TableColumn<Destination, Integer> cDur = colInt("Durée (min)",   "dureeEstimeeMin", 110);

        TableColumn<Destination, Double> cTarif = new TableColumn<>("Tarif de base");
        cTarif.setPrefWidth(150);
        cTarif.setCellValueFactory(new PropertyValueFactory<>("tarifBase"));
        cTarif.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(String.format("%,.0f FCFA", v));
                setStyle("-fx-font-weight:bold;-fx-text-fill:#16a34a;");
            }
        });

        TableColumn<Destination, Boolean> cActif = new TableColumn<>("Statut");
        cActif.setPrefWidth(100);
        cActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        cActif.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(UIHelper.badge(
                    Boolean.TRUE.equals(v) ? "✅ Actif" : "❌ Inactif",
                    Boolean.TRUE.equals(v) ? "#dcfce7" : "#fee2e2",
                    Boolean.TRUE.equals(v) ? "#15803d" : "#dc2626"));
            }
        });

        table.getColumns().addAll(cDep, cArr, cDist, cDur, cTarif, cActif);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox tableCard = UIHelper.card();
        tableCard.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(0, entete, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        root.setCenter(content);
    }

    private void chargerDonnees() { donnees.setAll(dao.listerToutes()); }

    private void ouvrirFormulaire() {
        Dialog<Destination> dlg = new Dialog<>();
        dlg.setTitle("Nouvelle destination");
        dlg.getDialogPane().setStyle("-fx-background-color:white;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(12); form.setPadding(new Insets(20));

        TextField tfDep   = UIHelper.champTexte("Ex: Yaoundé");
        TextField tfArr   = UIHelper.champTexte("Ex: Douala");
        TextField tfDist  = UIHelper.champTexte("En km");
        TextField tfDuree = UIHelper.champTexte("En minutes");
        TextField tfTarif = UIHelper.champTexte("En FCFA");

        int r = 0;
        form.add(UIHelper.champLabel("Ville départ *"),  0,r); form.add(tfDep,   1,r++);
        form.add(UIHelper.champLabel("Ville arrivée *"), 0,r); form.add(tfArr,   1,r++);
        form.add(UIHelper.champLabel("Distance (km)"),   0,r); form.add(tfDist,  1,r++);
        form.add(UIHelper.champLabel("Durée (min)"),     0,r); form.add(tfDuree, 1,r++);
        form.add(UIHelper.champLabel("Tarif de base *"), 0,r); form.add(tfTarif, 1,r++);

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().setPrefWidth(440);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                Destination d = new Destination();
                d.setVilleDepart(tfDep.getText().trim());
                d.setVilleArrivee(tfArr.getText().trim());
                d.setDistanceKm(tfDist.getText().isBlank() ? 0 : Double.parseDouble(tfDist.getText()));
                d.setDureeEstimeeMin(tfDuree.getText().isBlank() ? 0 : Integer.parseInt(tfDuree.getText()));
                d.setTarifBase(Double.parseDouble(tfTarif.getText().trim()));
                d.setActif(true);
                return d;
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Vérifiez les champs numériques.").showAndWait(); return null;
            }
        });

        dlg.showAndWait().ifPresent(d -> {
            if (dao.ajouter(d)) { chargerDonnees(); new Alert(Alert.AlertType.INFORMATION, "✅  Destination ajoutée !").showAndWait(); }
            else new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout.").showAndWait();
        });
    }

    private <T> TableColumn<Destination, T> col(String t, String p, int w) {
        TableColumn<Destination, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private <T> TableColumn<Destination, T> colDbl(String t, String p, int w) { return col(t, p, w); }
    private <T> TableColumn<Destination, T> colInt(String t, String p, int w) { return col(t, p, w); }

    public BorderPane getView() { return root; }
}
