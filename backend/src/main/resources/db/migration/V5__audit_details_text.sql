-- V5 — Rendre "details" portable (jsonb -> text)
--
-- Objectif : permettre un mapping JPA simple (String/Lob) et réduire la dépendance PostgreSQL.
-- Sur PostgreSQL : conversion jsonb -> text. Sur d'autres SGBD, une migration dédiée sera nécessaire.

-- Supprime l'index GIN (PostgreSQL-specific) s'il existe
drop index if exists gin_evenement_audit_details;

-- Convertit la colonne details en TEXT (PostgreSQL)
alter table evenement_audit
    alter column details type text
    using details::text;

