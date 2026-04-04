package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade que armazena os dados de prospecção de uma ficha.
 */
@Entity
@Table(name = "ficha_prospeccao", indexes = {
    @Index(name = "idx_ficha_prospeccao_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_prospeccao_config", columnList = "dado_prospeccao_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_prospeccao", columnNames = {"ficha_id", "dado_prospeccao_config_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaProspeccao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaProspeccao.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaProspeccao.DADO_CONFIG_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dado_prospeccao_config_id", nullable = false)
    private DadoProspeccaoConfig dadoProspeccaoConfig;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaProspeccao.QUANTIDADE_MINIMA)
    @Builder.Default
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 0;

    /**
     * Dado de prospecção disponível por efeito de VantagemConfig (tipo DADO_UP).
     * Null quando nenhuma vantagem ativa concede dado extra.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dado_disponivel_id")
    private DadoProspeccaoConfig dadoDisponivel;
}
