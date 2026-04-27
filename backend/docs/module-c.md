# Module C — Catalogue produits (V1)

Ce module couvre la création et la gestion du **catalogue produits** : produits, profils TVA, catégories, codes-barres, conditionnements, prix historisés, substituts.

## Principes non négociables (extraits spec)

- **Détection de doublon** : si un produit avec la même **DCI** et le même **dosage** existe déjà, le système doit signaler le doublon et inciter à créer un lien de substituabilité.
- **Historisation prix** : un prix **ne se modifie jamais**. On crée une nouvelle ligne de prix avec une date d’effet. L’ancienne ligne est clôturée.
- **Codes-barres** : un EAN13 ne doit pas pointer vers plusieurs produits (unicité globale).
- **Conditionnement principal** : un produit ne doit avoir qu’un seul conditionnement marqué "principal" (unité de vente principale).

## Contexte organisation (anti-ambiguïté)

L’API dérive `organisation_id` depuis le **JWT** (`claim organisation_id`).
Un client ne peut pas forcer une autre organisation via un paramètre.

## API (endpoints)

### Produits

- `POST /api/catalogue/produits` (ADMIN, PHARMACIEN)
  - Crée un produit et applique la détection de doublon DCI+dosage.
- `GET /api/catalogue/produits?q=...` (auth requis)
  - Recherche sur `nom_commercial` et `dci`.

### Codes-barres

- `POST /api/catalogue/produits/{produitId}/codes-barres` (ADMIN, PHARMACIEN)
  - Ajoute un EAN13 (13 chiffres) avec unicité globale.

### Catégories

- `POST /api/catalogue/categories` (ADMIN, PHARMACIEN)
  - Crée une catégorie (unicité logique : `organisation_id + parent_id + nom`).
- `GET /api/catalogue/categories` (auth requis)
  - Liste des catégories de l’organisation.

### Conditionnements

- `POST /api/catalogue/produits/{produitId}/conditionnements` (ADMIN, PHARMACIEN)
  - Ajoute un conditionnement.
  - Si `estPrincipal=true`, tout autre conditionnement principal du produit est automatiquement dé-priorisé.

### Prix (historisés)

- `POST /api/catalogue/produits/{produitId}/prix` (ADMIN uniquement)
  - Crée une nouvelle ligne de prix avec `dateDebut` (date d’effet).
  - Si un prix est actif, il est clôturé automatiquement : `dateFin = dateDebut - 1`.
  - Les chevauchements sont interdits.

### Substituts

- `POST /api/catalogue/produits/{produitId}/substituts` (ADMIN, PHARMACIEN)
  - Lie 2 produits comme substituts (`EQUIVALENT` ou `ALTERNATIVE`).
  - Le lien est enregistré dans les deux sens pour simplifier l’affichage/recherche.

## Données (DB)

Migration : `V6__catalogue_produits.sql`

- `profil_taxe`
- `categorie_produit`
- `produit`
- `code_barres_produit`
- `conditionnement_produit`
- `prix_produit`
- `produit_substitut`

## Événements d’audit (V1)

- `PRODUIT_CREE`
- `CODE_BARRES_CREE`
- `CATEGORIE_CREE`
- `CONDITIONNEMENT_CREE`
- `PRIX_CREE`
- `SUBSTITUT_LIE`

