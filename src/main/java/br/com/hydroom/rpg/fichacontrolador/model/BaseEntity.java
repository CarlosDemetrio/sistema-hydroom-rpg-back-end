package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe base para entidades com soft delete.
 * Registros nunca são deletados fisicamente, apenas marcados como inativos.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    /**
     * Marca o registro como inativo (soft delete).
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Reativa um registro previamente desativado.
     */
    public void ativar() {
        this.ativo = true;
    }
}
