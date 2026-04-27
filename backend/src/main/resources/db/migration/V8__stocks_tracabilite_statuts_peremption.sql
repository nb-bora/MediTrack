-- V8 — Module D : statuts de péremption (signalement J-30 + progression)

-- Étend la liste des statuts autorisés pour supporter les statuts "temps réel".
alter table lot_stock drop constraint if exists ck_lot_stock_statut;

alter table lot_stock add constraint ck_lot_stock_statut check (
    statut in (
        'ACTIF',
        'PEREMPTION_PRECOCE',
        'PEREMPTION_URGENTE',
        'PERIME_AUTOMATIQUE',
        'QUARANTAINE',
        'RAPPELE',
        'DETRUIT',
        'RETOUR_FOURNISSEUR'
    )
);

