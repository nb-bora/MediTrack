# Module A — Référentiel & configuration initiale (V1)

## Objectifs

- Créer l’organisation et ses données de base (site, emplacements, numérotation)
- Fournir des mécanismes de **configuration par organisation** en base

## Données

- `organisation`
- `site`
- `emplacement`
- `sequence_numerotation`
- `parametre`

## Configuration (Admin)

Référez-vous à `docs/parametres-metier.md` pour la liste des clés et les endpoints.

### Emplacements

Endpoints :
- `GET /api/admin/referentiel/emplacements`
- `POST /api/admin/referentiel/emplacements`
- `PUT /api/admin/referentiel/emplacements/{emplacementId}`

### Numérotation

Endpoints :
- `GET /api/admin/referentiel/numerotation`
- `PUT /api/admin/referentiel/numerotation/{typeDocument}`
- `POST /api/admin/referentiel/numerotation/{typeDocument}/reset`

