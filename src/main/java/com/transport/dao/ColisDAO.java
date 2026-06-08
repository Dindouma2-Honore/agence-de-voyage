package com.transport.dao;

import com.transport.models.Colis;
import com.transport.utils.DatabaseConnection;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColisDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static final String SELECT_JOINT =
        "SELECT c.*, " +
        "       CONCAT(u.prenom,' ',u.nom) AS expediteur_nom, " +
        "       CONCAT(d.ville_depart,' → ',d.ville_arrivee) AS trajet, " +
        "       DATE_FORMAT(v.date_depart,'%d/%m/%Y %H:%i') AS voyage_date " +
        "FROM colis c " +
        "JOIN utilisateurs u ON c.expediteur_id = u.id " +
        "JOIN destinations d ON c.destination_id = d.id " +
        "LEFT JOIN voyages v ON c.voyage_id = v.id ";

    public List<Colis> listerTous() {
        List<Colis> liste = new ArrayList<>();
        String sql = SELECT_JOINT + "ORDER BY c.date_envoi DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[ColisDAO] listerTous : " + e.getMessage());
        }
        return liste;
    }

    public List<Colis> listerParExpediteur(int expediteurId) {
        List<Colis> liste = new ArrayList<>();
        String sql = SELECT_JOINT + "WHERE c.expediteur_id=? ORDER BY c.date_envoi DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, expediteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[ColisDAO] listerParExpediteur : " + e.getMessage());
        }
        return liste;
    }

    public Colis rechercherParNumero(String numero) {
        String sql = SELECT_JOINT + "WHERE c.numero_suivi=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, numero.trim().toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper(rs);
        } catch (SQLException e) {
            System.err.println("[ColisDAO] rechercherParNumero : " + e.getMessage());
        }
        return null;
    }

    public boolean enregistrer(Colis c) {
        String numero = "COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        c.setNumeroSuivi(numero);
        String sql = "INSERT INTO colis (numero_suivi, expediteur_id, destinataire_nom, destinataire_tel, " +
            "destination_id, voyage_id, description, poids_kg, tarif, statut, notes) " +
            "VALUES (?,?,?,?,?,?,?,?,?,'EN_ATTENTE',?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, numero);
            ps.setInt(2, c.getExpediteurId());
            ps.setString(3, c.getDestinataireNom());
            ps.setString(4, c.getDestinataireTel());
            ps.setInt(5, c.getDestinationId());
            if (c.getVoyageId() > 0) ps.setInt(6, c.getVoyageId());
            else ps.setNull(6, Types.INTEGER);
            ps.setString(7, c.getDescription());
            ps.setDouble(8, c.getPoidsKg());
            ps.setDouble(9, c.getTarif());
            ps.setString(10, c.getNotes());
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) c.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ColisDAO] enregistrer : " + e.getMessage());
        }
        return false;
    }

    public boolean changerStatut(int id, String statut) {
        String sql = "UPDATE colis SET statut=?" +
            (statut.equals("LIVRE") ? ", date_livraison=NOW()" : "") +
            " WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ColisDAO] changerStatut : " + e.getMessage());
        }
        return false;
    }

    public double calculerTarif(int destinationId, double poidsKg) {
        String sql = "SELECT prix_par_kg, prix_minimum FROM tarifs_colis WHERE destination_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("prix_par_kg") * poidsKg;
                double min   = rs.getDouble("prix_minimum");
                return Math.max(total, min);
            }
        } catch (SQLException e) {
            System.err.println("[ColisDAO] calculerTarif : " + e.getMessage());
        }
        // Tarif par défaut si pas de config
        return Math.max(800 * poidsKg, 1500);
    }

    public int compterParStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM colis WHERE statut=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ColisDAO] compterParStatut : " + e.getMessage());
        }
        return 0;
    }

    private Colis mapper(ResultSet rs) throws SQLException {
        Colis c = new Colis();
        c.setId(rs.getInt("id"));
        c.setNumeroSuivi(rs.getString("numero_suivi"));
        c.setExpediteurId(rs.getInt("expediteur_id"));
        c.setDestinataireNom(rs.getString("destinataire_nom"));
        c.setDestinataireTel(rs.getString("destinataire_tel"));
        c.setDestinationId(rs.getInt("destination_id"));
        c.setVoyageId(rs.getInt("voyage_id"));
        c.setDescription(rs.getString("description"));
        c.setPoidsKg(rs.getDouble("poids_kg"));
        c.setTarif(rs.getDouble("tarif"));
        c.setStatut(rs.getString("statut"));
        Timestamp de = rs.getTimestamp("date_envoi");
        if (de != null) c.setDateEnvoi(de.toLocalDateTime());
        Timestamp dl = rs.getTimestamp("date_livraison");
        if (dl != null) c.setDateLivraison(dl.toLocalDateTime());
        c.setNotes(rs.getString("notes"));
        c.setExpediteurNom(rs.getString("expediteur_nom"));
        c.setTrajet(rs.getString("trajet"));
        c.setVoyageDate(rs.getString("voyage_date"));
        return c;
    }
}
