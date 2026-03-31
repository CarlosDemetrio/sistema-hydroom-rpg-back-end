package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Efeito concreto de uma vantagem.
 *
 * <p>Uma VantagemConfig pode ter múltiplos efeitos (ex: +2 FOR e +1 BBA ao mesmo tempo).
 * Cada efeito aponta para um alvo específico (atributo, aptidão, bônus derivado ou membro).</p>
 *
 * <p>Regra de preenchimento dos campos de alvo:</p>
 * <ul>
 *   <li>BONUS_ATRIBUTO → atributoAlvo obrigatório</li>
 *   <li>BONUS_APTIDAO → aptidaoAlvo obrigatório</li>
 *   <li>BONUS_DERIVADO → bonusAlvo obrigatório</li>
 *   <li>BONUS_VIDA_MEMBRO → membroAlvo obrigatório</li>
 *   <li>BONUS_VIDA, BONUS_ESSENCIA → nenhum alvo FK necessário</li>
 *   <li>DADO_UP → nenhum valor numérico; incremento é sempre +1 face</li>
 *   <li>FORMULA_CUSTOMIZADA → campo formula obrigatório; variável: nivel_vantagem</li>
 * </ul>
 */
@Entity
@Table(name = "vantagem_efeito", indexes = {
    @Index(name = "idx_vantagem_efeito_vantagem", columnList = "vantagem_config_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemEfeito extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEfeito tipoEfeito;

    // ===== ALVOS DO EFEITO (apenas UM será preenchido) =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_config_id")
    private AtributoConfig atributoAlvo; // para BONUS_ATRIBUTO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_config_id")
    private AptidaoConfig aptidaoAlvo; // para BONUS_APTIDAO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_config_id")
    private BonusConfig bonusAlvo; // para BONUS_DERIVADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_corpo_config_id")
    private MembroCorpoConfig membroAlvo; // para BONUS_VIDA_MEMBRO

    // ===== VALOR DO EFEITO =====

    /** Valor fixo adicionado independente do nível da vantagem. Ex: +5 VT sempre. */
    @Column(name = "valor_fixo", precision = 10, scale = 2)
    private BigDecimal valorFixo;

    /** Valor multiplicado pelo nível da vantagem. Ex: valorPorNivel=2 → nível 3 = +6. */
    @Column(name = "valor_por_nivel", precision = 10, scale = 2)
    private BigDecimal valorPorNivel;

    /**
     * Fórmula exp4j para FORMULA_CUSTOMIZADA.
     * Variáveis disponíveis: nivel_vantagem, e abreviações de atributos do jogo.
     */
    @Column(name = "formula", length = 200)
    private String formula;

    /** Descrição textual do efeito visível ao jogador. */
    @Column(name = "descricao_efeito", length = 500)
    private String descricaoEfeito;
}
