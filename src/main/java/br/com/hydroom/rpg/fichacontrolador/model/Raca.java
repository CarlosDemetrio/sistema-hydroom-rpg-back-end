package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma raça de personagem configurável pelo Mestre.
 * Exemplos: Humano, Elfo, Anão, etc.
 */
@Entity
@Table(name = "racas", uniqueConstraints = {
    @UniqueConstraint(name = "uk_raca_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Raca extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.Raca.JOGO_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank(message = ValidationMessages.Raca.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.RACA_NOME_MIN,
          max = ValidationMessages.Limites.RACA_NOME_MAX,
          message = ValidationMessages.Raca.NOME_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.RACA_NOME_MAX)
    private String nome;

    @Size(max = ValidationMessages.Limites.RACA_DESCRICAO_MAX,
          message = ValidationMessages.Raca.DESCRICAO_TAMANHO)
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @OneToMany(mappedBy = "raca", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RacaBonusAtributo> bonusAtributos = new HashSet<>();

    @OneToMany(mappedBy = "raca", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RacaClassePermitida> classesPermitidas = new HashSet<>();

    @OneToMany(mappedBy = "raca", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RacaPontosConfig> pontosConfig = new HashSet<>();

    @OneToMany(mappedBy = "raca", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RacaVantagemPreDefinida> vantagensPreDefinidas = new HashSet<>();

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
