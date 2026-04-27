## Module E — Achats & Fournisseurs (V1)

Ce module couvre la gestion des **fournisseurs**, des **bons de commande**, des **réceptions** (y compris partielles) et des **retours fournisseur**.

### Données (DB)

Migration : `V9__achats_fournisseurs.sql`

- `fournisseur`
- `bon_commande` + `bon_commande_ligne`
- `reception_fournisseur` + `reception_fournisseur_ligne`
- `retour_fournisseur` + `retour_fournisseur_ligne`

### Règles clés (V1)

- **Numérotation BC** : générée via `sequence_numerotation` (type `BON_COMMANDE`, format par défaut `BC-{AA}-{SEQ4}`).
- **Réception partielle** : la réception met à jour `quantite_recue` des lignes et le statut du BC :
  - `RECU_PARTIEL` tant que toutes les lignes ne sont pas couvertes
  - `RECU_COMPLET` quand toutes les quantités commandées sont reçues
- **Stock** : la réception appelle le Module D pour créer le lot + mouvement + stock emplacement (FEFO ensuite).
- **Retours** : un retour validé sort la quantité depuis l’emplacement `QUARANTAINE` (Module D), mouvement `RETOUR_FOURNISSEUR`.

### API (endpoints)

#### Fournisseurs

- `POST /api/achats/fournisseurs` (ADMIN)
- `GET /api/achats/fournisseurs` (auth)
- `PUT /api/achats/fournisseurs/{id}` (ADMIN)

#### Bons de commande

- `POST /api/achats/bons-commandes` (ADMIN, MAGASINIER)
- `POST /api/achats/bons-commandes/{id}/lignes` (ADMIN, MAGASINIER)
- `POST /api/achats/bons-commandes/{id}/validation` (ADMIN)
- `GET /api/achats/bons-commandes` (auth)
- `GET /api/achats/bons-commandes/{id}/pdf` (auth)

#### Réceptions

- `POST /api/achats/receptions` (ADMIN, MAGASINIER)
  - optionnel : `bonCommandeId` (pour appliquer réception partielle/complet)
  - lignes : produit, emplacement, lot, péremption, quantité, prix facture, température transport, etc.
  - workflow prix : si réception liée à un BC et prix facture fourni → statut `EN_ATTENTE` + alerte à traiter

- `POST /api/achats/receptions/{id}/prix/validation` (ADMIN, COMPTABLE)
- `POST /api/achats/receptions/{id}/prix/refus` (ADMIN, COMPTABLE)

#### Retours fournisseur

- `POST /api/achats/retours` (ADMIN, MAGASINIER)
- `POST /api/achats/retours/{id}/lignes` (ADMIN, MAGASINIER)
- `POST /api/achats/retours/{id}/validation` (ADMIN)
- `POST /api/achats/retours/{id}/envoi` (ADMIN)
- `POST /api/achats/retours/{id}/cloture` (ADMIN)

### Alertes (support)

- `GET /api/alertes` (ADMIN)
- `POST /api/alertes/{id}/resolution` (ADMIN)

### Événements d’audit (V1)

- `FOURNISSEUR_CREE`
- `FOURNISSEUR_MODIFIE`
- `BON_COMMANDE_CREE`
- `BON_COMMANDE_LIGNE_AJOUTEE`
- `BON_COMMANDE_VALIDE`
- `RECEPTION_FOURNISSEUR_ENREGISTREE`
- `RECEPTION_PRIX_VALIDE`
- `RECEPTION_PRIX_REFUSE`
- `RETOUR_FOURNISSEUR_CREE`
- `RETOUR_FOURNISSEUR_LIGNE_AJOUTEE`
- `RETOUR_FOURNISSEUR_VALIDE`
- `RETOUR_FOURNISSEUR_ENVOYE`
- `RETOUR_FOURNISSEUR_CLOTURE`

