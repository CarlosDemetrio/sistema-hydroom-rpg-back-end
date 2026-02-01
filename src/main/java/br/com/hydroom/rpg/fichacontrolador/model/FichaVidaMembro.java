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
import org.hibernate.envers.Audited;

/**
 * Entidade que armazena os danos nos membros do corpo de uma ficha.
 */
@Entity
@Table(name = "ficha_vida_membros", indexes = {
    @Index(name = "idx_ficha_vida_membro_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_vida_membro_config", columnList = "membro_corpo_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_vida_membro", columnNames = {"ficha_id", "membro_corpo_config_id"})
})
@Audited
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVidaMembro extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaVidaMembro.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaVidaMembro.MEMBRO_CONFIG_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_corpo_config_id", nullable = false)
    private MembroCorpoConfig membroCorpoConfig;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaVidaMembro.DANO_MINIMO)
    @Builder.Default
    @Column(name = "dano_recebido", nullable = false)
    private Integer danoRecebido = 0;

    /**
     * Calcula a vida restante do membro.
     * Restante = VidaBase - DanoRecebido
     * VidaBase = VidaTotal da Ficha * Porcentagem do Membro
     */
    public Integer calcularVidaRestante(Integer vidaTotal) {
        if (vidaTotal == null || membroCorpoConfig == null) {
            return 0;
        }

        double vidaBase = vidaTotal * membroCorpoConfig.getPorcentagemVida().doubleValue();
        int dano = danoRecebido != null ? danoRecebido : 0;

        return Math.max(0, (int) vidaBase - dano);
    }
}
