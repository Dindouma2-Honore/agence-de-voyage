package com.transport.dao;

import com.transport.models.Voyage;
import com.transport.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static final String SELECT_JOINT =
        "SELECT v.*, d.ville_depart, d.ville_arrivee, " +
        "       vh.immatriculation, vh.marque, vh.modele, vh.type_bus, " +
        "       CONCAT(u.prenom,' ',u.nom) AS chauffeur_nom " +
        "FROM voyages v " +
        "JOIN destinations d ON v.destination_id = d.id " +
        "JOIN vehicules vh ON v.vehicule_id = vh.id " +
        "JOIN chauffeurs c ON v.chauffeur_id = c.id " +
        "JOIN utilisateurs u ON c.utilisateur_id = u.id ";

    public List<Voyage> listerTous() {
        List<Voyage> liste = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOINT + "ORDER BY v.date_depart DESC")) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { System.err.println("[VoyageDAO] listerTous : " + e.getMessage()); }
        return liste;
    }

    public List<Voyage> listerDisponibles() {
        List<Voyage> liste = new ArrayList<>();
        String sql = SELECT_JOINT +
            "WHERE v.statut='PLANIFIE' AND v.places_disponibles > 0 AND v.date_depart > NOW() " +
            "ORDER BY v.date_depart";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { System.err.println("[VoyageDAO] listerDisponibles : " + e.getMessage()); }
        return liste;
    }

    public int getChauffeurIdParUtilisateur(int utilisateurId) {
        String sql = "SELECT id FROM chauffeurs WHERE utilisateur_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { System.err.println("[VoyageDAO] getChauffeurId : " + e.getMessage()); }
        return -1;
    }

    public boolean ajouter(Voyage v) {
        // Essai avec colonnes étendues
        String sql = "INSERT INTO voyages " +
            "(destination_id, vehicule_id, chauffeur_id, date_depart, statut, " +
            " places_disponibles, prix_par_place, prix_classique, prix_vip, " +
            " places_classique, places_vip, notes) " +
            "VALUES (?,?,?,?,'PLANIFIE',?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getDestinationId());
            ps.setInt(2, v.getVehiculeId());
            ps.setInt(3, v.getChauffeurId());
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateDepart()));
            ps.setInt(5, v.getPlacesDisponibles());
            ps.setDouble(6, v.getPrixParPlace());
            ps.setDouble(7, v.getPrixClassique());
            ps.setDouble(8, v.getPrixVip());
            ps.setInt(9, v.getPlacesClassique());
            ps.setInt(10, v.getPlacesVip());
            ps.setString(11, v.getNotes());
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) v.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[VoyageDAO] ajouter étendu : " + e.getMessage());
            return ajouterSimple(v);
        }
        return false;
    }

    private boolean ajouterSimple(Voyage v) {
        String sql = "INSERT INTO voyages " +
            "(destination_id, vehicule_id, chauffeur_id, date_depart, statut, places_disponibles, prix_par_place, notes) " +
            "VALUES (?,?,?,?,'PLANIFIE',?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getDestinationId());
            ps.setInt(2, v.getVehiculeId());
            ps.setInt(3, v.getChauffeurId());
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateDepart()));
            ps.setInt(5, v.getPlacesDisponibles());
            ps.setDouble(6, v.getPrixParPlace());
            ps.setString(7, v.getNotes());
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) v.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) { System.err.println("[VoyageDAO] ajouterSimple : " + e.getMessage()); }
        return false;
    }

    public boolean changerStatut(int id, String statut) {
        String sql = "UPDATE voyages SET statut=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[VoyageDAO] changerStatut : " + e.getMessage()); }
        return false;
    }

    public boolean decrementerPlaces(int voyageId, int nb) {
        String sql = "UPDATE voyages SET places_disponibles = places_disponibles - ? WHERE id=? AND places_disponibles >= ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, nb); ps.setInt(2, voyageId); ps.setInt(3, nb);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[VoyageDAO] decrementerPlaces : " + e.getMessage()); }
        return false;
    }

    public boolean incrementerPlaces(int voyageId, int nb) {
        String sql = "UPDATE voyages SET places_disponibles = places_disponibles + ? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, nb); ps.setInt(2, voyageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[VoyageDAO] incrementerPlaces : " + e.getMessage()); }
        return false;
    }

    public int compterParStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM voyages WHERE statut=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[VoyageDAO] compterParStatut : " + e.getMessage()); }
        return 0;
    }

    private Voyage mapper(ResultSet rs) throws SQLException {
        Voyage v = new Voyage();
        v.setId(rs.getInt("id"));
        v.setDestinationId(rs.getInt("destination_id"));
        v.setVehiculeId(rs.getInt("vehicule_id"));
        v.setChauffeurId(rs.getInt("chauffeur_id"));
        Timestamp dd = rs.getTimestamp("date_depart");
        if (dd != null) v.setDateDepart(dd.toLocalDateTime());
        Timestamp da = rs.getTimestamp("date_arrivee_prevue");
        if (da != null) v.setDateArriveePrevue(da.toLocalDateTime());
        v.setStatut(rs.getString("statut"));
        v.setPlacesDisponibles(rs.getInt("places_disponibles"));
        v.setPrixParPlace(rs.getDouble("prix_par_place"));
        try { v.setPrixClassique(rs.getDouble("prix_classique")); } catch (SQLException e) {}
        try { v.setPrixVip(rs.getDouble("prix_vip")); } catch (SQLException e) {}
        try { v.setPlacesClassique(rs.getInt("places_classique")); } catch (SQLException e) {}
        try { v.setPlacesVip(rs.getInt("places_vip")); } catch (SQLException e) {}
        v.setNotes(rs.getString("notes"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) v.setDateCreation(dc.toLocalDateTime());
        v.setVilleDepart(rs.getString("ville_depart"));
        v.setVilleArrivee(rs.getString("ville_arrivee"));
        String typeBus = "CLASSIQUE";
        try { typeBus = rs.getString("type_bus"); if (typeBus == null) typeBus = "CLASSIQUE"; } catch (SQLException e) {}
        v.setTypeBus(typeBus);
        String badge = "VIP".equals(typeBus) ? " [⭐VIP]" : " [🚌Classique]";
        v.setVehiculeLabel(rs.getString("immatriculation") + " " + rs.getString("marque") +
            " " + rs.getString("modele") + badge);
        v.setChauffeurNom(rs.getString("chauffeur_nom"));
        return v;
    }
}
