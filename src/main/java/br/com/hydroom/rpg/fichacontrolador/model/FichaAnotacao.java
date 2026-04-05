package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade que representa uma anotação em uma ficha de personagem.
 *
 * <p>Anotações do tipo MESTRE podem ser visíveis ou invisíveis para o Jogador
 * (controlado pelo campo {@code visivelParaJogador}).</p>
 */
@Entity
@Table(name = "ficha_anotacoes", indexes = {
    @Index(name = "idx_ficha_anotacao_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_anotacao_autor", columnList = "autor_id"),
    @Index(name = "idx_ficha_anotacao_tipo", columnList = "tipo_anotacao")
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaAnotacao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_anotacao", nullable = false, length = 20)
    private TipoAnotacao tipoAnotacao;

    /**
     * Controla se a anotação do Mestre é visível para o Jogador.
     * Relevante apenas para anotações do tipo MESTRE.
     */
    @Builder.Default
    @Column(name = "visivel_para_jogador", nullable = false)
    private Boolean visivelParaJogador = false;
}
