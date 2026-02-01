package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade que armazena os dados de vida de uma ficha.
 */
@Entity
@Table(name = "ficha_vida", indexes = {
    @Index(name = "idx_ficha_vida_ficha", columnList = "ficha_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_vida", columnNames = {"ficha_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaVida.FICHA_OBRIGATORIA)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    @NotNull
    @Builder.Default
    @Column(name = "valor_vantagens", nullable = false)
    private Integer valorVantagens = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_outros", nullable = false)
    private Integer valorOutros = 0;

    /**
     * Calcula o valor total de vida.
     * Total = Vigor + Nível + Vantagens + Renascimentos + Outros
     * Nota: Vigor, Nível e Renascimentos vêm da ficha e atributos
     */
    public Integer getValorModificadores() {
        return (valorVantagens != null ? valorVantagens : 0) +
               (valorOutros != null ? valorOutros : 0);
    }
}
