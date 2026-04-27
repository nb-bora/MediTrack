-- V1 — Schéma initial minimal (organisationnel + RBAC + audit)
--
-- Objectif : poser des tables fondations, auditées et cohérentes,
-- sur lesquelles les bounded contexts pourront construire les modules.
--
-- Notes PostgreSQL :
-- - Extensions requises : pgcrypto (gen_random_uuid)
-- - Tous les identifiants sont en UUID.
-- - L'audit immuable (evenement_audit) sera renforcé par trigger en V2.

create extension if not exists pgcrypto;

-- ============
-- Organisation
-- ============
create table if not exists organisation (
    id uuid primary key default gen_random_uuid(),
    nom_commercial text not null,
    numero_autorisation_ouverture varchar(50) not null,
    adresse text not null,
    telephone varchar(30) not null,
    email varchar(120) null,
    responsable_legal_nom text not null,
    responsable_legal_numero_ordre varchar(50) null,
    devise varchar(3) not null default 'XAF',
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create unique index if not exists ux_organisation_numero_autorisation on organisation (numero_autorisation_ouverture);

create table if not exists site (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    nom text not null,
    adresse text not null,
    telephone varchar(30) null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create index if not exists ix_site_organisation_id on site (organisation_id);
create unique index if not exists ux_site_organisation_nom on site (organisation_id, nom);

create table if not exists emplacement (
    id uuid primary key default gen_random_uuid(),
    site_id uuid not null references site(id) on delete restrict,
    code varchar(30) not null,
    nom text not null,
    type_emplacement varchar(30) not null,
    ordre_affichage integer not null default 0,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_emplacement_ordre_affichage_non_negatif check (ordre_affichage >= 0)
);

create index if not exists ix_emplacement_site_id on emplacement (site_id);
create unique index if not exists ux_emplacement_site_code on emplacement (site_id, code);

-- =========================
-- Paramètres / numérotation
-- =========================
create table if not exists parametre (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    cle varchar(120) not null,
    valeur text not null,
    type_valeur varchar(20) not null,
    description text null,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create index if not exists ix_parametre_organisation_id on parametre (organisation_id);
create index if not exists ix_parametre_cle on parametre (cle);
create unique index if not exists ux_parametre_organisation_cle on parametre (organisation_id, cle);

create table if not exists sequence_numerotation (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    type_document varchar(30) not null,
    format text not null,
    compteur_courant integer not null default 0,
    reset_frequence varchar(10) not null default 'ANNUEL',
    reset_dernier timestamptz null,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_sequence_compteur_non_negatif check (compteur_courant >= 0)
);

create index if not exists ix_sequence_organisation_id on sequence_numerotation (organisation_id);
create unique index if not exists ux_sequence_organisation_type_document on sequence_numerotation (organisation_id, type_document);

-- ====
-- RBAC
-- ====
create table if not exists utilisateur (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    nom text not null,
    prenom text not null,
    email varchar(120) null,
    telephone varchar(30) null,
    login varchar(80) not null,
    password_hash text not null,
    actif boolean not null default true,
    doit_changer_mdp boolean not null default true,
    mdp_expires_at timestamptz null,
    tentatives_echec integer not null default 0,
    verrouille_jusqua timestamptz null,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null,
    constraint ck_utilisateur_tentatives_echec_non_negatif check (tentatives_echec >= 0)
);

create index if not exists ix_utilisateur_organisation_id on utilisateur (organisation_id);
create index if not exists ix_utilisateur_login on utilisateur (login);
create unique index if not exists ux_utilisateur_organisation_login on utilisateur (organisation_id, login);
create unique index if not exists ux_utilisateur_organisation_email on utilisateur (organisation_id, email) where email is not null;

create table if not exists role (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    code varchar(30) not null,
    nom text not null,
    description text null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create unique index if not exists ux_role_organisation_code on role (organisation_id, code);

create table if not exists permission (
    id uuid primary key default gen_random_uuid(),
    code varchar(80) not null,
    nom text not null,
    description text null,
    created_at timestamptz not null default now(),
    created_by uuid null,
    updated_at timestamptz not null default now(),
    updated_by uuid null
);

create unique index if not exists ux_permission_code on permission (code);

create table if not exists role_permission (
    role_id uuid not null references role(id) on delete cascade,
    permission_id uuid not null references permission(id) on delete restrict,
    primary key (role_id, permission_id)
);

create index if not exists ix_role_permission_permission_id on role_permission (permission_id);

create table if not exists utilisateur_role (
    utilisateur_id uuid not null references utilisateur(id) on delete cascade,
    role_id uuid not null references role(id) on delete restrict,
    primary key (utilisateur_id, role_id)
);

-- ======================
-- Sessions / refresh JWT
-- ======================
create table if not exists session_auth (
    id uuid primary key default gen_random_uuid(),
    utilisateur_id uuid not null references utilisateur(id) on delete restrict,
    cree_le timestamptz not null default now(),
    expire_le timestamptz not null,
    refresh_token_hash text not null,
    revokee_le timestamptz null,
    poste_nom text null,
    adresse_ip text null
);

create index if not exists ix_session_auth_utilisateur_id on session_auth (utilisateur_id);
create index if not exists ix_session_auth_expire_le on session_auth (expire_le);

-- ============
-- Audit métier
-- ============
create table if not exists evenement_audit (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    horodatage timestamptz not null default now(),
    utilisateur_id uuid null references utilisateur(id) on delete set null,
    utilisateur_nom text null,
    utilisateur_role text null,
    poste text null,
    adresse_ip text null,
    action varchar(60) not null,
    entite varchar(60) null,
    entite_id text null,
    motif text null,
    details jsonb not null default '{}'::jsonb
);

create index if not exists ix_evenement_audit_organisation_horodatage on evenement_audit (organisation_id, horodatage desc);
create index if not exists ix_evenement_audit_action on evenement_audit (action);
create index if not exists ix_evenement_audit_entite_entite_id on evenement_audit (entite, entite_id);
create index if not exists gin_evenement_audit_details on evenement_audit using gin (details);

