package com.transport.models;

public class Destination {
    private int id;
    private String villeDepart;
    private String villeArrivee;
    private double distanceKm;
    private int dureeEstimeeMin;
    private double tarifBase;
    private boolean actif;

    public Destination() {}

    public String getTrajet() { return villeDepart + " → " + villeArrivee; }

    @Override
    public String toString() { return getTrajet(); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String s) { this.villeDepart = s; }
    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String s) { this.villeArrivee = s; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double d) { this.distanceKm = d; }
    public int getDureeEstimeeMin() { return dureeEstimeeMin; }
    public void setDureeEstimeeMin(int n) { this.dureeEstimeeMin = n; }
    public double getTarifBase() { return tarifBase; }
    public void setTarifBase(double d) { this.tarifBase = d; }
    public boolean isActif() { return actif; }
    public void setActif(boolean b) { this.actif = b; }
}
