## Module H — Assurance / Mutuelle (Tiers payant)

### Objectif

Paramétrer des organismes (CNPS, mutuelles), définir leurs règles de couverture et gérer les dossiers tiers payant (soumission, rejet, re-soumission, paiement).

### Données (Flyway)

- `organisme_assurance`
- `organisme_couverture`
- `dossier_tiers_payant`

### Règles (H.1/H.2)

- Un organisme a un **code unique** par organisation.
- La couverture est définie par taux + plafonds (journalier/mensuel/annuel) + pièces requises.
- Un dossier tiers payant suit les statuts: `BROUILLON → SOUMIS → (REJETE → RESOUMIS)* → PAYE` (ou `ANNULE`).
- En cas de rejet, un motif est obligatoire et le suivi conserve les dates.

### Endpoints (à implémenter)

#### Organismes

- `POST /api/assurances/organismes` (ADMIN)
- `GET /api/assurances/organismes` (ADMIN/COMPTABLE/PHARMACIEN)
- `PUT /api/assurances/organismes/{organismeId}/couverture` (ADMIN)
- `GET /api/assurances/organismes/{organismeId}/couverture` (ADMIN/COMPTABLE/PHARMACIEN)

#### Dossiers tiers payant

- `GET /api/assurances/dossiers?statut=SOUMIS` (ADMIN/COMPTABLE/PHARMACIEN)
- `POST /api/assurances/dossiers` (ADMIN/COMPTABLE) — body: `{ "venteId": "..." }`
- `POST /api/assurances/dossiers/{dossierId}/soumission` (ADMIN/COMPTABLE)
- `POST /api/assurances/dossiers/{dossierId}/rejet` (ADMIN/COMPTABLE) — body: `{ "motif": "..." }`
- `POST /api/assurances/dossiers/{dossierId}/resoumission` (ADMIN/COMPTABLE)
- `POST /api/assurances/dossiers/{dossierId}/paiement` (ADMIN/COMPTABLE)

#### Pièces dossier

- `GET /api/assurances/dossiers/{dossierId}/pieces` (ADMIN/COMPTABLE/PHARMACIEN)
- `POST /api/assurances/dossiers/{dossierId}/pieces` (ADMIN/COMPTABLE) — multipart: `type_piece`, `file`
- `GET /api/assurances/dossiers/{dossierId}/pieces/{pieceId}/download` (ADMIN/COMPTABLE/PHARMACIEN)

#### Statistiques rejets

- `GET /api/assurances/dossiers/stats/rejets` (ADMIN/COMPTABLE)

### Intégration POS (Module F)

- Si une vente contient un paiement `TIERS_PAYANT`, un dossier `dossier_tiers_payant` est **créé automatiquement** à la validation de la vente, à partir du contexte tiers payant défini sur la vente (patient/organisme/ordonnance/numéro adhérent).
- Une alerte dédupliquée `DOSSIER_TP_PIECES_A_VERIFIER` est ouverte tant que le dossier est en `BROUILLON` et qu’il reste des pièces obligatoires à joindre/contrôler avant soumission.

