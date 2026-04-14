package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
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
 * Pré-requisito polimórfico de vantagem.
 *
 * <p>Suporta múltiplos tipos de pré-requisito via campo {@code tipo}:
 * <ul>
 *   <li>VANTAGEM — ter outra vantagem em nível mínimo</li>
 *   <li>RACA — ter uma raça específica</li>
 *   <li>CLASSE — ter uma classe específica</li>
 *   <li>ATRIBUTO — ter valorBase de atributo >= valorMinimo</li>
 *   <li>NIVEL — ter nível de personagem >= valorMinimo</li>
 *   <li>APTIDAO — ter valorBase de aptidão >= valorMinimo</li>
 * </ul>
 *
 * <p>Lógica de verificação no momento da compra:
 * <ul>
 *   <li>Múltiplos pré-requisitos do mesmo tipo = OR (satisfaz qualquer um)</li>
 *   <li>Múltiplos pré-requisitos de tipos diferentes = AND (todos devem ser atendidos)</li>
 * </ul>
 */
@Entity
@Table(name = "vantagem_pre_requisitos")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemPreRequisito extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A vantagem que TEM o pré-requisito. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id", nullable = false)
    private VantagemConfig vantagem;

    /** Tipo do pré-requisito (polimorfismo por enum). */
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPreRequisito tipo = TipoPreRequisito.VANTAGEM;

    /** [VANTAGEM] A vantagem que É EXIGIDA como pré-requisito. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_requisito_id")
    private VantagemConfig requisito;

    /** [VANTAGEM] Nível mínimo que a vantagem requisito deve estar (padrão: 1). */
    @Min(1)
    @Builder.Default
    @Column(name = "nivel_minimo")
    private Integer nivelMinimo = 1;

    /** [RACA] Raça exigida como pré-requisito. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id")
    private Raca raca;

    /** [CLASSE] Classe exigida como pré-requisito. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    private ClassePersonagem classe;

    /** [ATRIBUTO] Atributo cujo valorBase deve atingir valorMinimo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_id")
    private AtributoConfig atributo;

    /** [APTIDAO] Aptidão cujo valorBase deve atingir valorMinimo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_id")
    private AptidaoConfig aptidao;

    /**
     * [ATRIBUTO / APTIDAO / NIVEL] Valor mínimo numérico exigido.
     * Para NIVEL: nível mínimo do personagem.
     * Para ATRIBUTO: valorBase mínimo do atributo.
     * Para APTIDAO: valorBase mínimo da aptidão.
     */
    @Column(name = "valor_minimo")
    private Integer valorMinimo;
}
