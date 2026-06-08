package com.transport.controllers;

import com.transport.dao.*;
import com.transport.models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoyageController {

    private final VoyageDAO voyageDAO    = new VoyageDAO();
    private final DestinationDAO destDAO = new DestinationDAO();
    private final VehiculeDAO vehDAO     = new VehiculeDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();

    private BorderPane root;
    private TableView<Voyage> table;
    private ObservableList<Voyage> donnees;
    private static final DateTimeFormatter FMT    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_IN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VoyageController() {
        construireVue();
        chargerDonnees();
    }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");
        root.setPadding(new Insets(25));

        HBox entete = new HBox(15);
        entete.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("✈️ Gestion des Voyages");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnAdd = btn("➕ Planifier un voyage", "#1a237e");
        btnAdd.setOnAction(e -> ouvrirFormulaire());
        Button btnRef = btn("🔄 Actualiser", "#37474f");
        btnRef.setOnAction(e -> chargerDonnees());

        entete.getChildren().addAll(titre, sp, btnAdd, btnRef);
        root.setTop(entete);
        BorderPane.setMargin(entete, new Insets(0, 0, 15, 0));

        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);

        TableColumn<Voyage, String> cTrajet = new TableColumn<>("Trajet");
        cTrajet.setPrefWidth(180);
        cTrajet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrajet()));

        TableColumn<Voyage, String> cDate = new TableColumn<>("Départ");
        cDate.setPrefWidth(135);
        cDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getDateDepart() != null ? d.getValue().getDateDepart().format(FMT) : ""));

        TableColumn<Voyage, String> cVeh = new TableColumn<>("Véhicule");
        cVeh.setPrefWidth(200);
        cVeh.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVehiculeLabel()));

        TableColumn<Voyage, String> cChauf = new TableColumn<>("Chauffeur");
        cChauf.setPrefWidth(140);
        cChauf.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChauffeurNom()));

        TableColumn<Voyage, Integer> cPlaces = col("Places", "placesDisponibles", 70);

        // ── UNE SEULE colonne Prix ────────────────────────────────────
        TableColumn<Voyage, String> cPrix = new TableColumn<>("Prix / place");
        cPrix.setPrefWidth(130);
        cPrix.setCellValueFactory(d -> {
            Voyage v = d.getValue();
            String typeBus = v.getTypeBus();
            double prix = v.getPrixParPlace();
            String label = "VIP".equals(typeBus)
                ? "⭐ " + String.format("%,.0f FCFA", prix)
                : "🚌 " + String.format("%,.0f FCFA", prix);
            return new SimpleStringProperty(label);
        });
        cPrix.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(s.startsWith("⭐")
                    ? "-fx-text-fill:#880e4f;-fx-font-weight:bold;"
                    : "-fx-text-fill:#1565c0;-fx-font-weight:bold;");
            }
        });

        TableColumn<Voyage, String> cStatut = new TableColumn<>("Statut");
        cStatut.setPrefWidth(110);
        cStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        cStatut.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s.replace("_", " "));
                String c = switch (s) {
                    case "PLANIFIE" -> "#1565c0";
                    case "EN_COURS" -> "#2e7d32";
                    case "TERMINE"  -> "#37474f";
                    default         -> "#c62828";
                };
                setStyle("-fx-text-fill:" + c + ";-fx-font-weight:bold;");
            }
        });

        TableColumn<Voyage, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(190);
        cAct.setCellFactory(tc -> new TableCell<>() {
            final Button bStart  = btn("▶ Démarrer", "#2e7d32");
            final Button bEnd    = btn("⏹ Terminer", "#37474f");
            final Button bCancel = btn("✖ Annuler",  "#c62828");
            final HBox box = new HBox(4, bStart, bEnd, bCancel);
            { box.setAlignment(Pos.CENTER);
              bStart.setOnAction(e  -> changerStatut(getIndex(), "EN_COURS"));
              bEnd.setOnAction(e    -> changerStatut(getIndex(), "TERMINE"));
              bCancel.setOnAction(e -> changerStatut(getIndex(), "ANNULE")); }
            @Override protected void updateItem(Void x, boolean empty) {
                super.updateItem(x, empty); setGraphic(empty ? null : box); }
        });

        table.getColumns().addAll(cTrajet, cDate, cVeh, cChauf, cPlaces, cPrix, cStatut, cAct);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox wrap = new VBox(table);
        wrap.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.08),8,0,0,2);");
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(wrap);
    }

    private void chargerDonnees() { donnees.setAll(voyageDAO.listerTous()); }

    private void changerStatut(int idx, String statut) {
        if (idx < 0 || idx >= donnees.size()) return;
        voyageDAO.changerStatut(donnees.get(idx).getId(), statut);
        chargerDonnees();
    }

    private void ouvrirFormulaire() {
        List<Destination> dests = destDAO.listerToutes();
        List<Vehicule>    vehs  = vehDAO.listerDisponibles();
        List<Utilisateur> chaufs= userDAO.listerParRole("CHAUFFEUR");

        if (dests.isEmpty())  { alerte("Aucune destination enregistrée."); return; }
        if (vehs.isEmpty())   { alerte("Aucun véhicule disponible."); return; }
        if (chaufs.isEmpty()) { alerte("Aucun chauffeur enregistré."); return; }

        Dialog<Voyage> dialog = new Dialog<>();
        dialog.setTitle("Planifier un nouveau voyage");
        dialog.setHeaderText("Remplissez les informations du voyage");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12); form.setPadding(new Insets(20));

        ComboBox<Destination> cbDest = new ComboBox<>(FXCollections.observableArrayList(dests));
        cbDest.setPrefWidth(300); cbDest.setPromptText("Choisir une destination...");

        ComboBox<Vehicule> cbVeh = new ComboBox<>(FXCollections.observableArrayList(vehs));
        cbVeh.setPrefWidth(300); cbVeh.setPromptText("Choisir un véhicule...");
        cbVeh.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Vehicule v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null :
                    v.getLabel() + " | " + v.getCapacite() + " places");
            }
        });
        cbVeh.setButtonCell(cbVeh.getCellFactory().call(null));

        ComboBox<Utilisateur> cbChauf = new ComboBox<>(FXCollections.observableArrayList(chaufs));
        cbChauf.setPrefWidth(300); cbChauf.setPromptText("Choisir un chauffeur...");

        TextField tfDate = new TextField(
            LocalDateTime.now().plusDays(1).withHour(6).withMinute(0).format(FMT_IN));
        tfDate.setPrefWidth(300);

        // ── Infos véhicule (affichées dynamiquement) ─────────────────
        Label lblTypeBus = new Label("");
        lblTypeBus.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");

        TextField tfPlaces = new TextField();
        tfPlaces.setPromptText("Auto-rempli selon le véhicule");
        tfPlaces.setPrefWidth(300);

        // ── UN SEUL champ Prix ────────────────────────────────────────
        Label lblPrixLabel = new Label("Prix / place (FCFA) *");
        lblPrixLabel.setStyle("-fx-font-weight:bold;-fx-text-fill:#37474f;");
        TextField tfPrix = new TextField();
        tfPrix.setPromptText("Auto-rempli selon véhicule et destination");
        tfPrix.setPrefWidth(300);

        Label lblPrixInfo = new Label("");
        lblPrixInfo.setStyle("-fx-font-size:11px;-fx-text-fill:#546e7a;");

        // ── Auto-remplissage quand on choisit véhicule ou destination ─
        Runnable majPrix = () -> {
            Vehicule v = cbVeh.getValue();
            Destination d = cbDest.getValue();
            if (v == null) return;

            tfPlaces.setText(String.valueOf(v.getCapacite()));

            if ("VIP".equals(v.getTypeBus())) {
                lblTypeBus.setText("⭐ Bus VIP — " + v.getCapacite() + " places");
                lblTypeBus.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#880e4f;" +
                    "-fx-background-color:#fce4ec;-fx-padding:4 10;-fx-background-radius:6;");
                double prix = v.getPrixVip() > 0 ? v.getPrixVip()
                            : (d != null ? d.getTarifBase() * 1.5 : 0);
                tfPrix.setText(String.valueOf((int)prix));
                lblPrixInfo.setText("Prix VIP du véhicule" + (d != null ? " sur " + d.getTrajet() : ""));
                lblPrixLabel.setText("Prix VIP / place (FCFA) *");
                lblPrixLabel.setStyle("-fx-font-weight:bold;-fx-text-fill:#880e4f;");
            } else {
                lblTypeBus.setText("🚌 Bus Classique — " + v.getCapacite() + " places");
                lblTypeBus.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1565c0;" +
                    "-fx-background-color:#e3f2fd;-fx-padding:4 10;-fx-background-radius:6;");
                double prix = v.getPrixClassique() > 0 ? v.getPrixClassique()
                            : (d != null ? d.getTarifBase() : 0);
                tfPrix.setText(String.valueOf((int)prix));
                lblPrixInfo.setText("Prix classique du véhicule" + (d != null ? " sur " + d.getTrajet() : ""));
                lblPrixLabel.setText("Prix Classique / place (FCFA) *");
                lblPrixLabel.setStyle("-fx-font-weight:bold;-fx-text-fill:#1565c0;");
            }
        };

        cbVeh.setOnAction(e -> majPrix.run());
        cbDest.setOnAction(e -> majPrix.run());

        int r = 0;
        form.add(lbl("Destination *"),    0, r); form.add(cbDest, 1, r++);
        form.add(lbl("Véhicule *"),       0, r); form.add(cbVeh, 1, r++);
        form.add(lbl("Type de bus"),      0, r); form.add(lblTypeBus, 1, r++);
        form.add(lbl("Chauffeur *"),      0, r); form.add(cbChauf, 1, r++);
        form.add(lbl("Date départ *"),    0, r); form.add(tfDate, 1, r++);
        form.add(lbl("Nb places *"),      0, r); form.add(tfPlaces, 1, r++);
        form.add(lblPrixLabel,            0, r); form.add(tfPrix, 1, r++);
        form.add(new Label(""),           0, r); form.add(lblPrixInfo, 1, r++);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                if (cbDest.getValue()  == null) throw new Exception("Sélectionnez une destination.");
                if (cbVeh.getValue()   == null) throw new Exception("Sélectionnez un véhicule.");
                if (cbChauf.getValue() == null) throw new Exception("Sélectionnez un chauffeur.");
                if (tfDate.getText().isBlank()) throw new Exception("Entrez la date de départ.");

                int chauffeurId = voyageDAO.getChauffeurIdParUtilisateur(cbChauf.getValue().getId());
                if (chauffeurId == -1) throw new Exception(
                    "Chauffeur non trouvé dans la table 'chauffeurs'.\n" +
                    "Vérifiez que cet utilisateur est bien enregistré comme chauffeur en base.");

                Vehicule veh = cbVeh.getValue();
                double prix  = tfPrix.getText().isBlank() ? 0 : Double.parseDouble(tfPrix.getText().trim());
                int places   = tfPlaces.getText().isBlank() ? veh.getCapacite() : Integer.parseInt(tfPlaces.getText().trim());

                Voyage v = new Voyage();
                v.setDestinationId(cbDest.getValue().getId());
                v.setVehiculeId(veh.getId());
                v.setChauffeurId(chauffeurId);
                v.setDateDepart(LocalDateTime.parse(tfDate.getText().trim(), FMT_IN));
                v.setPlacesDisponibles(places);
                v.setPlacesClassique("VIP".equals(veh.getTypeBus()) ? 0 : places);
                v.setPlacesVip("VIP".equals(veh.getTypeBus()) ? places : 0);
                v.setPrixParPlace(prix);
                v.setPrixClassique("VIP".equals(veh.getTypeBus()) ? 0 : prix);
                v.setPrixVip("VIP".equals(veh.getTypeBus()) ? prix : 0);
                v.setTypeBus(veh.getTypeBus());
                return v;
            } catch (Exception ex) {
                alerte(ex.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(v -> {
            if (v == null) return;
            if (voyageDAO.ajouter(v)) {
                chargerDonnees();
                new Alert(Alert.AlertType.INFORMATION,
                    "✅ Voyage planifié !\n" +
                    "Type     : " + ("VIP".equals(v.getTypeBus()) ? "⭐ VIP" : "🚌 Classique") + "\n" +
                    "Places   : " + v.getPlacesDisponibles() + "\n" +
                    "Prix/pl. : " + String.format("%,.0f FCFA", v.getPrixParPlace()),
                    ButtonType.OK).showAndWait();
            } else {
                alerte("Erreur lors de la création du voyage. Vérifiez la console.");
            }
        });
    }

    private <T> TableColumn<Voyage, T> col(String t, String p, int w) {
        TableColumn<Voyage, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private Button btn(String t, String bg) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:6;" +
            "-fx-cursor:hand;-fx-font-size:11px;-fx-padding:5 8;");
        return b;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight:bold;-fx-text-fill:#37474f;"); return l;
    }
    private void alerte(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }

    public BorderPane getView() { return root; }
}
