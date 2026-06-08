# 🚌 Agence de Transport — Système de Gestion JavaFX

Application de gestion complète pour agence de transport (Cameroun).  
Architecture **MVC** · **JavaFX 21** · **MySQL** · **Maven**

---

## 📋 Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Java JDK | 17+ |
| JavaFX SDK | 21.0.1 |
| MySQL | 8.0+ |
| Maven | 3.8+ |
| IntelliJ IDEA / NetBeans | Recommandé |

---

## 🗄️ Installation de la base de données

1. Ouvrez **MySQL Workbench** ou votre client MySQL
2. Exécutez le fichier `database.sql` :
   ```sql
   SOURCE /chemin/vers/database.sql;
   ```
   Ou copiez-collez le contenu dans votre client MySQL.

3. Vérifiez que la base `transport_db` a bien été créée avec toutes ses tables.

---

## ⚙️ Configuration de la connexion MySQL

Ouvrez le fichier :
```
src/main/java/com/transport/utils/DatabaseConnection.java
```

Modifiez les paramètres si nécessaire :
```java
private static final String URL      = "jdbc:mysql://localhost:3306/transport_db?...";
private static final String USER     = "root";         // votre utilisateur MySQL
private static final String PASSWORD = "";             // votre mot de passe MySQL
```

---

## 🚀 Lancer l'application

### Avec Maven (ligne de commande)
```bash
cd transport-app
mvn clean javafx:run
```

### Avec IntelliJ IDEA
1. Ouvrir le projet (`File > Open > dossier transport-app`)
2. Attendre que Maven télécharge les dépendances
3. Aller dans `src/main/java/com/transport/MainApp.java`
4. Clic droit > **Run 'MainApp'**

### Avec NetBeans
1. `File > Open Project > dossier transport-app`
2. Clic droit sur le projet > **Run**

---

## 🔑 Comptes de démonstration

| Email | Mot de passe | Rôle |
|-------|-------------|------|
| admin@transport.cm | Admin@123 | Administrateur |
| agent@transport.cm | Admin@123 | Agent |
| chauffeur@transport.cm | Admin@123 | Chauffeur |
| client@transport.cm | Admin@123 | Client |

---

## 📁 Structure du projet

```
transport-app/
├── pom.xml                          # Dépendances Maven
├── database.sql                     # Script SQL complet
├── README.md
└── src/main/java/com/transport/
    ├── MainApp.java                 # Point d'entrée JavaFX
    ├── models/                      # Entités (Utilisateur, Vehicule, Voyage...)
    ├── dao/                         # Accès base de données (PDO-like)
    ├── controllers/                 # Contrôleurs JavaFX (MVC)
    └── utils/                       # DatabaseConnection, SessionManager
```

---

## 🎯 Modules développés

| Module | Fonctionnalités |
|--------|----------------|
| **Authentification** | Login sécurisé BCrypt, gestion de session, rôles |
| **Tableau de bord** | Statistiques temps réel, KPIs, résumés |
| **Utilisateurs** | CRUD complet, gestion rôles (Admin/Agent/Chauffeur/Client) |
| **Véhicules** | CRUD, changement d'état, suivi kilométrage |
| **Destinations** | Gestion des trajets et tarifs |
| **Voyages** | Planification, affectation chauffeur/véhicule, gestion statuts |
| **Réservations** | Réservation de sièges, génération ticket, annulation |
| **Paiements** | Multi-modes (Espèces, Mobile Money, Carte, Virement) |
| **Statistiques** | Revenus, top destinations, tableaux de bord |

---

## 🏗️ Architecture

```
Vue (JavaFX)  ←→  Contrôleur  ←→  DAO  ←→  MySQL
```

- **Models** : POJOs représentant les entités métier
- **DAO** : Requêtes SQL avec PreparedStatement (protection injection SQL)
- **Controllers** : Logique d'affichage et interactions utilisateur
- **Utils** : Singleton de connexion DB + gestion session

---

## 🔒 Sécurité

- Mots de passe hashés avec **BCrypt** (coût 12)
- **PreparedStatements** sur toutes les requêtes SQL
- Gestion des rôles et permissions par module
- Session centralisée avec `SessionManager`

---

## 📚 Répartition des tâches (Projet académique)

| Développeur | Module |
|------------|--------|
| Dev 1 | Authentification, Utilisateurs, Sécurité |
| Dev 2 | Voyages, Réservations, Billets |
| Dev 3 | Véhicules, Chauffeurs, Maintenance |
| Dev 4 | Paiements, Statistiques, Rapports |

---

*Projet réalisé dans le cadre d'une Licence Professionnelle — Université de Yaoundé I*
