-- V17 — POS : Liaison Vente ↔ Tiers payant (Module H)

alter table vente add column if not exists patient_id uuid null references patient(id) on delete set null;
alter table vente add column if not exists ordonnance_id uuid null references ordonnance(id) on delete set null;
alter table vente add column if not exists organisme_id uuid null references organisme_assurance(id) on delete set null;
alter table vente add column if not exists numero_adherent text null;

create index if not exists ix_vente_org_patient on vente (organisation_id, patient_id);
create index if not exists ix_vente_org_ordonnance on vente (organisation_id, ordonnance_id);

