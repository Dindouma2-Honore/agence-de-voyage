package com.transport.models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int voyageId;
    private int clientId;
    private String numeroTicket;
    private int nombrePlaces;
    private String numeroSiege;
    private double montantTotal;
    private String statut;
    private String typPlace; // VIP ou CLASSIQUE
    private LocalDateTime dateReservation;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;

    // Champs joints
    private String clientNom;
    private String trajet;
    private LocalDateTime dateDepart;
    private String statutPaiement;

    public Reservation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVoyageId() { return voyageId; }
    public void setVoyageId(int n) { this.voyageId = n; }
    public int getClientId() { return clientId; }
    public void setClientId(int n) { this.clientId = n; }
    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String s) { this.numeroTicket = s; }
    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int n) { this.nombrePlaces = n; }
    public String getNumeroSiege() { return numeroSiege; }
    public void setNumeroSiege(String s) { this.numeroSiege = s; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double d) { this.montantTotal = d; }
    public String getStatut() { return statut; }
    public void setStatut(String s) { this.statut = s; }
    public String getTypPlace() { return typPlace; }
    public void setTypPlace(String s) { this.typPlace = s; }
    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime d) { this.dateReservation = d; }
    public LocalDateTime getDateAnnulation() { return dateAnnulation; }
    public void setDateAnnulation(LocalDateTime d) { this.dateAnnulation = d; }
    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String s) { this.motifAnnulation = s; }
    public String getClientNom() { return clientNom; }
    public void setClientNom(String s) { this.clientNom = s; }
    public String getTrajet() { return trajet; }
    public void setTrajet(String s) { this.trajet = s; }
    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime d) { this.dateDepart = d; }
    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String s) { this.statutPaiement = s; }
}
