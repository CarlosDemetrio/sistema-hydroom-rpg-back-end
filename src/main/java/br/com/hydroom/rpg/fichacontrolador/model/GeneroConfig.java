package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuração de gêneros disponíveis no jogo.
 * Totalmente configurável pelo Mestre.
 */
@Entity
@Table(name = "generos_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_genero_nome_jogo", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneroConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Nome do gênero é obrigatório")
    @Size(max = 50, message = "Nome do gênero não pode ter mais de 50 caracteres")
    private String nome;

    @Column(length = 200)
    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    private String descricao;

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
