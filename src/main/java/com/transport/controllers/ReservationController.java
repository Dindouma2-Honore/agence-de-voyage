package com.transport.controllers;

import com.transport.dao.*;
import com.transport.models.*;
import com.transport.utils.RecuImprimeur;
import com.transport.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationController {

    private final ReservationDAO resDAO  = new ReservationDAO();
    private final VoyageDAO voyageDAO    = new VoyageDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();

    private BorderPane root;
    private TableView<Reservation> table;
    private ObservableList<Reservation> donnees;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReservationController() {
        construireVue();
        chargerDonnees();
    }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");
        root.setPadding(new Insets(25));

        HBox entete = new HBox(15);
        entete.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("🎫 Gestion des Réservations");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClass = btnType("🚌 Réserver Classique", "#1565c0");
        Button btnVip   = btnType("⭐ Réserver VIP",       "#880e4f");
        btnClass.setOnAction(e -> ouvrirFormulaire("CLASSIQUE"));
        btnVip.setOnAction(e   -> ouvrirFormulaire("VIP"));

        Button btnRef = btn("🔄 Actualiser", "#37474f");
        btnRef.setOnAction(e -> chargerDonnees());

        entete.getChildren().addAll(titre, sp, btnClass, btnVip, btnRef);
        root.setTop(entete);
        BorderPane.setMargin(entete, new Insets(0, 0, 15, 0));

        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);

        TableColumn<Reservation, String> cTicket = col("N° Ticket",  "numeroTicket", 130);
        TableColumn<Reservation, String> cClient = col("Client",     "clientNom",    150);
        TableColumn<Reservation, String> cTrajet = col("Trajet",     "trajet",       170);

        TableColumn<Reservation, String> cDepart = new TableColumn<>("Départ");
        cDepart.setPrefWidth(130);
        cDepart.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getDateDepart() != null ? d.getValue().getDateDepart().format(FMT) : ""));

        TableColumn<Reservation, String> cType = new TableColumn<>("Type");
        cType.setPrefWidth(105);
        cType.setCellValueFactory(new PropertyValueFactory<>("typPlace"));
        cType.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setText(null); setStyle(""); return; }
                setText("VIP".equals(t) ? "⭐ VIP" : "🚌 Classique");
                setStyle("VIP".equals(t)
                    ? "-fx-text-fill:#880e4f;-fx-font-weight:bold;"
                    : "-fx-text-fill:#1565c0;-fx-font-weight:bold;");
            }
        });

        TableColumn<Reservation, Integer> cPlaces = col("Places", "nombrePlaces", 65);

        TableColumn<Reservation, Double> cMontant = new TableColumn<>("Montant");
        cMontant.setPrefWidth(125);
        cMontant.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        cMontant.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : String.format("%,.0f FCFA", m));
            }
        });

        TableColumn<Reservation, String> cStatut = new TableColumn<>("Statut");
        cStatut.setPrefWidth(105);
        cStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        cStatut.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s.replace("_", " "));
                setStyle("-fx-text-fill:" + switch (s) {
                    case "CONFIRMEE"  -> "#2e7d32";
                    case "EN_ATTENTE" -> "#e65100";
                    case "ANNULEE"    -> "#c62828";
                    default           -> "#37474f";
                } + ";-fx-font-weight:bold;");
            }
        });

        TableColumn<Reservation, String> cPaie = col("Paiement", "statutPaiement", 95);

        TableColumn<Reservation, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(245);
        cAct.setCellFactory(tc -> new TableCell<>() {
            final Button bPaye   = btn("💰 Payer",   "#2e7d32");
            final Button bAnn    = btn("✖ Annuler",  "#c62828");
            final Button bTicket = btn("🖨️ Reçu",   "#1565c0");
            final HBox box = new HBox(5, bPaye, bAnn, bTicket);
            {
                box.setAlignment(Pos.CENTER);
                bPaye.setOnAction(e   -> payer(getIndex()));
                bAnn.setOnAction(e    -> annuler(getIndex()));
                bTicket.setOnAction(e -> imprimer(getIndex()));
            }
            @Override protected void updateItem(Void x, boolean empty) {
                super.updateItem(x, empty); setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(cTicket, cClient, cTrajet, cDepart, cType, cPlaces, cMontant, cStatut, cPaie, cAct);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox wrap = new VBox(table);
        wrap.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.08),8,0,0,2);");
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(wrap);
    }

    private void chargerDonnees() {
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        if ("CLIENT".equals(role))
            donnees.setAll(resDAO.listerParClient(
                SessionManager.getInstance().getUtilisateurConnecte().getId()));
        else
            donnees.setAll(resDAO.listerToutes());
    }

    private void ouvrirFormulaire(String typePlaceChoisi) {
        List<Voyage> tous = voyageDAO.listerDisponibles();
        List<Voyage> voyages = tous.stream()
            .filter(v -> typePlaceChoisi.equals(v.getTypeBus()))
            .toList();
        List<Utilisateur> clients = userDAO.listerParRole("CLIENT");

        if (voyages.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                "Aucun voyage " + ("VIP".equals(typePlaceChoisi) ? "VIP" : "Classique") +
                " disponible actuellement.", ButtonType.OK).showAndWait();
            return;
        }

        Dialog<Reservation> dialog = new Dialog<>();
        String typeLabel = "VIP".equals(typePlaceChoisi) ? "⭐ VIP" : "🚌 Classique";
        dialog.setTitle("Réservation " + typeLabel);
        dialog.setHeaderText("Nouvelle réservation — Place " + typeLabel);

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12); form.setPadding(new Insets(20));

        Label lblBadge = new Label("  Place " + typeLabel + "  ");
        lblBadge.setStyle("-fx-background-color:" +
            ("VIP".equals(typePlaceChoisi) ? "#880e4f" : "#1565c0") +
            ";-fx-text-fill:white;-fx-background-radius:12;-fx-font-weight:bold;" +
            "-fx-padding:4 14;-fx-font-size:13px;");

        ComboBox<Voyage> cbVoyage = new ComboBox<>(FXCollections.observableArrayList(voyages));
        cbVoyage.setPrefWidth(340); cbVoyage.setPromptText("Choisir un voyage...");
        cbVoyage.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Voyage v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v.getTrajet() + "  |  " +
                    (v.getDateDepart() != null ? v.getDateDepart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "") +
                    "  |  " + String.format("%,.0f FCFA", v.getPrixParPlace()) +
                    "  |  " + v.getPlacesDisponibles() + " places");
            }
        });
        cbVoyage.setButtonCell(cbVoyage.getCellFactory().call(null));

        ComboBox<Utilisateur> cbClient = new ComboBox<>(FXCollections.observableArrayList(clients));
        cbClient.setPrefWidth(340); cbClient.setPromptText("Choisir le client...");
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        if ("CLIENT".equals(role)) {
            clients.stream().filter(c -> c.getId() ==
                SessionManager.getInstance().getUtilisateurConnecte().getId())
                .findFirst().ifPresent(cbClient::setValue);
            cbClient.setDisable(true);
        }

        Spinner<Integer> spPlaces = new Spinner<>(1, 20, 1);

        ComboBox<String> cbMode = new ComboBox<>(
            FXCollections.observableArrayList("ESPECES", "MOBILE_MONEY", "CARTE", "VIREMENT"));
        cbMode.setValue("ESPECES");

        Label lblPrix = new Label("Sélectionnez un voyage");
        lblPrix.setStyle("-fx-font-weight:bold;-fx-font-size:15px;-fx-text-fill:" +
            ("VIP".equals(typePlaceChoisi) ? "#880e4f" : "#1565c0") + ";");

        Runnable calcPrix = () -> {
            if (cbVoyage.getValue() != null) {
                double prix  = cbVoyage.getValue().getPrixParPlace();
                double total = prix * spPlaces.getValue();
                lblPrix.setText(String.format("Prix : %,.0f × %d = %,.0f FCFA",
                    prix, spPlaces.getValue(), total));
            }
        };
        cbVoyage.setOnAction(e -> calcPrix.run());
        spPlaces.valueProperty().addListener((obs, o, n) -> calcPrix.run());

        int r = 0;
        form.add(new Label(""),            0, r); form.add(lblBadge, 1, r++);
        form.add(lbl("Voyage *"),          0, r); form.add(cbVoyage, 1, r++);
        form.add(lbl("Client *"),          0, r); form.add(cbClient, 1, r++);
        form.add(lbl("Nb places *"),       0, r); form.add(spPlaces, 1, r++);
        form.add(lbl("Mode paiement"),     0, r); form.add(cbMode, 1, r++);
        form.add(lbl("Récapitulatif"),     0, r); form.add(lblPrix, 1, r++);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            if (cbVoyage.getValue() == null || cbClient.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Sélectionnez un voyage et un client.").showAndWait();
                return null;
            }
            double prix = cbVoyage.getValue().getPrixParPlace();
            Reservation res = new Reservation();
            res.setVoyageId(cbVoyage.getValue().getId());
            res.setClientId(cbClient.getValue().getId());
            res.setNombrePlaces(spPlaces.getValue());
            res.setMontantTotal(prix * spPlaces.getValue());
            res.setTypPlace(typePlaceChoisi);
            res.setNumeroSiege(typePlaceChoisi.charAt(0) + String.valueOf(spPlaces.getValue()));
            res.setMotifAnnulation(cbMode.getValue());
            return res;
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == null) return;
            String mode = res.getMotifAnnulation();
            res.setMotifAnnulation(null);

            boolean placesOk = voyageDAO.decrementerPlaces(res.getVoyageId(), res.getNombrePlaces());
            if (!placesOk) {
                new Alert(Alert.AlertType.ERROR, "Pas assez de places disponibles.").showAndWait();
                return;
            }
            if (resDAO.creer(res)) {
                resDAO.creerPaiement(res.getId(), res.getMontantTotal(), mode);
                chargerDonnees();
                // Proposer d'imprimer le reçu immédiatement
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "✅ Réservation confirmée !\nN° Ticket : " + res.getNumeroTicket() +
                    "\nMontant : " + String.format("%,.0f FCFA", res.getMontantTotal()) +
                    "\n\nVoulez-vous imprimer le reçu maintenant ?",
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Réservation réussie");
                confirm.showAndWait().ifPresent(b -> {
                    if (b == ButtonType.YES) {
                        // Recharger pour avoir toutes les infos jointes
                        donnees.stream()
                            .filter(r2 -> r2.getNumeroTicket().equals(res.getNumeroTicket()))
                            .findFirst()
                            .ifPresent(RecuImprimeur::imprimerReservation);
                    }
                });
            } else {
                voyageDAO.incrementerPlaces(res.getVoyageId(), res.getNombrePlaces());
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la réservation.").showAndWait();
            }
        });
    }

    private void payer(int idx) {
        if (idx < 0 || idx >= donnees.size()) return;
        Reservation r = donnees.get(idx);
        if ("PAYE".equals(r.getStatutPaiement())) {
            new Alert(Alert.AlertType.INFORMATION, "Cette réservation est déjà payée.").showAndWait();
            return;
        }
        ChoiceDialog<String> d = new ChoiceDialog<>("ESPECES","ESPECES","MOBILE_MONEY","CARTE","VIREMENT");
        d.setTitle("Mode de paiement"); d.setHeaderText("Choisir le mode de paiement");
        d.showAndWait().ifPresent(mode -> {
            resDAO.creerPaiement(r.getId(), r.getMontantTotal(), mode);
            chargerDonnees();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "✅ Paiement de " + String.format("%,.0f FCFA", r.getMontantTotal()) + " enregistré !\n\nImprimer le reçu ?",
                ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(b -> {
                if (b == ButtonType.YES) RecuImprimeur.imprimerReservation(donnees.get(idx));
            });
        });
    }

    private void annuler(int idx) {
        if (idx < 0 || idx >= donnees.size()) return;
        Reservation r = donnees.get(idx);
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Annulation"); d.setHeaderText("Motif d'annulation");
        d.showAndWait().ifPresent(motif -> {
            if (resDAO.annuler(r.getId(), motif)) {
                voyageDAO.incrementerPlaces(r.getVoyageId(), r.getNombrePlaces());
                chargerDonnees();
                new Alert(Alert.AlertType.INFORMATION, "Réservation annulée.").showAndWait();
            }
        });
    }

    private void imprimer(int idx) {
        if (idx < 0 || idx >= donnees.size()) return;
        RecuImprimeur.imprimerReservation(donnees.get(idx));
    }

    private <T> TableColumn<Reservation, T> col(String t, String p, int w) {
        TableColumn<Reservation, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private Button btn(String t, String bg) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:6;" +
            "-fx-cursor:hand;-fx-font-size:11px;-fx-padding:5 8;");
        return b;
    }
    private Button btnType(String t, String bg) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:8;" +
            "-fx-cursor:hand;-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:8 16;");
        return b;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight:bold;-fx-text-fill:#37474f;"); return l;
    }

    public BorderPane getView() { return root; }
}
