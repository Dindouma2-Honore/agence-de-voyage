-- ============================================================
--  AGENCE DE TRANSPORT - Script SQL Complet
--  Base de données : transport_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS transport_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE transport_db;

-- ─── UTILISATEURS ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS utilisateurs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    telephone   VARCHAR(20),
    mot_de_passe VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN','AGENT','CHAUFFEUR','CLIENT') DEFAULT 'CLIENT',
    actif       BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion DATETIME
);

CREATE TABLE IF NOT EXISTS historique_connexions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    date_connexion DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_adresse  VARCHAR(45),
    succes      BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ─── VÉHICULES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vehicules (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    marque      VARCHAR(100) NOT NULL,
    modele      VARCHAR(100) NOT NULL,
    capacite    INT NOT NULL DEFAULT 30,
    annee_fabrication INT,
    etat        ENUM('DISPONIBLE','EN_SERVICE','EN_MAINTENANCE','HORS_SERVICE') DEFAULT 'DISPONIBLE',
    date_derniere_maintenance DATE,
    prochaine_maintenance DATE,
    kilometrage  INT DEFAULT 0,
    date_ajout  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS maintenances (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    vehicule_id INT NOT NULL,
    type_maintenance VARCHAR(100),
    description TEXT,
    cout        DECIMAL(10,2),
    date_maintenance DATE NOT NULL,
    technicien  VARCHAR(150),
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE CASCADE
);

-- ─── CHAUFFEURS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chauffeurs (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL UNIQUE,
    numero_permis  VARCHAR(50) NOT NULL UNIQUE,
    categorie_permis VARCHAR(10) DEFAULT 'B',
    date_expiration_permis DATE,
    disponible     BOOLEAN DEFAULT TRUE,
    vehicule_actuel_id INT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicule_actuel_id) REFERENCES vehicules(id) ON DELETE SET NULL
);

-- ─── DESTINATIONS / TRAJETS ───────────────────────────────────
CREATE TABLE IF NOT EXISTS destinations (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    ville_depart VARCHAR(100) NOT NULL,
    ville_arrivee VARCHAR(100) NOT NULL,
    distance_km  DECIMAL(8,2),
    duree_estimee_min INT,
    tarif_base   DECIMAL(10,2) NOT NULL,
    actif        BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS voyages (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    destination_id  INT NOT NULL,
    vehicule_id     INT NOT NULL,
    chauffeur_id    INT NOT NULL,
    date_depart     DATETIME NOT NULL,
    date_arrivee_prevue DATETIME,
    statut          ENUM('PLANIFIE','EN_COURS','TERMINE','ANNULE') DEFAULT 'PLANIFIE',
    places_disponibles INT NOT NULL,
    prix_par_place  DECIMAL(10,2) NOT NULL,
    notes           TEXT,
    date_creation   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(id),
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id),
    FOREIGN KEY (chauffeur_id) REFERENCES chauffeurs(id)
);

-- ─── RÉSERVATIONS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    voyage_id       INT NOT NULL,
    client_id       INT NOT NULL,
    numero_ticket   VARCHAR(20) NOT NULL UNIQUE,
    nombre_places   INT NOT NULL DEFAULT 1,
    numero_siege    VARCHAR(20),
    montant_total   DECIMAL(10,2) NOT NULL,
    statut          ENUM('EN_ATTENTE','CONFIRMEE','ANNULEE','UTILISEE') DEFAULT 'EN_ATTENTE',
    date_reservation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_annulation DATETIME,
    motif_annulation TEXT,
    FOREIGN KEY (voyage_id) REFERENCES voyages(id),
    FOREIGN KEY (client_id) REFERENCES utilisateurs(id)
);

-- ─── PAIEMENTS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS paiements (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id  INT NOT NULL UNIQUE,
    montant         DECIMAL(10,2) NOT NULL,
    mode_paiement   ENUM('ESPECES','MOBILE_MONEY','CARTE','VIREMENT') DEFAULT 'ESPECES',
    statut          ENUM('EN_ATTENTE','PAYE','REMBOURSE','ECHOUE') DEFAULT 'EN_ATTENTE',
    reference       VARCHAR(100),
    date_paiement   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- ─── DONNÉES DE DÉMONSTRATION ─────────────────────────────────
-- Mot de passe : Admin@123 (en clair — sera auto-hashé BCrypt à la 1ère connexion)
INSERT IGNORE INTO utilisateurs (nom, prenom, email, telephone, mot_de_passe, role) VALUES
('Admin', 'Système',  'admin@transport.cm',     '+237670000001', 'Admin@123', 'ADMIN'),
('Dupont', 'Marie',   'agent@transport.cm',     '+237670000002', 'Admin@123', 'AGENT'),
('Mbarga', 'Paul',    'chauffeur@transport.cm', '+237670000003', 'Admin@123', 'CHAUFFEUR'),
('Nguema', 'Sophie',  'client@transport.cm',    '+237670000004', 'Admin@123', 'CLIENT');

INSERT IGNORE INTO vehicules (immatriculation, marque, modele, capacite, annee_fabrication, etat, kilometrage) VALUES
('LT-1234-CM', 'Mercedes', 'Sprinter', 30, 2020, 'DISPONIBLE', 45000),
('LT-5678-CM', 'Toyota', 'Coaster', 25, 2019, 'DISPONIBLE', 78000),
('LT-9012-CM', 'Hyundai', 'H350', 20, 2021, 'EN_MAINTENANCE', 12000),
('LT-3456-CM', 'Renault', 'Master', 15, 2018, 'DISPONIBLE', 120000);

INSERT IGNORE INTO chauffeurs (utilisateur_id, numero_permis, categorie_permis, date_expiration_permis, disponible) VALUES
(3, 'CMR-DL-20231045', 'D', '2026-12-31', TRUE);

INSERT IGNORE INTO destinations (ville_depart, ville_arrivee, distance_km, duree_estimee_min, tarif_base) VALUES
('Yaoundé', 'Douala', 305.5, 210, 4500),
('Yaoundé', 'Bafoussam', 320.0, 240, 5000),
('Douala', 'Kribi', 172.0, 150, 3500),
('Yaoundé', 'Bertoua', 355.0, 300, 6000),
('Ngaoundéré', 'Garoua', 253.0, 200, 4000),
('Douala', 'Limbé', 70.0, 90, 2000);

-- ─── MISE À JOUR : Type de bus sur les véhicules ──────────────
ALTER TABLE vehicules ADD COLUMN IF NOT EXISTS type_bus ENUM('CLASSIQUE','VIP') DEFAULT 'CLASSIQUE';
ALTER TABLE vehicules ADD COLUMN IF NOT EXISTS prix_classique DECIMAL(10,2) DEFAULT 0;
ALTER TABLE vehicules ADD COLUMN IF NOT EXISTS prix_vip DECIMAL(10,2) DEFAULT 0;

-- Mettre à jour les véhicules existants
UPDATE vehicules SET type_bus='CLASSIQUE', prix_classique=0, prix_vip=0;

-- ─── MISE À JOUR : Type de place sur réservations ─────────────
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS type_place ENUM('CLASSIQUE','VIP') DEFAULT 'CLASSIQUE';

-- ─── MISE À JOUR : Type de place sur voyages ──────────────────
ALTER TABLE voyages ADD COLUMN IF NOT EXISTS prix_classique DECIMAL(10,2) DEFAULT 0;
ALTER TABLE voyages ADD COLUMN IF NOT EXISTS prix_vip DECIMAL(10,2) DEFAULT 0;
ALTER TABLE voyages ADD COLUMN IF NOT EXISTS places_vip INT DEFAULT 0;
ALTER TABLE voyages ADD COLUMN IF NOT EXISTS places_classique INT DEFAULT 0;

-- Re-créer les données de démonstration véhicules avec types
UPDATE vehicules SET type_bus='VIP',      prix_vip=8000,  prix_classique=4500 WHERE immatriculation='LT-1234-CM';
UPDATE vehicules SET type_bus='CLASSIQUE',prix_vip=0,     prix_classique=5000 WHERE immatriculation='LT-5678-CM';
UPDATE vehicules SET type_bus='VIP',      prix_vip=7500,  prix_classique=4000 WHERE immatriculation='LT-9012-CM';
UPDATE vehicules SET type_bus='CLASSIQUE',prix_vip=0,     prix_classique=3500 WHERE immatriculation='LT-3456-CM';

-- ─── COLIS ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS colis (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    numero_suivi    VARCHAR(30) NOT NULL UNIQUE,
    expediteur_id   INT NOT NULL,
    destinataire_nom VARCHAR(150) NOT NULL,
    destinataire_tel VARCHAR(20) NOT NULL,
    destination_id  INT NOT NULL,
    voyage_id       INT,
    description     TEXT,
    poids_kg        DECIMAL(8,2) NOT NULL DEFAULT 1.0,
    tarif           DECIMAL(10,2) NOT NULL,
    statut          ENUM('EN_ATTENTE','EN_TRANSIT','LIVRE','RETOURNE','PERDU') DEFAULT 'EN_ATTENTE',
    date_envoi      DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_livraison  DATETIME,
    notes           TEXT,
    FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (destination_id) REFERENCES destinations(id),
    FOREIGN KEY (voyage_id) REFERENCES voyages(id) ON DELETE SET NULL
);

-- ─── TARIFS COLIS PAR DESTINATION ────────────────────────────
CREATE TABLE IF NOT EXISTS tarifs_colis (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    destination_id  INT NOT NULL UNIQUE,
    prix_par_kg     DECIMAL(10,2) NOT NULL DEFAULT 500,
    prix_minimum    DECIMAL(10,2) NOT NULL DEFAULT 1000,
    FOREIGN KEY (destination_id) REFERENCES destinations(id)
);

-- Insérer tarifs colis pour les destinations existantes
INSERT IGNORE INTO tarifs_colis (destination_id, prix_par_kg, prix_minimum)
SELECT id, 800, 1500 FROM destinations;
