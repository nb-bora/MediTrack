-- V16 — Module H : Pièces dossier tiers payant + stats rejets

create table if not exists dossier_tiers_payant_piece (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    dossier_id uuid not null references dossier_tiers_payant(id) on delete cascade,
    type_piece varchar(40) not null, -- ORDONNANCE, CARTE_ADHERENT, BON_PRISE_EN_CHARGE, EXAMENS, AUTRE
    fichier_nom text not null,
    contenu_type varchar(120) null,
    storage_key text not null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    constraint ck_dossier_tp_piece_type check (type_piece in ('ORDONNANCE','CARTE_ADHERENT','BON_PRISE_EN_CHARGE','EXAMENS','AUTRE'))
);

create index if not exists ix_dossier_tp_piece_dossier on dossier_tiers_payant_piece (dossier_id);
create index if not exists ix_dossier_tp_piece_org_type on dossier_tiers_payant_piece (organisation_id, type_piece);

