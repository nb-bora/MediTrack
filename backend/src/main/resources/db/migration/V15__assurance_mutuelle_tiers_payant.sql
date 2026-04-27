-- V15 — Module H : Assurance / Mutuelle (Tiers payant)

create table if not exists organisme_assurance (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    code varchar(40) not null,
    nom varchar(180) not null,
    type varchar(60) not null, -- ex: "ASSURANCE_MALADIE_OBLIGATOIRE"
    frequence_facturation varchar(20) not null default 'MENSUELLE', -- MENSUELLE, HEBDOMADAIRE, QUINZAINE, TRIMESTRIELLE
    delai_paiement_jours integer not null default 45,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ck_org_assurance_facturation check (frequence_facturation in ('MENSUELLE','HEBDOMADAIRE','QUINZAINE','TRIMESTRIELLE'))
);

create unique index if not exists ux_org_assurance_org_code on organisme_assurance (organisation_id, code);
create index if not exists ix_org_assurance_org_nom on organisme_assurance (organisation_id, nom);

create table if not exists organisme_couverture (
    id uuid primary key default gen_random_uuid(),
    organisme_id uuid not null references organisme_assurance(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    taux_generique numeric(5,2) not null default 0,
    taux_marque numeric(5,2) not null default 0,
    taux_parapharma numeric(5,2) not null default 0,
    taux_stupefiants numeric(5,2) not null default 0,
    plafond_journalier numeric(19,4) null,
    plafond_mensuel numeric(19,4) null,
    plafond_annuel numeric(19,4) null,
    piece_ordonnance_originale boolean not null default true,
    piece_carte_adherent boolean not null default true,
    piece_bon_prise_en_charge boolean not null default false,
    piece_examens boolean not null default false
);

create unique index if not exists ux_org_couverture_unique on organisme_couverture (organisme_id);

create table if not exists dossier_tiers_payant (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    organisme_id uuid not null references organisme_assurance(id) on delete restrict,
    patient_id uuid not null references patient(id) on delete restrict,
    vente_id uuid null references vente(id) on delete set null,
    ordonnance_id uuid null references ordonnance(id) on delete set null,
    numero_dossier varchar(40) not null,
    statut varchar(30) not null default 'BROUILLON',
    taux_couverture numeric(5,2) not null,
    montant_total numeric(19,4) not null default 0,
    montant_prise_en_charge numeric(19,4) not null default 0,
    montant_reste_patient numeric(19,4) not null default 0,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    soumis_at timestamptz null,
    soumis_by uuid null references utilisateur(id) on delete set null,
    rejete_at timestamptz null,
    rejete_by uuid null references utilisateur(id) on delete set null,
    motif_rejet text null,
    resoumis_at timestamptz null,
    paye_at timestamptz null,
    reference_paiement text null,
    constraint ck_dossier_tp_statut check (statut in ('BROUILLON','SOUMIS','REJETE','RESOUMIS','PAYE','ANNULE'))
);

create unique index if not exists ux_dossier_tp_org_numero on dossier_tiers_payant (organisation_id, numero_dossier);
create index if not exists ix_dossier_tp_org_statut on dossier_tiers_payant (organisation_id, statut, created_at desc);

