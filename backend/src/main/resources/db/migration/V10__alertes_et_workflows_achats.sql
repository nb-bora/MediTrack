-- V10 — Alertes (générique) + workflows achats (validation anomalies)

create table if not exists alerte (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    type_alerte varchar(40) not null,
    niveau varchar(20) not null default 'IMPORTANT', -- INFO, IMPORTANT, URGENT
    entite varchar(60) null,
    entite_id text null,
    message text not null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    resolved_at timestamptz null,
    resolved_by uuid null references utilisateur(id) on delete set null,
    resolution_message text null,
    constraint ck_alerte_niveau check (niveau in ('INFO','IMPORTANT','URGENT'))
);

create index if not exists ix_alerte_org_created_at on alerte (organisation_id, created_at desc);
create index if not exists ix_alerte_org_type on alerte (organisation_id, type_alerte);
create unique index if not exists ux_alerte_open_dedupe on alerte (organisation_id, type_alerte, entite, entite_id) where resolved_at is null;

alter table reception_fournisseur add column if not exists statut_validation_prix varchar(20) not null default 'OK';
alter table reception_fournisseur add constraint ck_reception_validation_prix check (statut_validation_prix in ('OK','EN_ATTENTE','VALIDE','REFUSE'));

