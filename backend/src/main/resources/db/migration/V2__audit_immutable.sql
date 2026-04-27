-- V2 — Audit immuable : interdire UPDATE/DELETE sur evenement_audit
--
-- Principe fondamental du système : personne (même l’administrateur) ne peut modifier
-- ou supprimer une entrée d’audit.

create or replace function trg_evenement_audit_immutable()
returns trigger
language plpgsql
as $$
begin
  raise exception 'evenement_audit est immuable (opération % interdite)', tg_op
    using errcode = '42501';
end;
$$;

drop trigger if exists evenement_audit_no_update on evenement_audit;
create trigger evenement_audit_no_update
before update on evenement_audit
for each row
execute function trg_evenement_audit_immutable();

drop trigger if exists evenement_audit_no_delete on evenement_audit;
create trigger evenement_audit_no_delete
before delete on evenement_audit
for each row
execute function trg_evenement_audit_immutable();

