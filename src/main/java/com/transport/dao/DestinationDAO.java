package com.transport.dao;

import com.transport.models.Destination;
import com.transport.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Destination> listerToutes() {
        List<Destination> liste = new ArrayList<>();
        String sql = "SELECT * FROM destinations WHERE actif=TRUE ORDER BY ville_depart, ville_arrivee";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Erreur liste : " + e.getMessage());
        }
        return liste;
    }

    public boolean ajouter(Destination d) {
        String sql = "INSERT INTO destinations (ville_depart, ville_arrivee, distance_km, duree_estimee_min, tarif_base) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getVilleDepart());
            ps.setString(2, d.getVilleArrivee());
            ps.setDouble(3, d.getDistanceKm());
            ps.setInt(4, d.getDureeEstimeeMin());
            ps.setDouble(5, d.getTarifBase());
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) d.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Erreur ajout : " + e.getMessage());
        }
        return false;
    }

    private Destination mapper(ResultSet rs) throws SQLException {
        Destination d = new Destination();
        d.setId(rs.getInt("id"));
        d.setVilleDepart(rs.getString("ville_depart"));
        d.setVilleArrivee(rs.getString("ville_arrivee"));
        d.setDistanceKm(rs.getDouble("distance_km"));
        d.setDureeEstimeeMin(rs.getInt("duree_estimee_min"));
        d.setTarifBase(rs.getDouble("tarif_base"));
        d.setActif(rs.getBoolean("actif"));
        return d;
    }
}
