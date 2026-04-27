-- V13 — Module G : Ordonnances & Dossier patient

create table if not exists patient (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    nom varchar(120) not null,
    prenom varchar(120) not null,
    date_naissance date null,
    sexe varchar(20) null, -- MASCULIN, FEMININ, AUTRE
    telephone varchar(30) null,
    adresse text null,
    -- "Résumé assurance" (visible caissier) — le détail Module H viendra après
    assurance_organisme_nom varchar(120) null,
    assurance_numero_adherent varchar(60) null,
    assurance_taux_couverture numeric(5,2) null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ck_patient_sexe check (sexe is null or sexe in ('MASCULIN','FEMININ','AUTRE'))
);

create unique index if not exists ux_patient_org_tel on patient (organisation_id, telephone) where telephone is not null;
create index if not exists ix_patient_org_nom_prenom on patient (organisation_id, nom, prenom);

create table if not exists patient_medical (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    patient_id uuid not null references patient(id) on delete cascade,
    allergies text null,
    pathologies_chroniques text null,
    medecin_traitant text null,
    updated_at timestamptz not null default now(),
    updated_by uuid null references utilisateur(id) on delete set null
);

create unique index if not exists ux_patient_medical_patient on patient_medical (patient_id);

create table if not exists prescripteur (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    nom varchar(180) not null,
    structure varchar(180) null,
    telephone varchar(30) null,
    created_at timestamptz not null default now()
);

create index if not exists ix_prescripteur_org_nom on prescripteur (organisation_id, nom);

create table if not exists ordonnance (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    patient_id uuid not null references patient(id) on delete restrict,
    prescripteur_id uuid null references prescripteur(id) on delete set null,
    date_prescription date not null,
    date_expiration date not null,
    statut varchar(30) not null default 'EN_ATTENTE_VALIDATION',
    ordonnance_parent_id uuid null references ordonnance(id) on delete set null,
    motif_refus text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    validated_at timestamptz null,
    validated_by uuid null references utilisateur(id) on delete set null,
    constraint ck_ordonnance_statut check (statut in ('EN_ATTENTE_VALIDATION','VALIDEE','PARTIELLEMENT_DISPENSEE','DISPENSEE','REFUSEE','ANNULEE','EXPIREE'))
);

create index if not exists ix_ordonnance_org_patient_created on ordonnance (organisation_id, patient_id, created_at desc);
create index if not exists ix_ordonnance_org_statut_exp on ordonnance (organisation_id, statut, date_expiration);

create table if not exists ordonnance_piece (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    ordonnance_id uuid not null references ordonnance(id) on delete cascade,
    fichier_nom text not null,
    contenu_type varchar(120) null,
    storage_key text not null,
    created_at timestamptz not null default now()
);

create index if not exists ix_ordonnance_piece_ordonnance on ordonnance_piece (ordonnance_id);

create table if not exists ordonnance_ligne (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    ordonnance_id uuid not null references ordonnance(id) on delete cascade,
    produit_id uuid not null references produit(id) on delete restrict,
    quantite_prescrite integer not null,
    posologie text null,
    duree_jours integer null,
    quantite_dispensee integer not null default 0,
    constraint ck_ord_ligne_qte check (quantite_prescrite > 0 and quantite_dispensee >= 0 and quantite_dispensee <= quantite_prescrite)
);

create unique index if not exists ux_ordonnance_ligne_unique on ordonnance_ligne (ordonnance_id, produit_id);

create table if not exists dispensation (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    ordonnance_id uuid not null references ordonnance(id) on delete restrict,
    ordonnance_ligne_id uuid not null references ordonnance_ligne(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    quantite integer not null,
    cree_par uuid null references utilisateur(id) on delete set null,
    created_at timestamptz not null default now(),
    motif_override text null,
    constraint ck_dispensation_qte check (quantite > 0)
);

create index if not exists ix_dispensation_ordonnance on dispensation (ordonnance_id);

