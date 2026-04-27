-- V7 — Stocks & traçabilité (Module D)
--
-- Tables : lot_stock, mouvement_stock, stock_emplacement, inventaire, inventaire_ligne, rappel_lot

create table if not exists lot_stock (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    numero_lot text not null,
    date_peremption date not null,
    statut varchar(30) not null default 'ACTIF', -- ACTIF, PERIME_AUTOMATIQUE, QUARANTAINE, RAPPELE, DETRUIT, RETOUR_FOURNISSEUR
    motif_statut text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    updated_at timestamptz not null default now(),
    updated_by uuid null references utilisateur(id) on delete set null,
    constraint ck_lot_stock_statut check (statut in ('ACTIF','PERIME_AUTOMATIQUE','QUARANTAINE','RAPPELE','DETRUIT','RETOUR_FOURNISSEUR'))
);

create unique index if not exists ux_lot_stock_org_produit_numero on lot_stock (organisation_id, produit_id, numero_lot);
create index if not exists ix_lot_stock_org_statut_peremption on lot_stock (organisation_id, statut, date_peremption);

create table if not exists stock_emplacement (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    emplacement_id uuid not null references emplacement(id) on delete restrict,
    lot_id uuid not null references lot_stock(id) on delete restrict,
    quantite integer not null,
    updated_at timestamptz not null default now(),
    constraint ck_stock_emplacement_quantite_non_negatif check (quantite >= 0)
);

create unique index if not exists ux_stock_emplacement_org_empl_lot on stock_emplacement (organisation_id, emplacement_id, lot_id);
create index if not exists ix_stock_emplacement_org_lot on stock_emplacement (organisation_id, lot_id);

create table if not exists mouvement_stock (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    type_mouvement varchar(30) not null, -- RECEPTION, TRANSFERT, AJUSTEMENT, MISE_QUARANTAINE, DESTRUCTION, RETOUR_FOURNISSEUR
    lot_id uuid not null references lot_stock(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    quantite integer not null,
    emplacement_source_id uuid null references emplacement(id) on delete restrict,
    emplacement_destination_id uuid null references emplacement(id) on delete restrict,
    reference_document text null,
    motif text null,
    cree_par uuid null references utilisateur(id) on delete set null,
    created_at timestamptz not null default now(),
    constraint ck_mouvement_stock_quantite_positive check (quantite > 0)
);

create index if not exists ix_mouvement_stock_org_created_at on mouvement_stock (organisation_id, created_at desc);
create index if not exists ix_mouvement_stock_org_lot on mouvement_stock (organisation_id, lot_id);
create index if not exists ix_mouvement_stock_org_produit on mouvement_stock (organisation_id, produit_id);

create table if not exists rappel_lot (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    lot_id uuid not null references lot_stock(id) on delete restrict,
    criticite varchar(20) not null default 'URGENT', -- INFO, IMPORTANT, URGENT
    motif text not null,
    source text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    constraint ck_rappel_lot_criticite check (criticite in ('INFO','IMPORTANT','URGENT'))
);

create unique index if not exists ux_rappel_lot_org_lot on rappel_lot (organisation_id, lot_id);

create table if not exists inventaire (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    statut varchar(20) not null default 'OUVERT', -- OUVERT, VALIDE, ANNULE
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    validated_at timestamptz null,
    validated_by uuid null references utilisateur(id) on delete set null,
    constraint ck_inventaire_statut check (statut in ('OUVERT','VALIDE','ANNULE'))
);

create index if not exists ix_inventaire_org_created_at on inventaire (organisation_id, created_at desc);

create table if not exists inventaire_ligne (
    id uuid primary key default gen_random_uuid(),
    inventaire_id uuid not null references inventaire(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    emplacement_id uuid not null references emplacement(id) on delete restrict,
    stock_theorique integer not null,
    stock_reel integer null,
    ecart integer null,
    motif_ecart text null,
    updated_at timestamptz not null default now(),
    constraint ck_inventaire_ligne_stock_non_negatif check (stock_theorique >= 0 and (stock_reel is null or stock_reel >= 0))
);

create unique index if not exists ux_inventaire_ligne_unique on inventaire_ligne (inventaire_id, produit_id, emplacement_id);

