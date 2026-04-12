package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Bônus que uma classe de personagem concede em um BonusConfig específico por nível.
 * Ex: Guerreiro +1 B.B.A. por nível.
 */
@Entity
@Table(name = "classe_bonus", uniqueConstraints = {
    @UniqueConstraint(name = "uk_classe_bonus_classe_bonus", columnNames = {"classe_id", "bonus_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "classe")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseBonus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id", nullable = false)
    private BonusConfig bonus;

    /** Valor acrescido por nível. Pode ser fracionário (ex: 0.5 por nível). */
    @NotNull
    @Column(name = "valor_por_nivel", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPorNivel;
}
