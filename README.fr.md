![](src/main/resources/banner_polytech_dams.png)

# Opal - Projet de Génie Logiciel

<div align="center">

**🌍 Language / Langue**

[![🇺🇸 English](https://img.shields.io/badge/🇺🇸-English-lightgrey?style=for-the-badge)](README.md)
[![🇫🇷 Français](https://img.shields.io/badge/🇫🇷-Français-blue?style=for-the-badge)](README.fr.md)

---

</div>

# Opal - Projet de Génie Logiciel

Opal est une application basée sur Java conçue pour la prise de notes et l'organisation. Elle permet aux utilisateurs de créer, d'éditer et de gérer des notes de manière structurée. L' application est construite en utilisant JavaFX et suit un modèle d'architecture propre, séparant les préoccupations en couches distinctes.

## Cas d'Utilisation Principaux

Basé sur la conception, l'application gère principalement :

### 1. Authentification
- **Connexion** : Les utilisateurs peuvent s'authentifier à l'aide de leurs identifiants.
- **Inscription** : Les nouveaux utilisateurs peuvent créer un compte.
- **Déconnexion** : Fin sécurisée de la session utilisateur.
- **Gestion du Profil** : Les utilisateurs peuvent visualiser et mettre à jour leurs profils et permissions.
- *Conçu par* : <a href="https://github.com/matheorevel17"><img src="https://github.com/matheorevel17.png" width="20" style="border-radius:50%; vertical-align: middle;"> **matheorevel17**</a>
- *Développé par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>

### 2. Gestion de Session
- **Suivi de Session** : Les sessions actives sont suivies avec des identifiants uniques.
- **Propriétés** : Gestion des propriétés spécifiques à la session, comme la couleur principale et la taille de la police.
- *Conçu par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>
- *Développé par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>

### 3. Amis & Inter-relations
- **Gérer les Amis** : Les utilisateurs peuvent ajouter, supprimer ou lister des amis.
- **Recherche** : Fonctionnalité pour rechercher d'autres utilisateurs.
- *Conçu par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>
- *Développé par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>

### 4. Entrée
- **Gestion des Entrées** : Les utilisateurs peuvent créer, participer à et modifier une entrée.
- **Export/Import** : Les utilisateurs peuvent importer et exporter une entrée (Non terminé).
- **Permissions** : Les propriétaires de projets peuvent ajouter d'autres utilisateurs avec les rôles de Lecteur, Commentateur ou Éditeur.
- **Commentaires** : Les utilisateurs peuvent commenter une entrée.
- *Conçu par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>
- *Développé par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>

### 5. Canaux de Discussion
- **Chat** : L'utilisateur peut envoyer un message privé à un autre utilisateur.
- *Conçu par* : <a href="https://github.com/matheorevel17"><img src="https://github.com/matheorevel17.png" width="20" style="border-radius:50%; vertical-align: middle;"> **matheorevel17**</a>
- *Développé par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>

### 6. Analytique (Abandonné)
- **Analyse** : L'utilisateur peut obtenir des statistiques pour un projet ou pour lui-même.
- *Conçu par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>
- *Développé par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a> (Abandonné)

### 7. Gestion de Projet  (N'a Pas Satisfait aux Exigences de Qualité lors du Développement)
- **Gérer les Entrées** : L'utilisateur peut voir les projets qui lui sont liés.
- *Conçu par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>
- *Développé par* : <a href="https://github.com/matheorevel17"><img src="https://github.com/matheorevel17.png" width="20" style="border-radius:50%; vertical-align: middle;"> **matheorevel17**</a> (NPSEQ)

### 8. Notifications (N'a Pas Satisfait aux Exigences de Qualité lors du Développement)
- **Alertes** : Lorsqu'un utilisateur est invité, il est alerté par des notifications.
- **Informer** : Lorsqu'un commentaire est posté, les utilisateurs liés au projet reçoivent une notification.
- *Conçu par* : <a href="https://github.com/byxis"><img src="https://github.com/byxis.png" width="20" style="border-radius:50%; vertical-align: middle;"> **byxis**</a>
- *Développé par* : <a href="https://github.com/matheorevel17"><img src="https://github.com/matheorevel17.png" width="20" style="border-radius:50%; vertical-align: middle;"> **matheorevel17**</a> (NPSEQ)

### 9. Journalisation (Non commencé)
- **Logs** : Toutes les actions de l'utilisateur sont enregistrées, afin qu'il puisse annuler et rétablir des actions.
- *Conçu par* : <a href="https://github.com/dawoldo"><img src="https://github.com/dawoldo.png" width="20" style="border-radius:50%; vertical-align: middle;"> **dawoldo**</a>
- *Développé par* : <a href="https://github.com/matheorevel17"><img src="https://github.com/matheorevel17.png" width="20" style="border-radius:50%; vertical-align: middle;"> **matheorevel17**</a> (Non commencé)
