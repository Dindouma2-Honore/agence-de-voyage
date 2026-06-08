package com.transport.dao;

import com.transport.models.Reservation;
import com.transport.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservationDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static final String SELECT_JOINT =
        "SELECT r.*, " +
        "       CONCAT(u.prenom,' ',u.nom) AS client_nom, " +
        "       CONCAT(d.ville_depart,' → ',d.ville_arrivee) AS trajet, " +
        "       v.date_depart, " +
        "       COALESCE(p.statut,'NON_PAYE') AS statut_paiement " +
        "FROM reservations r " +
        "JOIN utilisateurs u ON r.client_id = u.id " +
        "JOIN voyages v ON r.voyage_id = v.id " +
        "JOIN destinations d ON v.destination_id = d.id " +
        "LEFT JOIN paiements p ON p.reservation_id = r.id ";

    public List<Reservation> listerToutes() {
        List<Reservation> liste = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOINT + "ORDER BY r.date_reservation DESC")) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { System.err.println("[ReservationDAO] listerToutes : " + e.getMessage()); }
        return liste;
    }

    public List<Reservation> listerParClient(int clientId) {
        List<Reservation> liste = new ArrayList<>();
        String sql = SELECT_JOINT + "WHERE r.client_id=? ORDER BY r.date_reservation DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { System.err.println("[ReservationDAO] listerParClient : " + e.getMessage()); }
        return liste;
    }

    public boolean creer(Reservation r) {
        String ticket = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        r.setNumeroTicket(ticket);
        // Essai avec colonne type_place
        String sql = "INSERT INTO reservations " +
            "(voyage_id, client_id, numero_ticket, nombre_places, numero_siege, montant_total, statut, type_place) " +
            "VALUES (?,?,?,?,?,?,'CONFIRMEE',?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getVoyageId());
            ps.setInt(2, r.getClientId());
            ps.setString(3, ticket);
            ps.setInt(4, r.getNombrePlaces());
            ps.setString(5, r.getNumeroSiege());
            ps.setDouble(6, r.getMontantTotal());
            ps.setString(7, r.getTypPlace() != null ? r.getTypPlace() : "CLASSIQUE");
            int res = ps.executeUpdate();
            if (res > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) r.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] creer étendu : " + e.getMessage());
            return creerSimple(r);
        }
        return false;
    }

    private boolean creerSimple(Reservation r) {
        String sql = "INSERT INTO reservations " +
            "(voyage_id, client_id, numero_ticket, nombre_places, numero_siege, montant_total, statut) " +
            "VALUES (?,?,?,?,?,?,'CONFIRMEE')";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getVoyageId());
            ps.setInt(2, r.getClientId());
            ps.setString(3, r.getNumeroTicket());
            ps.setInt(4, r.getNombrePlaces());
            ps.setString(5, r.getNumeroSiege());
            ps.setDouble(6, r.getMontantTotal());
            int res = ps.executeUpdate();
            if (res > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) r.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) { System.err.println("[ReservationDAO] creerSimple : " + e.getMessage()); }
        return false;
    }

    public boolean annuler(int id, String motif) {
        String sql = "UPDATE reservations SET statut='ANNULEE', date_annulation=NOW(), motif_annulation=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, motif); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[ReservationDAO] annuler : " + e.getMessage()); }
        return false;
    }

    public boolean creerPaiement(int reservationId, double montant, String mode) {
        String sql = "INSERT INTO paiements (reservation_id, montant, mode_paiement, statut, reference) VALUES (?,?,?,'PAYE',?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setDouble(2, montant);
            ps.setString(3, mode);
            ps.setString(4, "REF-" + System.currentTimeMillis());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[ReservationDAO] creerPaiement : " + e.getMessage()); }
        return false;
    }

    public double totalRevenusJour() {
        String sql = "SELECT COALESCE(SUM(montant),0) FROM paiements WHERE statut='PAYE' AND DATE(date_paiement)=CURDATE()";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[ReservationDAO] totalRevenusJour : " + e.getMessage()); }
        return 0;
    }

    public double totalRevenusMois() {
        String sql = "SELECT COALESCE(SUM(montant),0) FROM paiements WHERE statut='PAYE' " +
            "AND MONTH(date_paiement)=MONTH(CURDATE()) AND YEAR(date_paiement)=YEAR(CURDATE())";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[ReservationDAO] totalRevenusMois : " + e.getMessage()); }
        return 0;
    }

    public int compterParStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE statut=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[ReservationDAO] compterParStatut : " + e.getMessage()); }
        return 0;
    }

    private Reservation mapper(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setVoyageId(rs.getInt("voyage_id"));
        r.setClientId(rs.getInt("client_id"));
        r.setNumeroTicket(rs.getString("numero_ticket"));
        r.setNombrePlaces(rs.getInt("nombre_places"));
        r.setNumeroSiege(rs.getString("numero_siege"));
        r.setMontantTotal(rs.getDouble("montant_total"));
        r.setStatut(rs.getString("statut"));
        try { r.setTypPlace(rs.getString("type_place")); } catch (SQLException e) { r.setTypPlace("CLASSIQUE"); }
        if (r.getTypPlace() == null) r.setTypPlace("CLASSIQUE");
        Timestamp dr = rs.getTimestamp("date_reservation");
        if (dr != null) r.setDateReservation(dr.toLocalDateTime());
        Timestamp da = rs.getTimestamp("date_annulation");
        if (da != null) r.setDateAnnulation(da.toLocalDateTime());
        r.setMotifAnnulation(rs.getString("motif_annulation"));
        r.setClientNom(rs.getString("client_nom"));
        r.setTrajet(rs.getString("trajet"));
        Timestamp dd = rs.getTimestamp("date_depart");
        if (dd != null) r.setDateDepart(dd.toLocalDateTime());
        r.setStatutPaiement(rs.getString("statut_paiement"));
        return r;
    }
}
