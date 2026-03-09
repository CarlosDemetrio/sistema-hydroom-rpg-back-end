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
 * Entidade que representa uma classe de personagem configurável pelo Mestre.
 * Exemplos: Guerreiro, Mago, Arqueiro, etc.
 */
@Entity
@Table(name = "classes_personagem", uniqueConstraints = {
    @UniqueConstraint(name = "uk_classe_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassePersonagem extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.ClassePersonagem.JOGO_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank(message = ValidationMessages.ClassePersonagem.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.CLASSE_NOME_MIN,
          max = ValidationMessages.Limites.CLASSE_NOME_MAX,
          message = ValidationMessages.ClassePersonagem.NOME_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.CLASSE_NOME_MAX)
    private String nome;

    @Size(max = ValidationMessages.Limites.CLASSE_DESCRICAO_MAX,
          message = ValidationMessages.ClassePersonagem.DESCRICAO_TAMANHO)
    @Column(columnDefinition = "TEXT")
    private String descricao;


    @OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ClasseBonus> bonusConfig = new HashSet<>();

    @OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ClasseAptidaoBonus> aptidaoBonus = new HashSet<>();

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
