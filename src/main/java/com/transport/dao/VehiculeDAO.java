package com.transport.dao;

import com.transport.models.Vehicule;
import com.transport.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Vehicule> listerTous() {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicules ORDER BY marque, modele";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur liste : " + e.getMessage());
        }
        return liste;
    }

    public List<Vehicule> listerDisponibles() {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicules WHERE etat='DISPONIBLE' ORDER BY type_bus DESC, marque";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur disponibles : " + e.getMessage());
        }
        return liste;
    }

    public boolean ajouter(Vehicule v) {
        String sql = "INSERT INTO vehicules (immatriculation, marque, modele, capacite, annee_fabrication, etat, kilometrage, type_bus, prix_classique, prix_vip) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getImmatriculation());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getModele());
            ps.setInt(4, v.getCapacite());
            ps.setInt(5, v.getAnneeFabrication());
            ps.setString(6, v.getEtat() != null ? v.getEtat() : "DISPONIBLE");
            ps.setInt(7, v.getKilometrage());
            ps.setString(8, v.getTypeBus() != null ? v.getTypeBus() : "CLASSIQUE");
            ps.setDouble(9, v.getPrixClassique());
            ps.setDouble(10, v.getPrixVip());
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) v.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur ajout : " + e.getMessage());
        }
        return false;
    }

    public boolean modifier(Vehicule v) {
        String sql = "UPDATE vehicules SET immatriculation=?, marque=?, modele=?, capacite=?, annee_fabrication=?, etat=?, kilometrage=?, type_bus=?, prix_classique=?, prix_vip=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, v.getImmatriculation());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getModele());
            ps.setInt(4, v.getCapacite());
            ps.setInt(5, v.getAnneeFabrication());
            ps.setString(6, v.getEtat());
            ps.setInt(7, v.getKilometrage());
            ps.setString(8, v.getTypeBus());
            ps.setDouble(9, v.getPrixClassique());
            ps.setDouble(10, v.getPrixVip());
            ps.setInt(11, v.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur modification : " + e.getMessage());
        }
        return false;
    }

    public boolean changerEtat(int id, String etat) {
        String sql = "UPDATE vehicules SET etat=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, etat);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur état : " + e.getMessage());
        }
        return false;
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM vehicules WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur suppression : " + e.getMessage());
        }
        return false;
    }

    public int compterParEtat(String etat) {
        String sql = "SELECT COUNT(*) FROM vehicules WHERE etat=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, etat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[VehiculeDAO] Erreur comptage : " + e.getMessage());
        }
        return 0;
    }

    private Vehicule mapper(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule();
        v.setId(rs.getInt("id"));
        v.setImmatriculation(rs.getString("immatriculation"));
        v.setMarque(rs.getString("marque"));
        v.setModele(rs.getString("modele"));
        v.setCapacite(rs.getInt("capacite"));
        v.setAnneeFabrication(rs.getInt("annee_fabrication"));
        v.setEtat(rs.getString("etat"));
        v.setKilometrage(rs.getInt("kilometrage"));
        // type_bus peut ne pas exister si ancienne base
        try { v.setTypeBus(rs.getString("type_bus")); } catch (SQLException e) { v.setTypeBus("CLASSIQUE"); }
        try { v.setPrixClassique(rs.getDouble("prix_classique")); } catch (SQLException e) { v.setPrixClassique(0); }
        try { v.setPrixVip(rs.getDouble("prix_vip")); } catch (SQLException e) { v.setPrixVip(0); }
        Date ddm = rs.getDate("date_derniere_maintenance");
        if (ddm != null) v.setDateDerniereMaintenance(ddm.toLocalDate());
        Date pm = rs.getDate("prochaine_maintenance");
        if (pm != null) v.setProchaineMaintenance(pm.toLocalDate());
        Timestamp da = rs.getTimestamp("date_ajout");
        if (da != null) v.setDateAjout(da.toLocalDateTime());
        return v;
    }
}
