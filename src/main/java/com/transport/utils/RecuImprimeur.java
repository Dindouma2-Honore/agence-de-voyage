package com.transport.utils;

import com.transport.models.Colis;
import com.transport.models.Reservation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Génère et affiche un aperçu imprimable du reçu
 * (réservation voyage ou colis), puis lance l'impression.
 */
public class RecuImprimeur {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String AGENCE = "AGENCE DE TRANSPORT";
    private static final String ADRESSE = "Yaoundé, Cameroun  |  Tél: +237 6XX XXX XXX";

    // ─── REÇU RÉSERVATION ─────────────────────────────────────────
    public static void imprimerReservation(Reservation r) {
        VBox recu = construireRecu(buildContenuReservation(r), "BILLET DE VOYAGE");
        afficherApercu(recu, "Reçu — " + r.getNumeroTicket());
    }

    // ─── REÇU COLIS ───────────────────────────────────────────────
    public static void imprimerColis(Colis c) {
        VBox recu = construireRecu(buildContenuColis(c), "BON D'EXPÉDITION COLIS");
    afficherApercu(recu, "Reçu colis — " + c.getNumeroSuivi());
    }

    // ─── Construction du contenu réservation ──────────────────────
    private static VBox buildContenuReservation(Reservation r) {
        VBox box = new VBox(6);
        String typeBadge = "VIP".equals(r.getTypPlace()) ? "⭐ VIP" : "🚌 Classique";

        box.getChildren().addAll(
            separateur(),
            ligne("N° Ticket",      r.getNumeroTicket(),                         true),
            ligne("Type de place",  typeBadge,                                   true),
            separateur(),
            titre2("PASSAGER"),
            ligne("Client",         r.getClientNom(),                            false),
            separateur(),
            titre2("VOYAGE"),
            ligne("Trajet",         r.getTrajet(),                               false),
            ligne("Départ",         r.getDateDepart() != null ? r.getDateDepart().format(FMT) : "—", false),
            ligne("Siège(s)",        r.getNumeroSiege(),                         false),
            ligne("Nb places",      String.valueOf(r.getNombrePlaces()),          false),
            separateur(),
            titre2("PAIEMENT"),
            ligne("Montant total",  String.format("%,.0f FCFA", r.getMontantTotal()), true),
            ligne("Statut paiement",r.getStatutPaiement(),                       false),
            ligne("Statut",         r.getStatut(),                               false),
            separateur(),
            ligne("Date émission",  LocalDateTime.now().format(FMT),             false)
        );
        return box;
    }

    // ─── Construction du contenu colis ────────────────────────────
    private static VBox buildContenuColis(Colis c) {
        VBox box = new VBox(6);
        box.getChildren().addAll(
            separateur(),
            ligne("N° Suivi",       c.getNumeroSuivi(),                          true),
            separateur(),
            titre2("EXPÉDITEUR"),
            ligne("Nom",            c.getExpediteurNom(),                        false),
            separateur(),
            titre2("DESTINATAIRE"),
            ligne("Nom",            c.getDestinataireNom(),                      false),
            ligne("Téléphone",      c.getDestinataireTel(),                      false),
            separateur(),
            titre2("COLIS"),
            ligne("Destination",    c.getTrajet(),                               false),
            ligne("Description",    c.getDescription() != null ? c.getDescription() : "—", false),
            ligne("Poids",          c.getPoidsKg() + " kg",                      false),
            ligne("Voyage assigné", c.getVoyageDate() != null ? c.getVoyageDate() : "Non assigné", false),
            separateur(),
            titre2("PAIEMENT"),
            ligne("Tarif",          String.format("%,.0f FCFA", c.getTarif()),   true),
            ligne("Statut",         c.getStatut(),                               false),
            separateur(),
            ligne("Date émission",  LocalDateTime.now().format(FMT),             false)
        );
        return box;
    }

    // ─── Assemblage visuel du reçu ─────────────────────────────────
    private static VBox construireRecu(VBox contenu, String typeTitre) {
        VBox recu = new VBox(0);
        recu.setStyle("-fx-background-color:white;");
        recu.setPrefWidth(380);
        recu.setMaxWidth(380);

        // En-tête
        VBox entete = new VBox(4);
        entete.setAlignment(Pos.CENTER);
        entete.setStyle("-fx-background-color:#1a237e;-fx-padding:18 20;");

        Text nomAgence = new Text(AGENCE);
        nomAgence.setFill(Color.WHITE);
        nomAgence.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text adresse = new Text(ADRESSE);
        adresse.setFill(Color.web("#b3c5f8"));
        adresse.setFont(Font.font("Arial", 10));

        Text typeTxt = new Text("═══  " + typeTitre + "  ═══");
        typeTxt.setFill(Color.web("#ffd54f"));
        typeTxt.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        entete.getChildren().addAll(nomAgence, adresse, typeTxt);

        // Corps
        contenu.setStyle("-fx-padding:15 20;");

        // Pied
        VBox pied = new VBox(4);
        pied.setAlignment(Pos.CENTER);
        pied.setStyle("-fx-background-color:#f5f5f5;-fx-padding:12 20;-fx-border-color:#e0e0e0;" +
            "-fx-border-width:1 0 0 0;");
        Text merci = new Text("Merci de votre confiance !");
        merci.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        merci.setFill(Color.web("#1a237e"));
        Text mention = new Text("Ce reçu fait office de preuve de paiement.");
        mention.setFont(Font.font("Arial", 9));
        mention.setFill(Color.GRAY);
        pied.getChildren().addAll(merci, mention);

        recu.getChildren().addAll(entete, contenu, pied);
        return recu;
    }

    // ─── Fenêtre d'aperçu avec bouton Imprimer ─────────────────────
    private static void afficherApercu(VBox recu, String titre) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Aperçu — " + titre);

        ScrollPane scroll = new ScrollPane(recu);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;");

        // Boutons
        Button btnImprimer = new Button("🖨️  Imprimer");
        btnImprimer.setStyle("-fx-background-color:#1a237e;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:9 22;");

        Button btnFermer = new Button("✖  Fermer");
        btnFermer.setStyle("-fx-background-color:#ef5350;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:9 22;");

        btnFermer.setOnAction(e -> stage.close());
        btnImprimer.setOnAction(e -> lancerImpression(recu, stage));

        HBox boutons = new HBox(15, btnImprimer, btnFermer);
        boutons.setAlignment(Pos.CENTER);
        boutons.setStyle("-fx-background-color:#f5f6fa;-fx-padding:14;");

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(boutons);
        root.setStyle("-fx-background-color:white;");

        Scene scene = new Scene(root, 440, 620);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // ─── Lancement impression système ──────────────────────────────
    private static void lancerImpression(Node noeud, Stage parent) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            new Alert(Alert.AlertType.ERROR,
                "Aucune imprimante disponible.\nVérifiez la connexion de votre imprimante.",
                ButtonType.OK).showAndWait();
            return;
        }
        boolean ok = job.showPrintDialog(parent);
        if (ok) {
            PageLayout layout = job.getPrinter().createPageLayout(
                Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
            boolean printed = job.printPage(layout, noeud);
            if (printed) {
                job.endJob();
                new Alert(Alert.AlertType.INFORMATION,
                    "✅ Document envoyé à l'imprimante !", ButtonType.OK).showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Échec de l'impression.", ButtonType.OK).showAndWait();
            }
        }
    }

    // ─── Helpers visuels ───────────────────────────────────────────
    private static HBox ligne(String cle, String valeur, boolean gras) {
        HBox h = new HBox();
        h.setPadding(new Insets(2, 0, 2, 0));
        Label lCle = new Label(cle);
        lCle.setPrefWidth(130);
        lCle.setStyle("-fx-font-size:11px;-fx-text-fill:#546e7a;");

        Label lVal = new Label(valeur != null ? valeur : "—");
        lVal.setStyle("-fx-font-size:12px;" +
            (gras ? "-fx-font-weight:bold;-fx-text-fill:#1a237e;" : "-fx-text-fill:#212121;"));
        lVal.setWrapText(true);
        lVal.setMaxWidth(220);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        h.getChildren().addAll(lCle, sp, lVal);
        return h;
    }

    private static Label titre2(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#78909c;" +
            "-fx-padding:6 0 2 0;");
        return l;
    }

    private static Separator separateur() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:#e0e0e0;");
        return s;
    }
}
