package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.model.enums.OrigemVantagem;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade que armazena as vantagens de uma ficha (compradas ou concedidas pelo Mestre).
 */
@Entity
@Table(name = "ficha_vantagens", indexes = {
    @Index(name = "idx_ficha_vantagem_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_vantagem_config", columnList = "vantagem_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_vantagem", columnNames = {"ficha_id", "vantagem_config_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVantagem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaVantagem.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaVantagem.VANTAGEM_CONFIG_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;

    @NotNull
    @Min(value = 1, message = ValidationMessages.FichaVantagem.NIVEL_MINIMO)
    @Builder.Default
    @Column(name = "nivel_atual", nullable = false)
    private Integer nivelAtual = 1;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaVantagem.CUSTO_MINIMO)
    @Column(name = "custo_pago", nullable = false)
    private Integer custoPago;

    /**
     * Indica se esta vantagem foi concedida diretamente pelo Mestre (Insolitus ou concessao manual).
     * Vantagens concedidas pelo Mestre nao custam pontos de vantagem.
     */
    @Builder.Default
    @Column(name = "concedido_pelo_mestre", nullable = false)
    private Boolean concedidoPeloMestre = false;

    /**
     * Origem da vantagem: JOGADOR (comprada com pontos), MESTRE (concedida manualmente),
     * ou SISTEMA (auto-concedida por ClasseVantagemPreDefinida ou RacaVantagemPreDefinida).
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "origem", nullable = false, length = 20)
    private OrigemVantagem origem = OrigemVantagem.JOGADOR;

    /**
     * Calcula se pode subir de nível.
     */
    public boolean podeSubirNivel() {
        if (vantagemConfig == null || nivelAtual == null) {
            return false;
        }

        Integer nivelMaximo = vantagemConfig.getNivelMaximo();
        return nivelMaximo == null || nivelAtual < nivelMaximo;
    }
}
