package com.transport;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.transport.controllers.*;
import com.transport.utils.DatabaseConnection;
import com.transport.utils.SessionManager;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Button btnActif = null;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("SIGAVT — Système de Gestion d'Agence de Voyage Terrestre");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        afficherEcranConnexion();
        stage.show();
    }

    public void afficherEcranConnexion() {
        LoginController lc = new LoginController(this);
        Scene scene = new Scene(lc.getView(), 1100, 700);
        appliquerCSS(scene);
        primaryStage.setScene(scene);
    }

    public void afficherApplicationPrincipale() {
        rootLayout = new BorderPane();
        rootLayout.setLeft(construireSidebar());
        rootLayout.setTop(construireHeader());
        afficherModule("dashboard");

        Scene scene = new Scene(rootLayout, 1280, 800);
        appliquerCSS(scene);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    private void appliquerCSS(Scene scene) {
        java.net.URL css = getClass().getResource("/css/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    // ─── HEADER ──────────────────────────────────────────────────
    private HBox construireHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 20));
        header.setPrefHeight(52);
        header.getStyleClass().add("header");

        Label logo = new Label("🚌 SIGAVT");
        logo.setStyle("-fx-text-fill:white;-fx-font-size:17px;-fx-font-weight:bold;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Heure
        Label lHeure = new Label();
        lHeure.setStyle("-fx-text-fill:#93c5fd;-fx-font-size:12px;");
        javafx.animation.Timeline clock = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e ->
                lHeure.setText(java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy")))));
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();
        lHeure.setText(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy")));

        // Notification fictive
        Label notif = new Label("🔔");
        notif.setStyle("-fx-text-fill:#93c5fd;-fx-font-size:16px;-fx-cursor:hand;");

        // Utilisateur
        String nom  = SessionManager.getInstance().getUtilisateurConnecte().getNomComplet();
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        Label lUser = new Label("👤  " + nom + "  ·  " + role);
        lUser.setStyle("-fx-text-fill:#e2e8f0;-fx-font-size:12px;");

        Button btnDeconn = new Button("Déconnexion");
        btnDeconn.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-font-size:11px;" +
            "-fx-font-weight:bold;-fx-background-radius:6;-fx-cursor:hand;-fx-padding:5 12;");
        btnDeconn.setOnAction(e -> {
            SessionManager.getInstance().deconnecter();
            afficherEcranConnexion();
        });

        header.getChildren().addAll(logo, sp, lHeure, notif, lUser, btnDeconn);
        return header;
    }

    // ─── SIDEBAR ─────────────────────────────────────────────────
    private VBox construireSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(12, 8, 12, 8));
        sidebar.getStyleClass().add("sidebar");

        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();

        // Logo zone
        VBox logoBox = new VBox(2);
        logoBox.setPadding(new Insets(8, 8, 16, 8));
        Label logo1 = new Label("🚌  SIGAVT");
        logo1.setStyle("-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;");
        Label logo2 = new Label("Agence de Transport");
        logo2.setStyle("-fx-text-fill:#64748b;-fx-font-size:10px;");
        logoBox.getChildren().addAll(logo1, logo2);

        Separator sep0 = new Separator();
        sep0.setStyle("-fx-background-color:#334155;");

        sidebar.getChildren().addAll(logoBox, sep0);

        // Menus principaux
        sectionLabel(sidebar, "PRINCIPAL");
        menuBtn(sidebar, "📊  Tableau de bord",  "dashboard",  true);
        menuBtn(sidebar, "✈️  Voyages",           "voyages",    false);
        menuBtn(sidebar, "🎫  Réservations",      "reservations", false);
        menuBtn(sidebar, "📦  Colis",             "colis",      false);

        if ("ADMIN".equals(role) || "AGENT".equals(role)) {
            sectionLabel(sidebar, "GESTION");
            menuBtn(sidebar, "👥  Utilisateurs",  "utilisateurs", false);
            menuBtn(sidebar, "🚌  Véhicules",     "vehicules",    false);
            menuBtn(sidebar, "🗺️  Destinations",  "destinations", false);
        }

        if ("ADMIN".equals(role)) {
            sectionLabel(sidebar, "FINANCE");
            menuBtn(sidebar, "💰  Paiements",     "paiements",    false);
            menuBtn(sidebar, "📈  Statistiques",  "statistiques", false);
        }

        // Pied sidebar
        Region spSidebar = new Region(); VBox.setVgrow(spSidebar, Priority.ALWAYS);
        sidebar.getChildren().add(spSidebar);

        Separator sepFin = new Separator();
        sepFin.setStyle("-fx-background-color:#334155;");
        sidebar.getChildren().add(sepFin);

        String nom  = SessionManager.getInstance().getUtilisateurConnecte().getNomComplet();
        VBox userBox = new VBox(2);
        userBox.setPadding(new Insets(10, 8, 4, 8));
        Label lNom  = new Label("👤  " + nom);
        lNom.setStyle("-fx-text-fill:#e2e8f0;-fx-font-size:12px;-fx-font-weight:bold;");
        Label lRole = new Label(role);
        lRole.setStyle("-fx-text-fill:#64748b;-fx-font-size:10px;");
        userBox.getChildren().addAll(lNom, lRole);
        sidebar.getChildren().add(userBox);

        return sidebar;
    }

    private void sectionLabel(VBox sidebar, String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("sidebar-section");
        sidebar.getChildren().add(l);
    }

    private void menuBtn(VBox sidebar, String texte, String module, boolean actif) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(actif ? "sidebar-btn-active" : "sidebar-btn");
        VBox.setMargin(btn, new Insets(1, 0, 1, 0));
        btn.setOnAction(e -> {
            if (btnActif != null) {
                btnActif.getStyleClass().remove("sidebar-btn-active");
                if (!btnActif.getStyleClass().contains("sidebar-btn"))
                    btnActif.getStyleClass().add("sidebar-btn");
            }
            btn.getStyleClass().remove("sidebar-btn");
            if (!btn.getStyleClass().contains("sidebar-btn-active"))
                btn.getStyleClass().add("sidebar-btn-active");
            btnActif = btn;
            afficherModule(module);
        });
        if (actif) btnActif = btn;
        sidebar.getChildren().add(btn);
    }

    public void afficherModule(String module) {
        javafx.scene.Node vue = switch (module) {
            case "dashboard"    -> new DashboardController().getView();
            case "voyages"      -> new VoyageController().getView();
            case "reservations" -> new ReservationController().getView();
            case "colis"        -> new ColisController().getView();
            case "utilisateurs" -> new UtilisateurController().getView();
            case "vehicules"    -> new VehiculeController().getView();
            case "destinations" -> new DestinationController().getView();
            case "paiements"    -> new PaiementController().getView();
            case "statistiques" -> new StatistiqueController().getView();
            default             -> new DashboardController().getView();
        };
        rootLayout.setCenter(vue);
    }

    @Override
    public void stop() { DatabaseConnection.getInstance().close(); }

    public static void main(String[] args) { launch(args); }
}
