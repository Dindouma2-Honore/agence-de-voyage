package com.transport.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Vehicule {
    private int id;
    private String immatriculation;
    private String marque;
    private String modele;
    private int capacite;
    private int anneeFabrication;
    private String etat;
    private String typeBus; // CLASSIQUE ou VIP
    private double prixClassique;
    private double prixVip;
    private LocalDate dateDerniereMaintenance;
    private LocalDate prochaineMaintenance;
    private int kilometrage;
    private LocalDateTime dateAjout;

    public Vehicule() {}

    public String getLabel() {
        String type = "VIP".equals(typeBus) ? " [VIP]" : " [Classique]";
        return immatriculation + " - " + marque + " " + modele + type;
    }

    public boolean isVip() { return "VIP".equals(typeBus); }

    // ─── Getters & Setters ─────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String s) { this.immatriculation = s; }
    public String getMarque() { return marque; }
    public void setMarque(String s) { this.marque = s; }
    public String getModele() { return modele; }
    public void setModele(String s) { this.modele = s; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int n) { this.capacite = n; }
    public int getAnneeFabrication() { return anneeFabrication; }
    public void setAnneeFabrication(int n) { this.anneeFabrication = n; }
    public String getEtat() { return etat; }
    public void setEtat(String s) { this.etat = s; }
    public String getTypeBus() { return typeBus; }
    public void setTypeBus(String s) { this.typeBus = s; }
    public double getPrixClassique() { return prixClassique; }
    public void setPrixClassique(double d) { this.prixClassique = d; }
    public double getPrixVip() { return prixVip; }
    public void setPrixVip(double d) { this.prixVip = d; }
    public LocalDate getDateDerniereMaintenance() { return dateDerniereMaintenance; }
    public void setDateDerniereMaintenance(LocalDate d) { this.dateDerniereMaintenance = d; }
    public LocalDate getProchaineMaintenance() { return prochaineMaintenance; }
    public void setProchaineMaintenance(LocalDate d) { this.prochaineMaintenance = d; }
    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int n) { this.kilometrage = n; }
    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime d) { this.dateAjout = d; }
}
