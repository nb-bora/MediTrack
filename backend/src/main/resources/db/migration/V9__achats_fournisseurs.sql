-- V9 — Module E : Achats & Fournisseurs
--
-- Tables : fournisseur, bon_commande, bon_commande_ligne, reception_fournisseur, reception_fournisseur_ligne,
--          retour_fournisseur, retour_fournisseur_ligne.

create table if not exists fournisseur (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    raison_sociale text not null,
    numero_rc varchar(80) null,
    numero_contribuable varchar(80) null,
    adresse text null,
    contact_nom text null,
    contact_telephone varchar(30) null,
    email_commandes varchar(120) null,
    mode_paiement_prefere text null,
    actif boolean not null default true,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    updated_at timestamptz not null default now(),
    updated_by uuid null references utilisateur(id) on delete set null
);

create index if not exists ix_fournisseur_org on fournisseur (organisation_id);
create unique index if not exists ux_fournisseur_org_raison_sociale on fournisseur (organisation_id, raison_sociale);
create unique index if not exists ux_fournisseur_org_rc on fournisseur (organisation_id, numero_rc) where numero_rc is not null;
create unique index if not exists ux_fournisseur_org_contribuable on fournisseur (organisation_id, numero_contribuable) where numero_contribuable is not null;

create table if not exists bon_commande (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    numero varchar(40) not null,
    fournisseur_id uuid not null references fournisseur(id) on delete restrict,
    statut varchar(20) not null default 'BROUILLON', -- BROUILLON, VALIDE, RECU_PARTIEL, RECU_COMPLET, ANNULE
    date_commande date not null default current_date,
    date_livraison_prevue date null,
    commentaire text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    validated_at timestamptz null,
    validated_by uuid null references utilisateur(id) on delete set null,
    constraint ck_bon_commande_statut check (statut in ('BROUILLON','VALIDE','RECU_PARTIEL','RECU_COMPLET','ANNULE'))
);

create unique index if not exists ux_bon_commande_org_numero on bon_commande (organisation_id, numero);
create index if not exists ix_bon_commande_org_statut on bon_commande (organisation_id, statut);

create table if not exists bon_commande_ligne (
    id uuid primary key default gen_random_uuid(),
    bon_commande_id uuid not null references bon_commande(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    quantite_commandee integer not null,
    quantite_recue integer not null default 0,
    prix_attendu_unitaire numeric(19,4) null,
    devise varchar(3) not null default 'XAF',
    constraint ck_bc_ligne_qte_non_neg check (quantite_commandee > 0 and quantite_recue >= 0)
);

create unique index if not exists ux_bc_ligne_unique on bon_commande_ligne (bon_commande_id, produit_id);

create table if not exists reception_fournisseur (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    bon_commande_id uuid null references bon_commande(id) on delete set null,
    fournisseur_id uuid not null references fournisseur(id) on delete restrict,
    reference_document text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null
);

create index if not exists ix_reception_org on reception_fournisseur (organisation_id, created_at desc);
create index if not exists ix_reception_bc on reception_fournisseur (bon_commande_id);

create table if not exists reception_fournisseur_ligne (
    id uuid primary key default gen_random_uuid(),
    reception_id uuid not null references reception_fournisseur(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    produit_id uuid not null references produit(id) on delete restrict,
    emplacement_destination_id uuid not null references emplacement(id) on delete restrict,
    numero_lot text not null,
    date_peremption date not null,
    quantite_recue integer not null,
    prix_facture_unitaire numeric(19,4) null,
    devise varchar(3) not null default 'XAF',
    temperature_transport_c numeric(6,2) null,
    constraint ck_reception_ligne_qte check (quantite_recue > 0)
);

create index if not exists ix_reception_ligne_reception on reception_fournisseur_ligne (reception_id);

create table if not exists retour_fournisseur (
    id uuid primary key default gen_random_uuid(),
    organisation_id uuid not null references organisation(id) on delete restrict,
    fournisseur_id uuid not null references fournisseur(id) on delete restrict,
    statut varchar(20) not null default 'BROUILLON', -- BROUILLON, VALIDE, ENVOYE, CLOTURE
    motif text not null,
    reference_document text null,
    created_at timestamptz not null default now(),
    created_by uuid null references utilisateur(id) on delete set null,
    validated_at timestamptz null,
    validated_by uuid null references utilisateur(id) on delete set null,
    constraint ck_retour_statut check (statut in ('BROUILLON','VALIDE','ENVOYE','CLOTURE'))
);

create index if not exists ix_retour_org on retour_fournisseur (organisation_id, created_at desc);

create table if not exists retour_fournisseur_ligne (
    id uuid primary key default gen_random_uuid(),
    retour_id uuid not null references retour_fournisseur(id) on delete cascade,
    organisation_id uuid not null references organisation(id) on delete restrict,
    lot_id uuid not null references lot_stock(id) on delete restrict,
    quantite integer not null,
    motif text null,
    constraint ck_retour_ligne_quantite check (quantite > 0)
);

create index if not exists ix_retour_ligne_retour on retour_fournisseur_ligne (retour_id);

