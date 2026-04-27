## Module D — Stocks & traçabilité (V1)

Ce module couvre la gestion des **lots**, des **mouvements de stock**, la règle **FEFO**, la **quarantaine / périmés**, les **rappels** et l’**inventaire**.

### Principes non négociables (extraits spec)

- **FEFO** : tout retrait de stock doit prioriser le lot qui périme le plus tôt.
- **Périmé / quarantaine** : un lot non ACTIF est considéré **non vendable**.
- **Traçabilité** : toute entrée/sortie doit créer un mouvement et un événement d’audit.

### Données (DB)

Migration : `V7__stocks_tracabilite.sql`

- `lot_stock` : lot + statut (`ACTIF`, `PERIME_AUTOMATIQUE`, `QUARANTAINE`, `RAPPELE`, …)
- `stock_emplacement` : solde par `emplacement_id` et `lot_id`
- `mouvement_stock` : journal de mouvements (réception/transfert/ajustement/…)
- `rappel_lot` : rappel fournisseur d’un lot (criticité, motif, source)
- `inventaire` + `inventaire_ligne` : inventaires mensuels et saisies

### Automatisme “périmés”

- Un job planifié à **minuit** met à jour automatiquement les statuts selon la proximité de la date :
  - **J-90 → J-31** : `PEREMPTION_PRECOCE` (signalement précoce)
  - **J-30 → 0** : `PEREMPTION_URGENTE` (signalement urgent)
  - **< 0** : `PERIME_AUTOMATIQUE`
- Événement d’audit : `LOT_STATUT_PEREMPTION_MAJ`

### API (endpoints)

#### Réception

- `POST /api/stocks/receptions` (ADMIN, MAGASINIER)
  - crée (ou réutilise) un lot `lot_stock` (unicité `organisation + produit + numero_lot`)
  - incrémente `stock_emplacement` sur l’emplacement destination
  - crée un `mouvement_stock` de type `RECEPTION`
  - audit : `STOCK_RECEPTIONNE`
  - anomalies tracées (audit) :
    - `RECEPTION_ANOMALIE_QTE` (si `quantite_commandee` > `quantite_recue`)
    - `RECEPTION_ANOMALIE_PRIX` (si `prix_attendu_unitaire` ≠ `prix_facture_unitaire`)
  - chaîne du froid :
    - si `temperature_transport_c` est fournie et hors plage config, le lot passe en `QUARANTAINE` et le stock est déplacé vers l’emplacement `QUARANTAINE`.

#### Transfert (réserve → comptoir, FEFO)

- `POST /api/stocks/transferts` (ADMIN, MAGASINIER)
  - sélectionne automatiquement les lots à déplacer selon FEFO
  - décrémente source / incrémente destination
  - crée un `mouvement_stock` `TRANSFERT` par lot consommé
  - audit : `STOCK_TRANSFERE`

#### Rappel lot

- `POST /api/stocks/rappels` (ADMIN, PHARMACIEN)
  - crée un `rappel_lot` (unicité `organisation + lot`)
  - passe le lot en statut `RAPPELE` (non vendable)
  - crée un mouvement logique `MISE_QUARANTAINE` (traçabilité)
  - audit : `RAPPEL_LOT_CREE`

#### Consultation / vues opérateur (V1)

- `GET /api/stocks/mouvements` (auth)
  - filtres optionnels : `produitId`, `lotId`, `from`, `to`
- `GET /api/stocks/emplacements/{emplacementId}/produits/{produitId}/lots` (auth)
  - lots disponibles ACTIF triés FEFO
- `GET /api/stocks/lots/{lotId}/localisation` (auth)
  - quantités par emplacement pour un lot
- `POST /api/stocks/allocations/fefo` (auth)
  - calcule un plan de prélèvement FEFO (utile pour POS / dispensation)

#### Inventaire

- `POST /api/stocks/inventaires` (ADMIN, PHARMACIEN)
  - crée un inventaire (un seul “OUVERT” à la fois)
  - génère les lignes à partir du stock théorique (lots ACTIF uniquement)
  - audit : `INVENTAIRE_CREE`

- `POST /api/stocks/inventaires/{inventaireId}/lignes/saisie` (PHARMACIEN, MAGASINIER, ADMIN)
  - saisie du stock réel ; motif obligatoire si écart
  - audit : `INVENTAIRE_LIGNE_SAISIE`

- `POST /api/stocks/inventaires/{inventaireId}/validation` (ADMIN, PHARMACIEN)
  - applique des ajustements :
    - écart négatif : retrait FEFO des lots ACTIF à l’emplacement
    - écart positif : ajout via un **lot d’ajustement** `AJUST-{inventaireId}`
  - mouvements : `AJUSTEMENT`
  - audit : `INVENTAIRE_VALIDE`

### Événements d’audit (V1)

- `STOCK_RECEPTIONNE`
- `RECEPTION_ANOMALIE_QTE`
- `RECEPTION_ANOMALIE_PRIX`
- `LOT_MIS_EN_QUARANTAINE`
- `STOCK_TRANSFERE`
- `LOT_STATUT_PEREMPTION_MAJ`
- `RAPPEL_LOT_CREE`
- `INVENTAIRE_CREE`
- `INVENTAIRE_LIGNE_SAISIE`
- `INVENTAIRE_VALIDE`

