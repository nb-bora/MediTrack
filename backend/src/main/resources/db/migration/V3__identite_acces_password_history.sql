-- V3 — Identité & accès : historique mots de passe + seed RBAC
--
-- Ajouts :
-- - Table password_history : empêche réutilisation des 5 derniers mots de passe
-- - Seed permissions et rôles standards

create table if not exists password_history (
    id uuid primary key default gen_random_uuid(),
    utilisateur_id uuid not null references utilisateur(id) on delete cascade,
    password_hash text not null,
    created_at timestamptz not null default now()
);

create index if not exists ix_password_history_utilisateur_created_at on password_history (utilisateur_id, created_at desc);

-- Seed permissions (globales, indépendantes de l’organisation)
insert into permission (code, nom, description)
values
  ('PRODUIT_CREER', 'Créer produit', 'Créer un produit dans le catalogue'),
  ('PRIX_MODIFIER', 'Modifier prix', 'Créer une nouvelle ligne de prix (historisation)'),
  ('VENTE_EFFECTUER', 'Vendre', 'Créer et finaliser une vente'),
  ('ORDONNANCE_VALIDER', 'Valider ordonnance', 'Valider/refuser une ordonnance'),
  ('STOCK_RECEPTIONNER', 'Réceptionner stock', 'Enregistrer une réception fournisseur'),
  ('STOCK_INVENTAIRE', 'Inventaire', 'Lancer/compter/valider un inventaire'),
  ('CAISSE_CLOTURER', 'Clôture caisse', 'Clôturer une session caisse'),
  ('COMPTA_EXPORTER', 'Exporter comptabilité', 'Exporter les données comptables'),
  ('UTILISATEUR_GERER', 'Gérer utilisateurs', 'Créer/modifier/désactiver des comptes'),
  ('AUDIT_VOIR', 'Voir journal audit', 'Consulter le journal d’audit'),
  ('LOT_PERIME_OVERRIDE', 'Override lot périmé', 'Autoriser une action exceptionnelle sur périmé (avec motif)'),
  ('VENTE_ANNULER', 'Annuler vente', 'Annuler une vente (avec motif)'),
  ('ACHAT_COUT_VOIR', "Voir coût d'achat", "Voir les coûts d'achat"),
  ('MARGE_VOIR', 'Voir marge bénéficiaire', 'Voir les marges'),
  ('SAUVEGARDE_GERER', 'Gérer sauvegardes', 'Configurer/lancer des sauvegardes')
on conflict (code) do nothing;

