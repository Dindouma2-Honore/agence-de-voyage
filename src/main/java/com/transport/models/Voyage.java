package com.transport.models;

import java.time.LocalDateTime;

public class Voyage {
    private int id;
    private int destinationId;
    private int vehiculeId;
    private int chauffeurId;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArriveePrevue;
    private String statut;
    private int placesDisponibles;
    private double prixParPlace;   // UN SEUL prix (VIP ou Classique selon typeBus)
    private double prixClassique;  // gardé pour DAO mais = prixParPlace si classique
    private double prixVip;        // gardé pour DAO mais = prixParPlace si VIP
    private int placesClassique;
    private int placesVip;
    private String typeBus;        // VIP ou CLASSIQUE
    private String notes;
    private LocalDateTime dateCreation;

    // Champs joints
    private String villeDepart;
    private String villeArrivee;
    private String vehiculeLabel;
    private String chauffeurNom;

    public Voyage() {}
    public String getTrajet() { return villeDepart + " → " + villeArrivee; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int n) { this.destinationId = n; }
    public int getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(int n) { this.vehiculeId = n; }
    public int getChauffeurId() { return chauffeurId; }
    public void setChauffeurId(int n) { this.chauffeurId = n; }
    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime d) { this.dateDepart = d; }
    public LocalDateTime getDateArriveePrevue() { return dateArriveePrevue; }
    public void setDateArriveePrevue(LocalDateTime d) { this.dateArriveePrevue = d; }
    public String getStatut() { return statut; }
    public void setStatut(String s) { this.statut = s; }
    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int n) { this.placesDisponibles = n; }
    public double getPrixParPlace() { return prixParPlace; }
    public void setPrixParPlace(double d) { this.prixParPlace = d; }
    public double getPrixClassique() { return prixClassique; }
    public void setPrixClassique(double d) { this.prixClassique = d; }
    public double getPrixVip() { return prixVip; }
    public void setPrixVip(double d) { this.prixVip = d; }
    public int getPlacesClassique() { return placesClassique; }
    public void setPlacesClassique(int n) { this.placesClassique = n; }
    public int getPlacesVip() { return placesVip; }
    public void setPlacesVip(int n) { this.placesVip = n; }
    public String getTypeBus() { return typeBus; }
    public void setTypeBus(String s) { this.typeBus = s; }
    public String getNotes() { return notes; }
    public void setNotes(String s) { this.notes = s; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime d) { this.dateCreation = d; }
    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String s) { this.villeDepart = s; }
    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String s) { this.villeArrivee = s; }
    public String getVehiculeLabel() { return vehiculeLabel; }
    public void setVehiculeLabel(String s) { this.vehiculeLabel = s; }
    public String getChauffeurNom() { return chauffeurNom; }
    public void setChauffeurNom(String s) { this.chauffeurNom = s; }
}
