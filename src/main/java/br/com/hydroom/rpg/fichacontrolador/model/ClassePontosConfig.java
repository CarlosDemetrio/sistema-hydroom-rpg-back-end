package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Configura pontos extras de atributo e vantagem concedidos por classe em determinado nível.
 *
 * <p>pontosAptidao AUSENTE: aptidões são independentes de classe/raça.
 * Pool de aptidão vem SOMENTE de NivelConfig.pontosAptidao (global).
 * Decisão PO 2026-04-04.</p>
 */
@Entity
@Table(name = "classe_pontos_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_pontos_nivel",
        columnNames = {"classe_personagem_id", "nivel"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassePontosConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_personagem_id", nullable = false)
    private ClassePersonagem classePersonagem;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @Column(name = "pontos_atributo", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosAtributo = 0;

    @Column(name = "pontos_vantagem", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosVantagem = 0;
}
