package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoVantagem;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Configuracao de vantagem do jogo.
 * Vantagens podem ser do tipo VANTAGEM (comprada com pontos) ou INSOLITUS (concedida pelo Mestre sem custo).
 */
@Entity
@Table(name = "vantagem_config", indexes = {
    @Index(name = "idx_vantagem_config_jogo", columnList = "jogo_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_vantagem_config_jogo_nome", columnNames = {"jogo_id", "nome"}),
    @UniqueConstraint(name = "uk_vantagem_config_jogo_sigla", columnNames = {"jogo_id", "sigla"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descricao;

    @Size(min = 2, max = 5, message = "Sigla deve ter entre 2 e 5 caracteres")
    @Column(name = "sigla", nullable = true, length = 5)
    private String sigla;

    @NotNull
    @Min(1)
    @Column(name = "nivel_maximo", nullable = false)
    @Builder.Default
    private Integer nivelMaximo = 10;

    @NotBlank
    @Size(max = 100)
    @Column(name = "formula_custo", nullable = false, length = 100)
    private String formulaCusto; // Ex: "NIVEL * 2", "NIVEL * NIVEL", etc.

    @Size(max = 500)
    @Column(name = "descricao_efeito", length = 500)
    private String descricaoEfeito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_vantagem_id")
    private CategoriaVantagem categoriaVantagem;

    @OneToMany(mappedBy = "vantagem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VantagemPreRequisito> preRequisitos = new ArrayList<>();

    @OneToMany(mappedBy = "vantagemConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<VantagemEfeito> efeitos = new ArrayList<>();

    /**
     * Tipo da vantagem: VANTAGEM (comprada com pontos) ou INSOLITUS (concedida pelo Mestre).
     * Default: VANTAGEM para manter compatibilidade retroativa.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vantagem", nullable = false, length = 20)
    private TipoVantagem tipoVantagem = TipoVantagem.VANTAGEM;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;
}
