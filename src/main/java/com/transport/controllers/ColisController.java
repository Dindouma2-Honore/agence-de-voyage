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

public class ColisController {

    private final ColisDAO colisDAO      = new ColisDAO();
    private final DestinationDAO destDAO = new DestinationDAO();
    private final VoyageDAO voyageDAO    = new VoyageDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();

    private BorderPane root;
    private TableView<Colis> table;
    private ObservableList<Colis> donnees;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ColisController() {
        construireVue();
        chargerDonnees();
    }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");
        root.setPadding(new Insets(25));

        // ─── En-tête ─────────────────────────────────────────────────
        HBox entete = new HBox(15);
        entete.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("📦 Gestion des Colis");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        TextField tfRecherche = new TextField();
        tfRecherche.setPromptText("🔍 N° suivi COL-XXXXXXXX");
        tfRecherche.setPrefWidth(210);
        Button btnRecherche = btn("Tracer", "#37474f");
        btnRecherche.setOnAction(e -> tracerColis(tfRecherche.getText()));
        tfRecherche.setOnAction(e -> tracerColis(tfRecherche.getText()));

        Button btnAdd = btn("➕ Enregistrer un colis", "#1a237e");
        btnAdd.setOnAction(e -> ouvrirFormulaire());
        Button btnRef = btn("🔄 Actualiser", "#546e7a");
        btnRef.setOnAction(e -> chargerDonnees());

        entete.getChildren().addAll(titre, sp, tfRecherche, btnRecherche, btnAdd, btnRef);
        root.setTop(entete);
        BorderPane.setMargin(entete, new Insets(0, 0, 15, 0));

        // ─── Statistiques ────────────────────────────────────────────
        HBox stats = new HBox(15);
        stats.getChildren().addAll(
            statCard("⏳ En attente", String.valueOf(colisDAO.compterParStatut("EN_ATTENTE")),  "#e65100", "#fff3e0"),
            statCard("🚚 En transit", String.valueOf(colisDAO.compterParStatut("EN_TRANSIT")),  "#1565c0", "#e3f2fd"),
            statCard("✅ Livrés",     String.valueOf(colisDAO.compterParStatut("LIVRE")),        "#2e7d32", "#e8f5e9"),
            statCard("↩️ Retournés", String.valueOf(colisDAO.compterParStatut("RETOURNE")),     "#880e4f", "#fce4ec")
        );
        for (javafx.scene.Node n : stats.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ─── Table ───────────────────────────────────────────────────
        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);

        TableColumn<Colis, String> cNum = new TableColumn<>("N° Suivi");
        cNum.setPrefWidth(155);
        cNum.setCellValueFactory(new PropertyValueFactory<>("numeroSuivi"));
        cNum.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle("-fx-font-weight:bold;-fx-text-fill:#1a237e;-fx-font-family:monospace;");
            }
        });

        TableColumn<Colis, String> cExp  = col("Expéditeur",    "expediteurNom",   140);
        TableColumn<Colis, String> cDest = col("Destinataire",  "destinataireNom", 140);
        TableColumn<Colis, String> cTel  = col("Tél Dest.",     "destinataireTel", 115);
        TableColumn<Colis, String> cTraj = col("Trajet",        "trajet",          165);

        TableColumn<Colis, Double> cPoids = new TableColumn<>("Poids");
        cPoids.setPrefWidth(75);
        cPoids.setCellValueFactory(new PropertyValueFactory<>("poidsKg"));
        cPoids.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p + " kg");
            }
        });

        TableColumn<Colis, Double> cTarif = new TableColumn<>("Tarif");
        cTarif.setPrefWidth(115);
        cTarif.setCellValueFactory(new PropertyValueFactory<>("tarif"));
        cTarif.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : String.format("%,.0f FCFA", t));
                if (!empty) setStyle("-fx-font-weight:bold;-fx-text-fill:#2e7d32;");
            }
        });

        TableColumn<Colis, String> cVoy = new TableColumn<>("Voyage assigné");
        cVoy.setPrefWidth(130);
        cVoy.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getVoyageDate() != null ? d.getValue().getVoyageDate() : "Non assigné"));

        TableColumn<Colis, String> cStat = new TableColumn<>("Statut");
        cStat.setPrefWidth(115);
        cStat.setCellValueFactory(new PropertyValueFactory<>("statut"));
        cStat.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                String label = switch (s) {
                    case "EN_ATTENTE" -> "⏳ En attente";
                    case "EN_TRANSIT" -> "🚚 En transit";
                    case "LIVRE"      -> "✅ Livré";
                    case "RETOURNE"   -> "↩️ Retourné";
                    default           -> "❌ " + s;
                };
                setText(label);
                setStyle("-fx-text-fill:" + switch (s) {
                    case "EN_ATTENTE" -> "#e65100";
                    case "EN_TRANSIT" -> "#1565c0";
                    case "LIVRE"      -> "#2e7d32";
                    case "RETOURNE"   -> "#880e4f";
                    default           -> "#c62828";
                } + ";-fx-font-weight:bold;");
            }
        });

        TableColumn<Colis, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(255);
        cAct.setCellFactory(tc -> new TableCell<>() {
            final Button bTransit = btn("🚚 Transit", "#1565c0");
            final Button bLivrer  = btn("✅ Livré",   "#2e7d32");
            final Button bRetour  = btn("↩️ Retour",  "#880e4f");
            final Button bRecu    = btn("🖨️ Reçu",   "#37474f");
            final HBox box = new HBox(4, bTransit, bLivrer, bRetour, bRecu);
            {
                box.setAlignment(Pos.CENTER);
                bTransit.setOnAction(e -> changerStatut(getIndex(), "EN_TRANSIT"));
                bLivrer.setOnAction(e  -> changerStatut(getIndex(), "LIVRE"));
                bRetour.setOnAction(e  -> changerStatut(getIndex(), "RETOURNE"));
                bRecu.setOnAction(e    -> imprimer(getIndex()));
            }
            @Override protected void updateItem(Void x, boolean empty) {
                super.updateItem(x, empty); setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(cNum, cExp, cDest, cTel, cTraj, cPoids, cTarif, cVoy, cStat, cAct);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox wrap = new VBox(10, stats, new Separator(), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        wrap.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.08),8,0,0,2);");
        wrap.setPadding(new Insets(15));
        root.setCenter(wrap);
    }

    private void chargerDonnees() {
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        if ("CLIENT".equals(role))
            donnees.setAll(colisDAO.listerParExpediteur(
                SessionManager.getInstance().getUtilisateurConnecte().getId()));
        else
            donnees.setAll(colisDAO.listerTous());
    }

    private void changerStatut(int idx, String statut) {
        if (idx < 0 || idx >= donnees.size()) return;
        Colis c = donnees.get(idx);
        if (colisDAO.changerStatut(c.getId(), statut)) chargerDonnees();
    }

    private void imprimer(int idx) {
        if (idx < 0 || idx >= donnees.size()) return;
        RecuImprimeur.imprimerColis(donnees.get(idx));
    }

    private void tracerColis(String numero) {
        if (numero == null || numero.isBlank()) return;
        Colis c = colisDAO.rechercherParNumero(numero);
        if (c == null) {
            new Alert(Alert.AlertType.WARNING,
                "Aucun colis trouvé avec le N° : " + numero, ButtonType.OK).showAndWait();
            return;
        }
        afficherDetailColis(c);
    }

    private void afficherDetailColis(Colis c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Détail du colis");
        a.setHeaderText("📦 " + c.getNumeroSuivi());
        a.setContentText(
            "Expéditeur       : " + c.getExpediteurNom() + "\n" +
            "Destinataire     : " + c.getDestinataireNom() + "\n" +
            "Tél destinataire : " + c.getDestinataireTel() + "\n" +
            "Trajet           : " + c.getTrajet() + "\n" +
            "Description      : " + (c.getDescription() != null ? c.getDescription() : "—") + "\n" +
            "Poids            : " + c.getPoidsKg() + " kg\n" +
            "Tarif            : " + String.format("%,.0f FCFA", c.getTarif()) + "\n" +
            "Voyage assigné   : " + (c.getVoyageDate() != null ? c.getVoyageDate() : "Non assigné") + "\n" +
            "Date d'envoi     : " + (c.getDateEnvoi() != null ? c.getDateEnvoi().format(FMT) : "—") + "\n" +
            "Date livraison   : " + (c.getDateLivraison() != null ? c.getDateLivraison().format(FMT) : "—") + "\n" +
            "Statut           : " + c.getStatut()
        );
        a.showAndWait();
    }

    private void ouvrirFormulaire() {
        List<Destination> dests   = destDAO.listerToutes();
        List<Voyage>      voyages = voyageDAO.listerDisponibles();
        List<Utilisateur> clients = userDAO.listerParRole("CLIENT");

        if (dests.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucune destination disponible.", ButtonType.OK).showAndWait();
            return;
        }

        Dialog<Colis> dialog = new Dialog<>();
        dialog.setTitle("Enregistrer un colis");
        dialog.setHeaderText("📦 Nouveau colis");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(11); form.setPadding(new Insets(20));

        ComboBox<Utilisateur> cbExp = new ComboBox<>(FXCollections.observableArrayList(clients));
        cbExp.setPrefWidth(280); cbExp.setPromptText("Choisir l'expéditeur...");
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        if ("CLIENT".equals(role)) {
            clients.stream()
                .filter(u -> u.getId() == SessionManager.getInstance().getUtilisateurConnecte().getId())
                .findFirst().ifPresent(cbExp::setValue);
            cbExp.setDisable(true);
        }

        TextField tfDestNom = champ("Nom complet *");
        TextField tfDestTel = champ("Téléphone *");

        ComboBox<Destination> cbDest = new ComboBox<>(FXCollections.observableArrayList(dests));
        cbDest.setPrefWidth(280); cbDest.setPromptText("Choisir la destination...");

        ComboBox<Voyage> cbVoyage = new ComboBox<>(FXCollections.observableArrayList(voyages));
        cbVoyage.setPrefWidth(280); cbVoyage.setPromptText("Assigner à un voyage (optionnel)");
        cbVoyage.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Voyage v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null :
                    v.getTrajet() + " | " + (v.getDateDepart() != null ?
                        v.getDateDepart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
            }
        });
        cbVoyage.setButtonCell(cbVoyage.getCellFactory().call(null));

        Spinner<Double> spPoids = new Spinner<>(0.1, 500.0, 1.0, 0.5);
        spPoids.setEditable(true); spPoids.setPrefWidth(150);

        Label lblTarif = new Label("— FCFA");
        lblTarif.setStyle("-fx-font-weight:bold;-fx-font-size:16px;-fx-text-fill:#2e7d32;");

        Runnable calcTarif = () -> {
            if (cbDest.getValue() != null) {
                double t = colisDAO.calculerTarif(cbDest.getValue().getId(), spPoids.getValue());
                lblTarif.setText(String.format("%,.0f FCFA", t));
            }
        };
        cbDest.setOnAction(e -> calcTarif.run());
        spPoids.valueProperty().addListener((obs, o, n) -> calcTarif.run());

        TextField tfDesc  = champ("Description du contenu");
        TextField tfNotes = champ("Notes / Instructions spéciales");

        int r = 0;
        form.add(lbl("Expéditeur *"),        0, r); form.add(cbExp, 1, r++);
        form.add(lbl("Destinataire (Nom) *"),0, r); form.add(tfDestNom, 1, r++);
        form.add(lbl("Destinataire (Tél) *"),0, r); form.add(tfDestTel, 1, r++);
        form.add(lbl("Destination *"),       0, r); form.add(cbDest, 1, r++);
        form.add(lbl("Voyage assigné"),      0, r); form.add(cbVoyage, 1, r++);
        form.add(lbl("Poids (kg) *"),        0, r); form.add(spPoids, 1, r++);
        form.add(lbl("Tarif calculé"),       0, r); form.add(lblTarif, 1, r++);
        form.add(lbl("Description"),         0, r); form.add(tfDesc, 1, r++);
        form.add(lbl("Notes"),               0, r); form.add(tfNotes, 1, r++);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                if (cbExp.getValue()  == null)       throw new Exception("Sélectionnez un expéditeur.");
                if (tfDestNom.getText().isBlank())    throw new Exception("Entrez le nom du destinataire.");
                if (tfDestTel.getText().isBlank())    throw new Exception("Entrez le téléphone du destinataire.");
                if (cbDest.getValue() == null)        throw new Exception("Sélectionnez une destination.");

                Colis c = new Colis();
                c.setExpediteurId(cbExp.getValue().getId());
                c.setDestinataireNom(tfDestNom.getText().trim());
                c.setDestinataireTel(tfDestTel.getText().trim());
                c.setDestinationId(cbDest.getValue().getId());
                c.setVoyageId(cbVoyage.getValue() != null ? cbVoyage.getValue().getId() : 0);
                c.setPoidsKg(spPoids.getValue());
                c.setTarif(colisDAO.calculerTarif(cbDest.getValue().getId(), spPoids.getValue()));
                c.setDescription(tfDesc.getText().trim());
                c.setNotes(tfNotes.getText().trim());
                return c;
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                return null;
            }
        });

        dialog.showAndWait().ifPresent(c -> {
            if (c == null) return;
            if (colisDAO.enregistrer(c)) {
                chargerDonnees();
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "✅ Colis enregistré !\nN° Suivi : " + c.getNumeroSuivi() +
                    "\nTarif : " + String.format("%,.0f FCFA", c.getTarif()) +
                    "\n\nVoulez-vous imprimer le reçu maintenant ?",
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Colis enregistré");
                confirm.showAndWait().ifPresent(b -> {
                    if (b == ButtonType.YES) {
                        donnees.stream()
                            .filter(cc -> cc.getNumeroSuivi().equals(c.getNumeroSuivi()))
                            .findFirst()
                            .ifPresent(RecuImprimeur::imprimerColis);
                    }
                });
            } else {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement.", ButtonType.OK).showAndWait();
            }
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────
    private <T> TableColumn<Colis, T> col(String t, String p, int w) {
        TableColumn<Colis, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private Button btn(String t, String bg) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:6;" +
            "-fx-cursor:hand;-fx-font-size:11px;-fx-padding:5 7;");
        return b;
    }
    private TextField champ(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); tf.setPrefWidth(280); return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight:bold;-fx-text-fill:#37474f;"); return l;
    }
    private HBox statCard(String titre, String valeur, String couleur, String bg) {
        HBox c = new HBox(10);
        c.setPadding(new Insets(12, 18, 12, 18));
        c.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:8;");
        c.setAlignment(Pos.CENTER_LEFT);
        VBox txt = new VBox(3);
        Label lt = new Label(titre);
        lt.setStyle("-fx-font-size:11px;-fx-text-fill:" + couleur + ";-fx-font-weight:bold;");
        Label lv = new Label(valeur);
        lv.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");
        txt.getChildren().addAll(lt, lv);
        c.getChildren().add(txt);
        return c;
    }

    public BorderPane getView() { return root; }
}
