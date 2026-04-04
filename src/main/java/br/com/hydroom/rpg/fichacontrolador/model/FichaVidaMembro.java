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
import org.hibernate.annotations.SQLRestriction;

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
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVidaMembro extends BaseEntity {

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

    /**
     * Vida calculada: floor(vidaTotal × porcentagem).
     */
    @NotNull
    @Builder.Default
    @Column(name = "vida", nullable = false)
    private Integer vida = 0;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaVidaMembro.DANO_MINIMO)
    @Builder.Default
    @Column(name = "dano_recebido", nullable = false)
    private Integer danoRecebido = 0;

    /**
     * Bônus de vida no membro vindos de efeitos de VantagemConfig (tipo BONUS_VIDA_MEMBRO).
     * Acumula sobre a vida base calculada pela porcentagem, sem alterar o pool global.
     */
    @NotNull
    @Builder.Default
    @Column(name = "bonus_vantagens", nullable = false)
    private Integer bonusVantagens = 0;

    /**
     * Calcula a vida restante do membro.
     */
    public Integer calcularVidaRestante() {
        int v = vida != null ? vida : 0;
        int dano = danoRecebido != null ? danoRecebido : 0;
        return Math.max(0, v - dano);
    }
}
