-- V11 — Module F : Caisse / Ventes (POS)

create table if not exists session_caisse (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    poste_nom text not null,
    statut varchar(20) not null default 'OUVERTE', -- OUVERTE, FERMEE
    caissier_id uuid not null references utilisateur(id) on delete restrict,
    fond_initial numeric(19,4) not null,
    devise varchar(3) not null default 'XAF',
    opened_at timestamptz not null default now(),
    closed_at timestamptz null,
    montant_reel_fermeture numeric(19,4) null,
    motif_ecart text null,
    constraint ck_session_caisse_statut check (statut in ('OUVERTE','FERMEE')),
    constraint ck_session_caisse_fond_non_negatif check (fond_initial >= 0)
);

create unique index if not exists ux_session_caisse_org_poste_ouverte on session_caisse (organisation_id, poste_nom) where statut = 'OUVERTE';
create index if not exists ix_session_caisse_org_opened_at on session_caisse (organisation_id, opened_at desc);

create table if not exists vente (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    session_caisse_id uuid not null references session_caisse(id) on delete restrict,
    numero_vente varchar(40) not null,
    numero_ticket varchar(40) not null,
    statut varchar(20) not null default 'BROUILLON', -- BROUILLON, VALIDEE, ANNULEE
    total_ttc numeric(19,4) not null default 0,
    total_remise numeric(19,4) not null default 0,
    devise varchar(3) not null default 'XAF',
    created_at timestamptz not null default now(),
    created_by uuid not null references utilisateur(id) on delete restrict,
    validated_at timestamptz null,
    validated_by uuid null references utilisateur(id) on delete set null,
    annulee_at timestamptz null,
    annulee_by uuid null references utilisateur(id) on delete set null,
    motif_annulation text null,
    constraint ck_vente_statut check (statut in ('BROUILLON','VALIDEE','ANNULEE'))
);

create unique index if not exists ux_vente_org_numero on vente (organisation_id, numero_vente);
create unique index if not exists ux_vente_org_ticket on vente (organisation_id, numero_ticket);
create index if not exists ix_vente_org_created_at on vente (organisation_id, created_at desc);

create table if not exists vente_ligne (
    id uuid primary key default gen_random_uuid(),
    vente_id uuid not null references vente(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    quantite integer not null,
    prix_unitaire_ttc numeric(19,4) not null,
    remise numeric(19,4) not null default 0,
    total_ligne numeric(19,4) not null,
    constraint ck_vente_ligne_quantite check (quantite > 0),
    constraint ck_vente_ligne_montants check (prix_unitaire_ttc >= 0 and remise >= 0 and total_ligne >= 0)
);

create index if not exists ix_vente_ligne_vente on vente_ligne (vente_id);

create table if not exists vente_lot (
    id uuid primary key default gen_random_uuid(),
    vente_ligne_id uuid not null references vente_ligne(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    lot_id uuid not null references lot_stock(id) on delete restrict,
    quantite integer not null,
    emplacement_id uuid not null references emplacement(id) on delete restrict,
    constraint ck_vente_lot_quantite check (quantite > 0)
);

create index if not exists ix_vente_lot_ligne on vente_lot (vente_ligne_id);

create table if not exists paiement_vente (
    id uuid primary key default gen_random_uuid(),
    vente_id uuid not null references vente(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    mode_paiement varchar(30) not null, -- ESPECES, MOBILE_MONEY_MTN, MOBILE_MONEY_ORANGE, VIREMENT, CHEQUE, TIERS_PAYANT, AVOIR
    montant numeric(19,4) not null,
    reference text null,
    created_at timestamptz not null default now(),
    constraint ck_paiement_montant check (montant >= 0)
);

create index if not exists ix_paiement_vente_vente on paiement_vente (vente_id);

