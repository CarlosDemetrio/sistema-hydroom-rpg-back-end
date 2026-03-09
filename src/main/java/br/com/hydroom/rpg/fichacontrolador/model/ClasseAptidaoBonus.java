package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Bônus fixo que uma classe de personagem concede em uma AptidaoConfig.
 * Ex: Ladrão +2 em Furtividade.
 */
@Entity
@Table(name = "classe_aptidao_bonus", uniqueConstraints = {
    @UniqueConstraint(name = "uk_classe_aptidao_bonus", columnNames = {"classe_id", "aptidao_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseAptidaoBonus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_id", nullable = false)
    private AptidaoConfig aptidao;

    /** Bônus fixo na aptidão (não negativo). */
    @NotNull
    @Min(0)
    @Column(name = "bonus", nullable = false)
    private Integer bonus;
}
