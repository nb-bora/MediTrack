-- V14 — Module G : Traçabilité dispensation (lot/emplacement) + alertes

alter table dispensation add column if not exists lot_id uuid null references lot_stock(id) on delete restrict;
alter table dispensation add column if not exists emplacement_id uuid null references emplacement(id) on delete restrict;

create index if not exists ix_dispensation_lot on dispensation (lot_id);

