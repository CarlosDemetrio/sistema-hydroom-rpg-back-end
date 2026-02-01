package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Configuração de atributo do jogo (Força, Destreza, etc.).
 */
@Entity
@Table(name = "atributo_config", indexes = {
    @Index(name = "idx_atributo_config_jogo", columnList = "jogo_id, ativo")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_atributo_config_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Audited
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nome;

    @Size(max = 500)
    @Column(length = 500)
    private String descricao;

    @Size(max = 100)
    @Column(name = "formula_impeto", length = 100)
    private String formulaImpeto;

    @Size(max = 200)
    @Column(name = "descricao_impeto", length = 200)
    private String descricaoImpeto;

    @Builder.Default
    @Column(name = "valor_minimo")
    private Integer valorMinimo = 0;

    @Builder.Default
    @Column(name = "valor_maximo")
    private Integer valorMaximo = 999;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
