package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Entidade customizada de revisão para Hibernate Envers.
 * Armazena informações adicionais sobre cada revisão: usuário e IP.
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(CustomRevisionListener.class)
@Data
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    private Long id;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private Long timestamp;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "ip_origem", length = 50)
    private String ipOrigem;
}
