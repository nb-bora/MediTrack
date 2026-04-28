package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
public class UtilisateurJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "login", nullable = false, length = 80)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "doit_changer_mdp", nullable = false)
    private boolean doitChangerMdp;

    @Column(name = "mdp_expires_at")
    private Instant mdpExpiresAt;

    @Column(name = "tentatives_echec", nullable = false)
    private int tentativesEchec;

    @Column(name = "verrouille_jusqua")
    private Instant verrouilleJusqua;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UtilisateurJpaEntity() {
    }

    public static UtilisateurJpaEntity create(UUID id, UUID organisationId, String nom, String prenom,
                                              String email, String telephone, String login, String passwordHash,
                                              Instant mdpExpiresAt, Instant now) {
        UtilisateurJpaEntity u = new UtilisateurJpaEntity();
        u.id = id;
        u.organisationId = organisationId;
        u.nom = nom;
        u.prenom = prenom;
        u.email = email;
        u.telephone = telephone;
        u.login = login;
        u.passwordHash = passwordHash;
        u.actif = true;
        u.doitChangerMdp = true;
        u.mdpExpiresAt = mdpExpiresAt;
        u.tentativesEchec = 0;
        u.verrouilleJusqua = null;
        u.createdAt = now;
        u.updatedAt = now;
        return u;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActif() {
        return actif;
    }

    public boolean isDoitChangerMdp() {
        return doitChangerMdp;
    }

    public Instant getMdpExpiresAt() {
        return mdpExpiresAt;
    }

    public int getTentativesEchec() {
        return tentativesEchec;
    }

    public Instant getVerrouilleJusqua() {
        return verrouilleJusqua;
    }

    public void setTentativesEchec(int tentativesEchec) {
        this.tentativesEchec = tentativesEchec;
    }

    public void setVerrouilleJusqua(Instant verrouilleJusqua) {
        this.verrouilleJusqua = verrouilleJusqua;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setDoitChangerMdp(boolean doitChangerMdp) {
        this.doitChangerMdp = doitChangerMdp;
    }

    public void setMdpExpiresAt(Instant mdpExpiresAt) {
        this.mdpExpiresAt = mdpExpiresAt;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void resetLockout() {
        this.tentativesEchec = 0;
        this.verrouilleJusqua = null;
    }

    public void touch(Instant now) {
        this.updatedAt = now;
    }
}

