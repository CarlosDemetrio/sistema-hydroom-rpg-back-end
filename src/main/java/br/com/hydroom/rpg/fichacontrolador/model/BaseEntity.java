package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Classe base para entidades com soft delete e auditoria de timestamps.
 * Soft delete via campo deleted_at (null = ativo, preenchido = deletado).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Marca o registro como deletado (soft delete).
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura um registro previamente deletado.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Verifica se o registro foi deletado.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Verifica se o registro está ativo (não deletado).
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
