## Module F — Caisse / Ventes (POS)

Ce module couvre la caisse (sessions par poste), la création de ventes, le scan EAN, les paiements et la validation avec déstockage FEFO au comptoir (traçabilité lots), conformément aux contraintes :

- Accès contrôlé par rôles (`@PreAuthorize`).
- Données multi-organisation via le claim JWT `organisation_id`.
- Poste via le claim JWT `poste_nom` (V2).
- Traçabilité stock via Module D : allocation FEFO, mouvements, lots consommés.
- Audit immuable sur toutes les actions significatives.

### Endpoints

#### Sessions caisse

- `POST /api/caisse/sessions`
  - Rôles: `CAISSIER`, `COMPTABLE`, `ADMIN`
  - Règle: **une seule session OUVERTE par poste** (contrainte unique + validation métier).

- `POST /api/caisse/sessions/{sessionId}/fermeture`
  - Rôles: `COMPTABLE`, `ADMIN`
  - Corps: `montantReel`, `motifEcart` (motif optionnel).

#### Ventes

- `POST /api/caisse/ventes`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Règle: nécessite une session caisse OUVERTE sur le poste.
  - Génère `numero_vente` + `numero_ticket` via `NumerotationService`.

- `POST /api/caisse/ventes/{venteId}/scan`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Ajoute un produit à la vente via `ean13` (code-barres).
  - Applique le **prix de vente actif** (historisation Module C).

- `POST /api/caisse/ventes/{venteId}/paiements`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Supporte plusieurs paiements (paiement mixte).
  - Modes: `ESPECES`, `MOBILE_MONEY_MTN`, `MOBILE_MONEY_ORANGE`, `VIREMENT`, `CHEQUE`, `TIERS_PAYANT`, `AVOIR`.

- `POST /api/caisse/ventes/{venteId}/remises`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Applique une remise sur une ligne (en pourcentage).
  - Plafonds:
    - `CAISSIER` ≤ 5%
    - `PHARMACIEN` ≤ 15%
    - `ADMIN` au-delà (motif obligatoire si > 15%)

- `POST /api/caisse/ventes/{venteId}/arrondi`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - `CAISSIER` limité à 50 XAF.

- `POST /api/caisse/ventes/{venteId}/validation`
  - Rôles: `CAISSIER`, `PHARMACIEN`, `ADMIN`
  - Règles:
    - Vente doit être `BROUILLON`.
    - Paiement total >= **total net** (total TTC - arrondi) (sinon refus).
    - **FEFO au comptoir** : décrémente `stock_emplacement` en prenant les lots `ACTIF` triés par péremption croissante.
    - Trace les lots réellement consommés dans `vente_lot`.
    - Crée les mouvements `mouvement_stock` de type `VENTE`.
    - **Ordonnance**: si un produit `necessite_ordonnance=true`, la validation est refusée sauf `PHARMACIEN`/`ADMIN`.
  - Retourne un ticket texte (format initial).

- `POST /api/caisse/ventes/{venteId}/annulation`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Règles:
    - Vente doit être `VALIDEE`.
    - Motif obligatoire.
    - Réintègre le stock **au même lot et au même emplacement** à partir des enregistrements `vente_lot`.
    - Crée des mouvements `mouvement_stock` de type `ANNULATION_VENTE`.

- `POST /api/caisse/ventes/{venteId}/retours`
  - Rôles: `PHARMACIEN`, `ADMIN`
  - Crée un retour **partiel ou total** (sans annuler la vente d’origine).
  - Règles:
    - Vente doit être `VALIDEE`.
    - Motif obligatoire.
    - Réintégration stock **au même lot et au même emplacement** en se basant sur les `vente_lot` des lignes retournées.
    - Crée des mouvements `mouvement_stock` de type `RETOUR_VENTE`.
    - Génère un `numero_retour` (séquence `RETOUR_VENTE`).

### Audit (exemples d’actions)

- `CAISSE_OUVERTE`, `CAISSE_FERMEE`
- `VENTE_CREEE`, `VENTE_LIGNE_SCANNED`
- `PAIEMENT_AJOUTE`
- `VENTE_LOT_ALLOUE`, `VENTE_VALIDEE`
- `VENTE_ANNULEE`

### Points restant à étendre (prochaines itérations)

- Retours partiels (sélection d’articles) et règles “non retournable”.
- Remises/arrondis plafonnés par rôle.
- Ticket final (libellés produits, caissier, montants reçus, etc.) + option PDF.
- Tiers payant assurance : dossier, créance, facturation différée.

