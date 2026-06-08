package com.transport.models;

import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motDePasse;
    private String role;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email, String telephone, String role, boolean actif) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.telephone = telephone;
        this.role = role; this.actif = actif;
    }

    public String getNomComplet() { return prenom + " " + nom; }

    // ─── Getters & Setters ─────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime d) { this.dateCreation = d; }
    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime d) { this.derniereConnexion = d; }

    @Override
    public String toString() { return getNomComplet() + " [" + role + "]"; }
}
