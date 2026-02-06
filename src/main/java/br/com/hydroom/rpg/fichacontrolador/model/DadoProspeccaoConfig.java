package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuração de dado de prospecção do jogo (d4, d6, d8, d10, d12, d20, d100).
 */
@Entity
@Table(name = "dado_prospeccao_config", indexes = {
    @Index(name = "idx_dado_prospeccao_config_jogo", columnList = "jogo_id, ativo")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_dado_prospeccao_config_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DadoProspeccaoConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String nome; // Ex: "d4", "d6", "d8", "d10", "d12", "d20", "d100"

    @Size(max = 200)
    @Column(length = 200)
    private String descricao;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "numero_faces", nullable = false)
    private Integer numeroFaces; // 4, 6, 8, 10, 12, 20, 100

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;
}
