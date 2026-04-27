package cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evenement_audit")
public class EvenementAuditJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "horodatage", nullable = false)
    private Instant horodatage;

    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @Column(name = "utilisateur_nom")
    private String utilisateurNom;

    @Column(name = "utilisateur_role")
    private String utilisateurRole;

    @Column(name = "poste")
    private String poste;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "entite", length = 60)
    private String entite;

    @Column(name = "entite_id")
    private String entiteId;

    @Column(name = "motif")
    private String motif;

    @Lob
    @Column(name = "details", nullable = false)
    private String details;

    protected EvenementAuditJpaEntity() {
    }

    public static EvenementAuditJpaEntity from(UUID id, UUID organisationId, Instant at, UUID utilisateurId,
                                               String utilisateurNom, String utilisateurRole, String poste, String ip,
                                               String action, String entite, String entiteId, String motif,
                                               String details) {
        EvenementAuditJpaEntity e = new EvenementAuditJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.horodatage = at;
        e.utilisateurId = utilisateurId;
        e.utilisateurNom = utilisateurNom;
        e.utilisateurRole = utilisateurRole;
        e.poste = poste;
        e.adresseIp = ip;
        e.action = action;
        e.entite = entite;
        e.entiteId = entiteId;
        e.motif = motif;
        e.details = details == null || details.isBlank() ? "{}" : details;
        return e;
    }
}

