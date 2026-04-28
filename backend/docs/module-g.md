## Module G — Ordonnances & Dossier patient

### Objectif

Gérer le dossier patient (identité + médical protégé), l’ordonnance (création, validation pharmacien), la dispensation avec traçabilité (lots) et les alertes (allergies, renouvellement).

### Modèle de données (Flyway)

- `patient`: identité + résumé assurance (visible caissier)
- `patient_medical`: allergies/pathologies/médecin traitant (**accès pharmacien**)
- `prescripteur`
- `ordonnance`: statut, dates, lien `ordonnance_parent_id` (renouvellement)
- `ordonnance_ligne`: quantités prescrites / dispensées
- `dispensation`: enregistrement de chaque dispensation, avec **lot_id + emplacement_id** (V14)
- `ordonnance_piece`: métadonnées d’une pièce scannée (storage_key)

### Règles métier implémentées

#### G.1 Dossier patient
- **Création patient** par `CAISSIER`/`PHARMACIEN`/`ADMIN` (saisie basique possible).
- **Médical protégé**: mise à jour uniquement `PHARMACIEN`/`ADMIN`.

#### G.2 Allergies (blocage + override pharmacien)
- Lors d’une dispensation, si allergies patient indiquent un risque (ex: “Pénicillines”) et que le produit ressemble à une pénicilline (ex: Amoxicilline),
  la dispensation est **bloquée**.
- Override possible uniquement `PHARMACIEN/ADMIN` avec **motif_override** obligatoire.
- Une alerte dédupliquée `ALLERGIE_MEDICAMENTEUSE` est ouverte.

#### G.3 Dispensation partielle
- Une ligne d’ordonnance maintient `quantite_dispensee`.
- Le statut de l’ordonnance passe à `PARTIELLEMENT_DISPENSEE` ou `DISPENSEE`.
- Déstockage **FEFO au comptoir médicaments** + mouvements stock `DISPENSATION`.

#### G.4 Renouvellement
- Scheduler quotidien: alerte `ORDONNANCE_EXPIRE_BIENTOT` à J-7 (dédupliquée).

### Endpoints REST (RBAC)

#### Patients
- `POST /api/patients`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
- `PUT /api/patients/{patientId}/medical`
  - Rôles: `PHARMACIEN`, `ADMIN`
- `GET /api/patients`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Retourne uniquement: identité + téléphone + assurance (pas de médical).
  - Recherche (optionnelle): `GET /api/patients?q=kamga` (nom/prénom/téléphone), limité à 50 résultats.
- `GET /api/patients/{patientId}`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Retourne uniquement: identité + contact + assurance (pas de médical).
- `GET /api/patients/{patientId}/medical`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Lecture médical protégé (allergies/pathologies/médecin traitant).
- `GET /api/patients/{patientId}/historique-dispensations`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Historique des dispensations (limit 500) trié par date décroissante.

#### Ordonnances
- `POST /api/ordonnances`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
- `POST /api/ordonnances/{ordonnanceId}/validation`
  - Rôles: `PHARMACIEN`, `ADMIN`
- `POST /api/ordonnances/{ordonnanceId}/refus`
  - Rôles: `PHARMACIEN`, `ADMIN`
- `POST /api/ordonnances/{ordonnanceId}/dispensations`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Blocage allergie + override `PHARMACIEN/ADMIN`.
- `POST /api/ordonnances/{ordonnanceId}/pieces` (multipart)
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Upload d’une pièce scannée (stockage disque + `storage_key`).
- `GET /api/ordonnances/{ordonnanceId}`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Retourne détails + lignes + pièces + dispensations.
- `GET /api/ordonnances/{ordonnanceId}/pieces/{pieceId}/download`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Téléchargement d’une pièce (stockage local) avec contrôle d’appartenance à l’ordonnance.
- `GET /api/ordonnances/patients/{patientId}`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Liste des ordonnances d’un patient.
- `GET /api/ordonnances/en-attente-validation`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Liste (limit 100) des ordonnances `EN_ATTENTE_VALIDATION`.
- `POST /api/ordonnances/{ordonnanceId}/renouvellement`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Crée une nouvelle ordonnance liée (`ordonnance_parent_id`) en recopiant les lignes (posologie/durée incluses).

#### Prescripteurs
- `POST /api/prescripteurs`
  - Rôles: `PHARMACIEN`, `ADMIN`
- `GET /api/prescripteurs`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`

### Audit (exemples)
- `PATIENT_CREE`, `PATIENT_MEDICAL_MAJ`
- `ORDONNANCE_CREEE`, `ORDONNANCE_VALIDEE`, `ORDONNANCE_REFUSEE`
- `ORDONNANCE_PIECE_AJOUTEE`
- `PRESCRIPTEUR_CREE`
- `DISPENSATION_EFFECTUEE`

