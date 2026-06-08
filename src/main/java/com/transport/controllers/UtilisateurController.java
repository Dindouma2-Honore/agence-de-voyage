package com.transport.controllers;

import com.transport.dao.UtilisateurDAO;
import com.transport.models.Utilisateur;
import com.transport.utils.UIHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class UtilisateurController {

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private BorderPane root;
    private TableView<Utilisateur> table;
    private ObservableList<Utilisateur> donnees;
    private ComboBox<String> cbRole;

    public UtilisateurController() { construireVue(); chargerDonnees(); }

    private void construireVue() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f5f9;");
        root.setPadding(new Insets(24));

        cbRole = new ComboBox<>(FXCollections.observableArrayList("TOUS","ADMIN","AGENT","CHAUFFEUR","CLIENT"));
        cbRole.setValue("TOUS");
        cbRole.setStyle("-fx-background-radius:8;-fx-border-color:#cbd5e1;-fx-background-color:white;");
        cbRole.setOnAction(e -> chargerDonnees());

        Button btnAdd = UIHelper.btnPrimary("➕  Nouvel utilisateur");
        btnAdd.setOnAction(e -> ouvrirFormulaire(null));
        Button btnRef = UIHelper.btnSecondary("🔄  Actualiser");
        btnRef.setOnAction(e -> chargerDonnees());

        HBox entete = UIHelper.enteteModule("👥  Gestion des Utilisateurs",
            new Label("Rôle :"), cbRole, btnAdd, btnRef);

        // Stats
        HBox stats = new HBox(12);
        stats.setPadding(new Insets(0, 0, 16, 0));
        stats.getChildren().addAll(
            miniStat("👑 Admins",    String.valueOf(dao.compterParRole("ADMIN")),    "#7c3aed","#f3e8ff"),
            miniStat("🧑 Agents",   String.valueOf(dao.compterParRole("AGENT")),    "#2563eb","#dbeafe"),
            miniStat("🚌 Chauffeurs",String.valueOf(dao.compterParRole("CHAUFFEUR")),"#16a34a","#dcfce7"),
            miniStat("👤 Clients",  String.valueOf(dao.compterParRole("CLIENT")),   "#ea580c","#ffedd5")
        );

        table = new TableView<>();
        donnees = FXCollections.observableArrayList();
        table.setItems(donnees);

        TableColumn<Utilisateur, String> cNom    = col("Nom",      "nom",      120);
        TableColumn<Utilisateur, String> cPrenom = col("Prénom",   "prenom",   120);
        TableColumn<Utilisateur, String> cEmail  = col("Email",    "email",    200);
        TableColumn<Utilisateur, String> cTel    = col("Téléphone","telephone", 130);

        TableColumn<Utilisateur, String> cRole = new TableColumn<>("Rôle");
        cRole.setPrefWidth(115);
        cRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        cRole.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setGraphic(null); return; }
                String[] bc = switch (r) {
                    case "ADMIN"     -> new String[]{"👑 Admin",     "#f3e8ff","#7c3aed"};
                    case "AGENT"     -> new String[]{"🧑 Agent",     "#dbeafe","#1d4ed8"};
                    case "CHAUFFEUR" -> new String[]{"🚌 Chauffeur", "#dcfce7","#15803d"};
                    default          -> new String[]{"👤 Client",    "#f1f5f9","#475569"};
                };
                setGraphic(UIHelper.badge(bc[0], bc[1], bc[2]));
            }
        });

        TableColumn<Utilisateur, Boolean> cActif = new TableColumn<>("Statut");
        cActif.setPrefWidth(90);
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

        TableColumn<Utilisateur, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(180);
        cAct.setCellFactory(tc -> new TableCell<>() {
            final Button bEdit = UIHelper.btnTable("✏️ Modifier",    "#2563eb");
            final Button bDes  = UIHelper.btnTable("🚫 Désactiver",  "#dc2626");
            final HBox box = new HBox(6, bEdit, bDes);
            { box.setAlignment(Pos.CENTER);
              bEdit.setOnAction(e -> ouvrirFormulaire(donnees.get(getIndex())));
              bDes.setOnAction(e -> {
                  Utilisateur u = donnees.get(getIndex());
                  if (confirm("Désactiver " + u.getNomComplet() + " ?")) {
                      dao.supprimer(u.getId()); chargerDonnees(); }
              }); }
            @Override protected void updateItem(Void x, boolean empty) {
                super.updateItem(x, empty); setGraphic(empty ? null : box); }
        });

        table.getColumns().addAll(cNom, cPrenom, cEmail, cTel, cRole, cActif, cAct);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox tableCard = UIHelper.card();
        tableCard.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(0, entete, stats, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        root.setCenter(content);
    }

    private void chargerDonnees() {
        String r = cbRole.getValue();
        donnees.setAll("TOUS".equals(r) ? dao.listerTous() : dao.listerParRole(r));
    }

    private void ouvrirFormulaire(Utilisateur u) {
        Dialog<Utilisateur> dlg = new Dialog<>();
        dlg.setTitle(u == null ? "Nouvel utilisateur" : "Modifier utilisateur");
        dlg.getDialogPane().setStyle("-fx-background-color:white;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(12); form.setPadding(new Insets(20));

        TextField tfNom    = UIHelper.champTexte("Nom");
        TextField tfPrenom = UIHelper.champTexte("Prénom");
        TextField tfEmail  = UIHelper.champTexte("email@exemple.com");
        TextField tfTel    = UIHelper.champTexte("+237 6XX XXX XXX");
        PasswordField pfMdp= new PasswordField();
        pfMdp.setPromptText(u == null ? "Mot de passe *" : "Laisser vide = inchangé");
        pfMdp.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#cbd5e1;-fx-padding:7 10;");
        pfMdp.setPrefWidth(260);
        ComboBox<String> cbRole = new ComboBox<>(
            FXCollections.observableArrayList("CLIENT","AGENT","CHAUFFEUR","ADMIN"));
        cbRole.setValue("CLIENT"); cbRole.setPrefWidth(260);
        CheckBox cbActif = new CheckBox("Compte actif"); cbActif.setSelected(true);

        if (u != null) {
            tfNom.setText(u.getNom()); tfPrenom.setText(u.getPrenom());
            tfEmail.setText(u.getEmail()); tfTel.setText(u.getTelephone());
            cbRole.setValue(u.getRole()); cbActif.setSelected(u.isActif());
        }

        int r = 0;
        form.add(UIHelper.champLabel("Nom *"),       0,r); form.add(tfNom,   1,r++);
        form.add(UIHelper.champLabel("Prénom *"),    0,r); form.add(tfPrenom,1,r++);
        form.add(UIHelper.champLabel("Email *"),     0,r); form.add(tfEmail, 1,r++);
        form.add(UIHelper.champLabel("Téléphone"),   0,r); form.add(tfTel,   1,r++);
        form.add(UIHelper.champLabel("Mot de passe"),0,r); form.add(pfMdp,   1,r++);
        form.add(UIHelper.champLabel("Rôle"),        0,r); form.add(cbRole,  1,r++);
        if (u != null) { form.add(new Label(""), 0,r); form.add(cbActif,1,r++); }

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().setPrefWidth(460);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            Utilisateur nu = u != null ? u : new Utilisateur();
            nu.setNom(tfNom.getText().trim()); nu.setPrenom(tfPrenom.getText().trim());
            nu.setEmail(tfEmail.getText().trim()); nu.setTelephone(tfTel.getText().trim());
            nu.setRole(cbRole.getValue());
            if (u != null) nu.setActif(cbActif.isSelected());
            if (!pfMdp.getText().isBlank()) nu.setMotDePasse(pfMdp.getText());
            return nu;
        });

        dlg.showAndWait().ifPresent(nu -> {
            boolean ok;
            if (u == null) ok = dao.inscrire(nu);
            else { ok = dao.modifier(nu); if (!pfMdp.getText().isBlank()) dao.changerMotDePasse(nu.getId(), pfMdp.getText()); }
            if (ok) { chargerDonnees(); new Alert(Alert.AlertType.INFORMATION, u == null ? "✅  Utilisateur créé !" : "✅  Modifié !").showAndWait(); }
            else new Alert(Alert.AlertType.ERROR, "Opération échouée. Email déjà utilisé ?").showAndWait();
        });
    }

    private <T> TableColumn<Utilisateur, T> col(String t, String p, int w) {
        TableColumn<Utilisateur, T> c = new TableColumn<>(t);
        c.setPrefWidth(w); c.setCellValueFactory(new PropertyValueFactory<>(p)); return c;
    }
    private HBox miniStat(String label, String val, String fg, String bg) {
        HBox h = new HBox(10); h.setPadding(new Insets(10,16,10,16));
        h.setStyle("-fx-background-color:"+bg+";-fx-background-radius:8;"); h.setAlignment(Pos.CENTER_LEFT);
        Label lv = new Label(val); lv.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:"+fg+";");
        Label ll = new Label(label); ll.setStyle("-fx-font-size:11px;-fx-text-fill:"+fg+";");
        h.getChildren().addAll(lv, ll); return h;
    }
    private boolean confirm(String m) {
        return new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO)
            .showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }
    public BorderPane getView() { return root; }
}
