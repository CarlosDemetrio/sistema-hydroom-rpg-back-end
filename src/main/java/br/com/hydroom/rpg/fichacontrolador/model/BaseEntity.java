package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * Classe base para entidades com soft delete e auditoria completa.
 *
 * <p>Soft delete via campo deleted_at (null = ativo, preenchido = deletado).</p>
 * <p>Auditoria automática de timestamps (createdAt, updatedAt) e usuários (createdBy, updatedBy).</p>
 *
 * <p>Todos os campos de auditoria são preenchidos automaticamente via @PrePersist e @PreUpdate.</p>
 *
 * <p>@SQLRestriction garante que apenas registros não deletados sejam retornados automaticamente.</p>
 */
@Getter
@Setter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false, length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

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

        String currentUser = getCurrentUsername();
        this.createdBy = currentUser;
        this.updatedBy = currentUser;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = getCurrentUsername();
    }

    /**
     * Obtém o username do usuário autenticado atual.
     * Retorna "system" se não houver usuário autenticado.
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Log silenciosamente e retorna "system"
        }
        return "system";
    }
}
