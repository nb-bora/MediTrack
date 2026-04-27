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

- Organismes
  - `POST /api/assurances/organismes`
  - `GET /api/assurances/organismes`
  - `PUT /api/assurances/organismes/{id}/couverture`
- Dossiers tiers payant
  - `POST /api/assurances/dossiers`
  - `POST /api/assurances/dossiers/{id}/soumission`
  - `POST /api/assurances/dossiers/{id}/rejet`
  - `POST /api/assurances/dossiers/{id}/resoumission`
  - `POST /api/assurances/dossiers/{id}/paiement`

