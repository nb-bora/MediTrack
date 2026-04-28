# Paramètres métiers configurables (par organisation)

Ce document décrit les **paramètres métiers** stockés en base (`parametre`) et utilisés automatiquement **par organisation**.

## Principe (fallback automatique)

- Si un paramètre existe en base pour l’organisation, **il est utilisé**.
- Sinon, le système utilise la **valeur par défaut** (comportement historique du code).

## API Admin (gestion des paramètres)

- **Lister** : `GET /api/admin/parametres`
- **Créer / modifier** : `PUT /api/admin/parametres/{cle}`

Corps (JSON) :

```json
{
  "valeur": "90",
  "typeValeur": "NUMBER",
  "description": "Alerte péremption précoce (jours)"
}
```

Notes :
- `cle` est normalisée en **MAJUSCULES**.
- `typeValeur` est informatif (contrôle applicatif typé au runtime).

## API Admin (référentiel configurable)

### Emplacements

- `GET /api/admin/referentiel/emplacements`
- `POST /api/admin/referentiel/emplacements`
- `PUT /api/admin/referentiel/emplacements/{emplacementId}`

### Numérotation (séquences)

- `GET /api/admin/referentiel/numerotation`
- `PUT /api/admin/referentiel/numerotation/{typeDocument}`
- `POST /api/admin/referentiel/numerotation/{typeDocument}/reset`

## Catalogue des clés (par module)

### Module A — Référentiel / devise / numérotation

- **`DEVISE`** (STRING, défaut: `XAF`)

### Module B — Sécurité / Auth

- **`AUTH_MAX_FAILED_ATTEMPTS`** (NUMBER, défaut: `5`)
- **`AUTH_LOCK_DAYS`** (NUMBER, défaut: `3650`)
- **`PASSWORD_EXPIRY_DAYS`** (NUMBER, défaut: `90`)
- **`PASSWORD_HISTORY_COUNT`** (NUMBER, défaut: `5`)
- **`PASSWORD_MIN_LENGTH`** (NUMBER, défaut: `8`)
- **`PASSWORD_REQUIRE_UPPER`** (BOOL, défaut: `true`)
- **`PASSWORD_REQUIRE_DIGIT`** (BOOL, défaut: `true`)
- **`PASSWORD_REQUIRE_SPECIAL`** (BOOL, défaut: `true`)
- **`LOGIN_RATELIMIT_ENABLED`** (BOOL, défaut: `true`)
- **`LOGIN_RATELIMIT_CAPACITY`** (NUMBER, défaut: `10`)
- **`LOGIN_RATELIMIT_REFILL_SECONDS`** (NUMBER, défaut: `60`)

### Module C — Catalogue produits

- **`PRODUIT_DEDUP_DCI_DOSAGE_ENABLED`** (BOOL, défaut: `true`)
- **`PRODUIT_PROFIL_TAXE_DEFAUT_MEDICAMENTS`** (STRING, défaut: `MEDICAMENTS`)
- **`PRODUIT_PROFIL_TAXE_DEFAUT_PARAPHARMA`** (STRING, défaut: `PARAPHARMA`)
- **`EAN13_UNIQUE_GLOBAL`** (BOOL, défaut: `true`)  
  - `true`: unicité EAN13 globale (toutes organisations)  
  - `false`: unicité EAN13 limitée à l’organisation
- **`PRIX_CHEVAUCHEMENT_INTERDIT`** (BOOL, défaut: `true`)

### Module D — Stocks / péremption / réception

- **`ALERTE_PEREMPTION_PRECOCE_JOURS`** (NUMBER, défaut: `90`)
- **`ALERTE_PEREMPTION_URGENTE_JOURS`** (NUMBER, défaut: `30`)
- **`RECEPTION_ALERTE_PEREMPTION_MOIS`** (NUMBER, défaut: `6`)
- **`RECEPTION_CHAINE_FROID_TEMP_MIN_C`** (NUMBER, défaut: `2.0`)
- **`RECEPTION_CHAINE_FROID_TEMP_MAX_C`** (NUMBER, défaut: `8.0`)

### Module E — Achats / fournisseurs

- **`BC_RETARD_ALERTE_ENABLED`** (BOOL, défaut: `true`)
- **`BC_RETARD_GRACE_JOURS`** (NUMBER, défaut: `0`)
- **`BC_RETARD_SEVERITE`** (STRING, défaut: `IMPORTANT`)
- **`RECEPTION_WORKFLOW_PRIX_DIFFERENT_ACTIF`** (BOOL, défaut: `true`)
- **`RECEPTION_PRIX_DIFFERENT_SEVERITE`** (STRING, défaut: `IMPORTANT`)
- **`FOURNISSEUR_KPI_FENETRE_JOURS`** (NUMBER, défaut: `180`)
- **`FOURNISSEUR_KPI_POIDS_LIVRAISON_PCT`** (NUMBER, défaut: `80.0`)
- **`FOURNISSEUR_KPI_PENALITE_RETOUR_PAR_UNITE`** (NUMBER, défaut: `2.0`)
- **`FOURNISSEUR_KPI_PENALITE_RETOUR_MAX`** (NUMBER, défaut: `20.0`)

### Module F — Caisse

- **`REMISE_MAX_CAISSIER_PCT`** (NUMBER, défaut: `5`)
- **`REMISE_MAX_PHARMACIEN_PCT`** (NUMBER, défaut: `15`)
- **`ARRONDI_MAX_CAISSIER`** (NUMBER, défaut: `50`)

### Module G — Ordonnances

- **`ORDONNANCE_ALERTE_RENOUVELLEMENT_JOURS`** (NUMBER, défaut: `7`)

### Module H — Assurance / tiers payant

- **`TP_PLAFONDS_ACTIFS`** (BOOL, défaut: `true`)
- **`TP_PIECES_ALERTE_ENABLED`** (BOOL, défaut: `true`)
- **`TP_PIECES_ALERTE_SEVERITE`** (STRING, défaut: `IMPORTANT`)

