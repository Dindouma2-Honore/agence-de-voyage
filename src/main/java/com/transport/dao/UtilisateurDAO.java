package com.transport.dao;

import com.transport.models.Utilisateur;
import com.transport.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Utilisateur authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ? AND actif = TRUE";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("mot_de_passe");
                int uid = rs.getInt("id");
                boolean ok = false;

                // Vérifier si c'est un hash BCrypt (commence par $2a$ ou $2b$)
                if (hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$"))) {
                    // Mot de passe hashé → vérification BCrypt
                    try {
                        ok = BCrypt.checkpw(motDePasse, hash);
                    } catch (Exception ex) {
                        System.err.println("[AUTH] Erreur BCrypt : " + ex.getMessage());
                        ok = false;
                    }
                } else {
                    // Mot de passe en clair → comparaison directe
                    ok = motDePasse.equals(hash);
                    if (ok) {
                        // Re-hasher automatiquement pour sécuriser
                        String newHash = BCrypt.hashpw(motDePasse, BCrypt.gensalt(10));
                        mettreAJourHash(uid, newHash);
                        System.out.println("[AUTH] Mot de passe re-hashé en BCrypt.");
                    }
                }

                if (ok) {
                    Utilisateur u = mapper(rs);
                    mettreAJourDerniereConnexion(uid);
                    enregistrerConnexion(uid, true);
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur auth : " + e.getMessage());
        }
        return null;
    }

    public List<Utilisateur> listerTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom, prenom";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur liste : " + e.getMessage());
        }
        return liste;
    }

    public List<Utilisateur> listerParRole(String role) {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE role = ? ORDER BY nom, prenom";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur listerParRole : " + e.getMessage());
        }
        return liste;
    }

    public boolean inscrire(Utilisateur u) {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, telephone, mot_de_passe, role) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail().trim().toLowerCase());
            ps.setString(4, u.getTelephone());
            ps.setString(5, BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt(10)));
            ps.setString(6, u.getRole() != null ? u.getRole() : "CLIENT");
            int r = ps.executeUpdate();
            if (r > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) u.setId(gk.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur inscription : " + e.getMessage());
        }
        return false;
    }

    public boolean modifier(Utilisateur u) {
        String sql = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, telephone=?, role=?, actif=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail().trim().toLowerCase());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getRole());
            ps.setBoolean(6, u.isActif());
            ps.setInt(7, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur modification : " + e.getMessage());
        }
        return false;
    }

    public boolean changerMotDePasse(int userId, String nouveauMotDePasse) {
        String newHash = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt(10));
        return mettreAJourHash(userId, newHash);
    }

    private boolean mettreAJourHash(int userId, String hash) {
        String sql = "UPDATE utilisateurs SET mot_de_passe=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur MAJ hash : " + e.getMessage());
        }
        return false;
    }

    public boolean supprimer(int id) {
        String sql = "UPDATE utilisateurs SET actif=FALSE WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur suppression : " + e.getMessage());
        }
        return false;
    }

    public int compterParRole(String role) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE role=? AND actif=TRUE";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur comptage : " + e.getMessage());
        }
        return 0;
    }

    private void mettreAJourDerniereConnexion(int id) {
        String sql = "UPDATE utilisateurs SET derniere_connexion=NOW() WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] MAJ connexion : " + e.getMessage());
        }
    }

    private void enregistrerConnexion(int userId, boolean succes) {
        String sql = "INSERT INTO historique_connexions (utilisateur_id, succes) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBoolean(2, succes);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Historique connexion : " + e.getMessage());
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setRole(rs.getString("role"));
        u.setActif(rs.getBoolean("actif"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) u.setDateCreation(dc.toLocalDateTime());
        Timestamp dlc = rs.getTimestamp("derniere_connexion");
        if (dlc != null) u.setDerniereConnexion(dlc.toLocalDateTime());
        return u;
    }
}
