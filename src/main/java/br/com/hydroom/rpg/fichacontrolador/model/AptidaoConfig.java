package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuração de aptidão do jogo.
 */
@Entity
@Table(name = "aptidao_config", indexes = {
    @Index(name = "idx_aptidao_config_jogo", columnList = "jogo_id, ativo"),
    @Index(name = "idx_aptidao_config_tipo", columnList = "tipo_aptidao_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_aptidao_config_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptidaoConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_aptidao_id", nullable = false)
    private TipoAptidao tipoAptidao;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nome;

    @Size(max = 500)
    @Column(length = 500)
    private String descricao;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
