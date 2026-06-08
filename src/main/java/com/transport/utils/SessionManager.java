package com.transport.utils;

import com.transport.models.Utilisateur;

/**
 * Gestion de la session utilisateur courante (Singleton).
 */
public class SessionManager {

    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }

    public void setUtilisateurConnecte(Utilisateur u) { this.utilisateurConnecte = u; }

    public boolean estConnecte() { return utilisateurConnecte != null; }

    public boolean estAdmin() {
        return estConnecte() && "ADMIN".equals(utilisateurConnecte.getRole());
    }

    public boolean estAgent() {
        return estConnecte() && ("AGENT".equals(utilisateurConnecte.getRole()) || estAdmin());
    }

    public void deconnecter() { utilisateurConnecte = null; }
}
