package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categorias de vantagens (ex: Atributo, Combate, Magia, Social, etc).
 * Configurável pelo Mestre.
 */
@Entity
@Table(name = "categorias_vantagem", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categoria_vantagem_nome_jogo", columnNames = {"jogo_id", "nome"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaVantagem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria não pode ter mais de 100 caracteres")
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    @NotNull(message = "Campo ativo é obrigatório")
    private Boolean ativo = true;

    @Column(name = "ordem", nullable = false)
    @NotNull(message = "Ordem de exibição é obrigatória")
    private Integer ordem;

    /**
     * Cor em hexadecimal para representação visual (ex: #FF0000).
     */
    @Column(length = 7)
    @Size(max = 7, message = "Cor deve estar no formato #RRGGBB")
    private String cor;
}
