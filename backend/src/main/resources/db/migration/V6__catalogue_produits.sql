-- V6 — Catalogue produits (Module C)
--
-- Tables : categorie_produit, profil_taxe, produit, code_barres_produit, conditionnement_produit,
--          prix_produit (historique), produit_substitut.

create table if not exists categorie_produit (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    parent_id uuid null references categorie_produit(id) on delete restrict,
    nom text not null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create unique index if not exists ux_categorie_produit_organisation_parent_nom on categorie_produit (organisation_id, parent_id, nom);

create table if not exists profil_taxe (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    nom text not null,
    taux_tva numeric(7,4) not null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_profil_taxe_taux check (taux_tva >= 0 and taux_tva <= 1)
);

create unique index if not exists ux_profil_taxe_organisation_nom on profil_taxe (organisation_id, nom);

create table if not exists produit (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    type_produit varchar(20) not null,
    dci text null,
    nom_commercial text not null,
    forme_galenique text null,
    dosage text null,
    laboratoire text null,
    pays_origine text null,
    categorie_id uuid null references categorie_produit(id) on delete restrict,
    necessite_ordonnance boolean not null default false,
    est_stupefiant boolean not null default false,
    est_psychotrope boolean not null default false,
    est_controle boolean not null default false,
    profil_taxe_id uuid not null references profil_taxe(id) on delete restrict,
    stock_minimum integer null,
    stock_securite integer null,
    delai_reappro_jours integer null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_produit_stock_min_non_negatif check (stock_minimum is null or stock_minimum >= 0),
    constraint ck_produit_stock_sec_non_negatif check (stock_securite is null or stock_securite >= 0),
    constraint ck_produit_delai_non_negatif check (delai_reappro_jours is null or delai_reappro_jours >= 0)
);

create index if not exists ix_produit_organisation_id on produit (organisation_id);
create index if not exists ix_produit_nom_commercial on produit (nom_commercial);
create index if not exists ix_produit_dci on produit (dci);
create index if not exists ix_produit_categorie_id on produit (categorie_id);

create table if not exists code_barres_produit (
    id uuid primary key default gen_random_uuid(),
    produit_id uuid not null references produit(id) on delete cascade,
    ean13 varchar(13) not null,
    libelle text null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create unique index if not exists ux_code_barres_produit_ean13 on code_barres_produit (ean13);
create index if not exists ix_code_barres_produit_produit_id on code_barres_produit (produit_id);

create table if not exists conditionnement_produit (
    id uuid primary key default gen_random_uuid(),
    produit_id uuid not null references produit(id) on delete cascade,
    nom text not null,
    unite_base_nom text not null,
    quantite_unite_base integer not null,
    est_principal boolean not null default false,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_conditionnement_quantite check (quantite_unite_base > 0)
);

create index if not exists ix_conditionnement_produit_produit_id on conditionnement_produit (produit_id);

create table if not exists prix_produit (
    id uuid primary key default gen_random_uuid(),
    produit_id uuid not null references produit(id) on delete cascade,
    type_prix varchar(10) not null,
    montant numeric(19,4) not null,
    devise varchar(3) not null default 'XAF',
    date_debut date not null,
    date_fin date null,
    motif text null,
    cree_par uuid null references utilisateur(id) on delete set null,
    created_at timestamptz not null default now(),
    constraint ck_prix_montant_non_negatif check (montant >= 0),
    constraint ck_prix_dates check (date_fin is null or date_fin >= date_debut)
);

create index if not exists ix_prix_produit_produit_type_date on prix_produit (produit_id, type_prix, date_debut desc);

create table if not exists produit_substitut (
    produit_id uuid not null references produit(id) on delete cascade,
    substitut_produit_id uuid not null references produit(id) on delete restrict,
    niveau varchar(20) not null,
    created_at timestamptz not null default now(),
    created_by uuid null,
    primary key (produit_id, substitut_produit_id),
    constraint ck_produit_substitut_distinct check (produit_id <> substitut_produit_id)
);

