package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_auth")
public class SessionAuthJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Column(name = "revokee_le")
    private Instant revokeeLe;

    @Column(name = "poste_nom")
    private String posteNom;

    @Column(name = "adresse_ip")
    private String adresseIp;

    protected SessionAuthJpaEntity() {
    }

    public static SessionAuthJpaEntity create(UUID id, UUID utilisateurId, Instant now, Instant expireLe, String refreshTokenHash,
                                              String posteNom, String adresseIp) {
        SessionAuthJpaEntity s = new SessionAuthJpaEntity();
        s.id = id;
        s.utilisateurId = utilisateurId;
        s.creeLe = now;
        s.expireLe = expireLe;
        s.refreshTokenHash = refreshTokenHash;
        s.revokeeLe = null;
        s.posteNom = posteNom;
        s.adresseIp = adresseIp;
        return s;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUtilisateurId() {
        return utilisateurId;
    }

    public Instant getExpireLe() {
        return expireLe;
    }

    public Instant getRevokeeLe() {
        return revokeeLe;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void rotate(String newHash, Instant newExpire, String poste, String ip) {
        this.refreshTokenHash = newHash;
        this.expireLe = newExpire;
        this.posteNom = poste;
        this.adresseIp = ip;
    }

    public void revoke(Instant now) {
        if (this.revokeeLe == null) {
            this.revokeeLe = now;
        }
    }
}

