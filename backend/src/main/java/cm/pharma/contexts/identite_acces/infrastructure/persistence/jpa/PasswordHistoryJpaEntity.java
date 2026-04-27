package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_history")
public class PasswordHistoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PasswordHistoryJpaEntity() {
    }

    public static PasswordHistoryJpaEntity from(UUID id, UUID utilisateurId, String passwordHash, Instant at) {
        PasswordHistoryJpaEntity e = new PasswordHistoryJpaEntity();
        e.id = id;
        e.utilisateurId = utilisateurId;
        e.passwordHash = passwordHash;
        e.createdAt = at;
        return e;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}

