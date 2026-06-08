package com.transport.models;

import java.time.LocalDateTime;

public class Colis {
    private int id;
    private String numeroSuivi;
    private int expediteurId;
    private String destinataireNom;
    private String destinataireTel;
    private int destinationId;
    private int voyageId;
    private String description;
    private double poidsKg;
    private double tarif;
    private String statut; // EN_ATTENTE, EN_TRANSIT, LIVRE, RETOURNE, PERDU
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLivraison;
    private String notes;

    // Champs joints
    private String expediteurNom;
    private String trajet;
    private String voyageDate;

    public Colis() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String s) { this.numeroSuivi = s; }
    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int n) { this.expediteurId = n; }
    public String getDestinataiреNom() { return destinataireNom; }
    public void setDestinataireNom(String s) { this.destinataireNom = s; }
    public String getDestinataireNom() { return destinataireNom; }
    public String getDestinataireTel() { return destinataireTel; }
    public void setDestinataireTel(String s) { this.destinataireTel = s; }
    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int n) { this.destinationId = n; }
    public int getVoyageId() { return voyageId; }
    public void setVoyageId(int n) { this.voyageId = n; }
    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }
    public double getPoidsKg() { return poidsKg; }
    public void setPoidsKg(double d) { this.poidsKg = d; }
    public double getTarif() { return tarif; }
    public void setTarif(double d) { this.tarif = d; }
    public String getStatut() { return statut; }
    public void setStatut(String s) { this.statut = s; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime d) { this.dateEnvoi = d; }
    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime d) { this.dateLivraison = d; }
    public String getNotes() { return notes; }
    public void setNotes(String s) { this.notes = s; }
    public String getExpediteurNom() { return expediteurNom; }
    public void setExpediteurNom(String s) { this.expediteurNom = s; }
    public String getTrajet() { return trajet; }
    public void setTrajet(String s) { this.trajet = s; }
    public String getVoyageDate() { return voyageDate; }
    public void setVoyageDate(String s) { this.voyageDate = s; }
}
