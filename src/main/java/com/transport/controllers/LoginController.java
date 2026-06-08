package com.transport.controllers;

import com.transport.MainApp;
import com.transport.dao.UtilisateurDAO;
import com.transport.models.Utilisateur;
import com.transport.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginController {

    private final MainApp app;
    private final UtilisateurDAO dao = new UtilisateurDAO();
    private StackPane root;
    private TextField tfEmail;
    private PasswordField pfPassword;
    private Label lblErreur;

    public LoginController(MainApp app) {
        this.app = app;
        construireVue();
    }

    private void construireVue() {
        root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e3a5f, #1e40af);");

        // Carte centrale
        VBox carte = new VBox(20);
        carte.setMaxWidth(420);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 30, 0, 0, 10);");
        carte.setPadding(new Insets(44, 50, 44, 50));
        carte.setAlignment(Pos.CENTER);

        // Logo
        Label ico = new Label("🚌");
        ico.setStyle("-fx-font-size: 52px;");

        Label titre = new Label("SIGAVT");
        titre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");

        Label sous = new Label("Système de Gestion d'Agence de Voyage");
        sous.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Label pays = new Label("🇨🇲  Cameroun");
        pays.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Separator sep = new Separator();

        // Email
        Label lEmail = new Label("Adresse email");
        lEmail.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        tfEmail = new TextField();
        tfEmail.setPromptText("votre@email.com");
        tfEmail.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#cbd5e1;" +
            "-fx-padding:9 12;-fx-font-size:13px;-fx-background-color:white;");
        VBox gEmail = new VBox(5, lEmail, tfEmail);

        // MDP
        Label lMdp = new Label("Mot de passe");
        lMdp.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        pfPassword = new PasswordField();
        pfPassword.setPromptText("••••••••");
        pfPassword.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#cbd5e1;" +
            "-fx-padding:9 12;-fx-font-size:13px;-fx-background-color:white;");
        VBox gMdp = new VBox(5, lMdp, pfPassword);

        // Erreur
        lblErreur = new Label();
        lblErreur.setStyle("-fx-text-fill:#dc2626;-fx-font-size:12px;" +
            "-fx-background-color:#fee2e2;-fx-background-radius:6;-fx-padding:6 10;");
        lblErreur.setMaxWidth(Double.MAX_VALUE);
        lblErreur.setVisible(false);

        // Bouton
        Button btnLogin = new Button("SE CONNECTER");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:11;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color:#1d4ed8;-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:11;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:11;"));
        btnLogin.setOnAction(e -> seConnecter());
        pfPassword.setOnAction(e -> seConnecter());
        tfEmail.setOnAction(e -> pfPassword.requestFocus());

        // Info démo
        Label demo = new Label("Démo : admin@transport.cm  •  mot de passe en base");
        demo.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        // Footer
        Label footer = new Label("SIGAVT v1.0 — Université de Yaoundé I");
        footer.setStyle("-fx-font-size:10px;-fx-text-fill:#cbd5e1;");

        carte.getChildren().addAll(ico, titre, sous, pays, sep, gEmail, gMdp, lblErreur, btnLogin, demo);

        VBox outer = new VBox(16, carte, footer);
        outer.setAlignment(Pos.CENTER);
        StackPane.setAlignment(outer, Pos.CENTER);
        root.getChildren().add(outer);

        tfEmail.setText("admin@transport.cm");
    }

    private void seConnecter() {
        String email = tfEmail.getText().trim();
        String mdp   = pfPassword.getText();
        if (email.isEmpty() || mdp.isEmpty()) { afficherErreur("Remplissez tous les champs."); return; }
        Utilisateur u = dao.authentifier(email, mdp);
        if (u != null) {
            SessionManager.getInstance().setUtilisateurConnecte(u);
            lblErreur.setVisible(false);
            app.afficherApplicationPrincipale();
        } else {
            afficherErreur("Identifiant ou mot de passe incorrect.");
            pfPassword.clear();
        }
    }

    private void afficherErreur(String msg) {
        lblErreur.setText("⚠  " + msg);
        lblErreur.setVisible(true);
    }

    public StackPane getView() { return root; }
}
