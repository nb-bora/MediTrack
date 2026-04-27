-- V4 — Identité & accès : mapping rôle -> permissions (selon matrice Module B.2)
--
-- Note : les rôles sont par organisation. Les permissions sont globales (table permission).
-- Ce script assigne les permissions aux rôles existants, pour toute organisation.

-- Helper : assigne une permission à un rôle (idempotent)
-- (On utilise INSERT ... SELECT ... WHERE EXISTS)

-- ADMIN : toutes permissions listées
insert into role_permission(role_id, permission_id)
select r.id, p.id
from role r
join permission p on p.code in (
  'PRODUIT_CREER','PRIX_MODIFIER','VENTE_EFFECTUER','ORDONNANCE_VALIDER','STOCK_RECEPTIONNER',
  'STOCK_INVENTAIRE','CAISSE_CLOTURER','COMPTA_EXPORTER','UTILISATEUR_GERER','AUDIT_VOIR',
  'LOT_PERIME_OVERRIDE','VENTE_ANNULER','ACHAT_COUT_VOIR','MARGE_VOIR','SAUVEGARDE_GERER'
)
where r.code = 'ADMIN'
on conflict do nothing;

-- PHARMACIEN
insert into role_permission(role_id, permission_id)
select r.id, p.id
from role r
join permission p on p.code in (
  'PRODUIT_CREER','VENTE_EFFECTUER','ORDONNANCE_VALIDER','STOCK_INVENTAIRE','LOT_PERIME_OVERRIDE',
  'VENTE_ANNULER','ACHAT_COUT_VOIR'
)
where r.code = 'PHARMACIEN'
on conflict do nothing;

-- CAISSIER
insert into role_permission(role_id, permission_id)
select r.id, p.id
from role r
join permission p on p.code in ('VENTE_EFFECTUER')
where r.code = 'CAISSIER'
on conflict do nothing;

-- MAGASINIER
insert into role_permission(role_id, permission_id)
select r.id, p.id
from role r
join permission p on p.code in ('STOCK_RECEPTIONNER','STOCK_INVENTAIRE','ACHAT_COUT_VOIR')
where r.code = 'MAGASINIER'
on conflict do nothing;

-- COMPTABLE
insert into role_permission(role_id, permission_id)
select r.id, p.id
from role r
join permission p on p.code in ('CAISSE_CLOTURER','COMPTA_EXPORTER','ACHAT_COUT_VOIR','MARGE_VOIR')
where r.code = 'COMPTABLE'
on conflict do nothing;

