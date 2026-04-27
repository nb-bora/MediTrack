-- V12 — Module F : Retours/annulations partiels + remises/arrondi

alter table vente add column if not exists arrondi numeric(19,4) not null default 0;
alter table vente add constraint ck_vente_arrondi_non_negatif check (arrondi >= 0);

create table if not exists retour_vente (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    vente_id uuid not null references vente(id) on delete restrict,
    numero_retour varchar(40) not null,
    motif text not null,
    mode_remboursement varchar(30) not null, -- ESPECES, MOBILE_MONEY_MTN, MOBILE_MONEY_ORANGE, VIREMENT, CHEQUE, AVOIR
    reference text null,
    montant_rembourse numeric(19,4) not null default 0,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    constraint ck_retour_vente_montant_non_negatif check (montant_rembourse >= 0)
);

create unique index if not exists ux_retour_vente_org_numero on retour_vente (organisation_id, numero_retour);
create index if not exists ix_retour_vente_org_created_at on retour_vente (organisation_id, created_at desc);
create index if not exists ix_retour_vente_vente on retour_vente (vente_id);

create table if not exists retour_vente_ligne (
    id uuid primary key default gen_random_uuid(),
    retour_vente_id uuid not null references retour_vente(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    vente_ligne_id uuid not null references vente_ligne(id) on delete restrict,
    lot_id uuid not null references lot_stock(id) on delete restrict,
    emplacement_id uuid not null references emplacement(id) on delete restrict,
    quantite integer not null,
    montant_ligne numeric(19,4) not null,
    constraint ck_retour_vente_ligne_quantite check (quantite > 0),
    constraint ck_retour_vente_ligne_montant check (montant_ligne >= 0)
);

create index if not exists ix_retour_vente_ligne_retour on retour_vente_ligne (retour_vente_id);
create index if not exists ix_retour_vente_ligne_lot on retour_vente_ligne (lot_id);

