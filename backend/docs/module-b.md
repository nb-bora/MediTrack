# Module B — Utilisateurs, rôles, permissions, audit (V1)

Ce document décrit **le module B** tel qu’implémenté dans le backend (`backend/`), conformément à la spécification `pharma_system_complet.md`.

## Objectifs

- Authentifier les utilisateurs (login / refresh / logout) via **JWT + refresh token**
- Appliquer les règles de sécurité :
  - mots de passe en **BCrypt**
  - politique de complexité
  - expiration à 90 jours
  - blocage après 5 tentatives échouées
  - non-réutilisation des 5 derniers mots de passe
  - rate limiting sur le login
- Gérer RBAC : rôles et permissions
- Tracer les actions dans un **journal d’audit immuable**

## Endpoints (API)

### Bootstrap (setup)

- `POST /api/setup/creer-admin-initial`
  - **But** : créer le tout premier compte Administrateur après le setup Module A.
  - **Préconditions** :
    - une organisation existe
    - aucun utilisateur n’existe encore
  - **Résultat** : renvoie un mot de passe temporaire (une seule fois) et force le changement au 1er login.

### Authentification

- `POST /api/auth/login`
  - **Entrée** : `login`, `password`
  - **Sortie** : `accessToken`, `refreshToken`, `doitChangerMdp`
  - **Règles** :
    - si verrouillé : refus
    - si mdp expiré : refus
    - si mauvais mdp : incrémente tentatives + audit + verrouille après 5
    - si OK : reset tentatives + crée session + audit

- `POST /api/auth/refresh`
  - **Entrée** : `refreshToken`
  - **Sortie** : `accessToken`, `refreshToken` (rotation)
  - **Règles** :
    - refresh token hashé en base (SHA-256)
    - rotation à chaque refresh
    - session expirée/révoquée : refus

- `POST /api/auth/logout`
  - **Entrée** : `refreshToken`
  - **Effet** : révocation de la session

- `POST /api/auth/changer-mot-de-passe`
  - **Auth** : JWT requis
  - **Entrée** : `ancienMotDePasse`, `nouveauMotDePasse`
  - **Règles** :
    - complexité
    - non-réutilisation des 5 derniers (`password_history`)
    - maj de l’expiration (90 jours)

## Données (modèle)

- `utilisateur` : compte, état, tentatives, verrouillage, expiration
- `role` / `permission` : RBAC
- `utilisateur_role` : liaison user↔role
- `session_auth` : refresh token hashé + expiry + révocation
- `password_history` : historique des hashes (5 derniers)
- `evenement_audit` : audit append-only (immutabilité enforce en base)

## Rate limiting (anti-brute force)

Filtre : `LoginRateLimitFilter`  
Stratégie V1 : in-memory par IP (suffisant en mono-poste / petit LAN).

## Audit

Writer : `AuditWriter` (JPA)  
Événements tracés (V1) :
- `CONNEXION_REUSSIE`
- `CONNEXION_ECHEC`
- `TOKEN_REFRESH`
- `DECONNEXION`
- `UTILISATEUR_CREE` (admin initial)
- `MDP_CHANGE`
- `COMPTE_VERROUILLE`

