package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Entidade que representa uma ficha de personagem de RPG.
 * Totalmente normalizada - dados configuráveis armazenados em tabelas relacionadas.
 */
@Entity
@Table(name = "fichas", indexes = {
    @Index(name = "idx_ficha_jogo_usuario_ativo", columnList = "jogo_id, usuario_id, ativo"),
    @Index(name = "idx_ficha_nome_personagem", columnList = "nome_personagem")
})
@Audited
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ficha extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.Ficha.JOGO_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotNull(message = ValidationMessages.Ficha.USUARIO_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_personagem_id")
    private ClassePersonagem classePersonagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id")
    private Raca raca;

    // ===== DADOS BÁSICOS DO PERSONAGEM =====

    @NotBlank(message = ValidationMessages.Ficha.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.FICHA_NOME_MIN,
          max = ValidationMessages.Limites.FICHA_NOME_MAX,
          message = ValidationMessages.Ficha.NOME_TAMANHO)
    @Column(name = "nome_personagem", nullable = false, length = ValidationMessages.Limites.FICHA_NOME_MAX)
    private String nomePersonagem;

    @Size(max = ValidationMessages.Limites.FICHA_JOGADOR_NOME_MAX,
          message = ValidationMessages.Ficha.JOGADOR_NOME_TAMANHO)
    @Column(name = "jogador_nome", length = ValidationMessages.Limites.FICHA_JOGADOR_NOME_MAX)
    private String jogadorNome;

    @Size(max = ValidationMessages.Limites.FICHA_TITULO_MAX,
          message = ValidationMessages.Ficha.TITULO_TAMANHO)
    @Column(name = "titulo_heroico", length = ValidationMessages.Limites.FICHA_TITULO_MAX)
    private String tituloHeroico;

    @Size(max = ValidationMessages.Limites.FICHA_INSOLITUS_MAX,
          message = ValidationMessages.Ficha.INSOLITUS_TAMANHO)
    @Column(length = ValidationMessages.Limites.FICHA_INSOLITUS_MAX)
    private String insolitus;

    @Size(max = ValidationMessages.Limites.FICHA_ORIGEM_MAX,
          message = ValidationMessages.Ficha.ORIGEM_TAMANHO)
    @Column(length = ValidationMessages.Limites.FICHA_ORIGEM_MAX)
    private String origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genero_id")
    private GeneroConfig genero;

    @Size(max = ValidationMessages.Limites.FICHA_ARQUETIPO_MAX,
          message = ValidationMessages.Ficha.ARQUETIPO_TAMANHO)
    @Column(name = "arquetipo_referencia", length = ValidationMessages.Limites.FICHA_ARQUETIPO_MAX)
    private String arquetipoReferencia;

    // ===== APARÊNCIA FÍSICA =====

    @Min(value = 0, message = ValidationMessages.Ficha.IDADE_MINIMA)
    @Max(value = 9999, message = ValidationMessages.Ficha.IDADE_MAXIMA)
    private Integer idade;

    @Min(value = 0, message = ValidationMessages.Ficha.ALTURA_MINIMA)
    @Max(value = 999, message = ValidationMessages.Ficha.ALTURA_MAXIMA)
    @Column(name = "altura_cm")
    private Integer alturaCm;

    @DecimalMin(value = "0.0", message = ValidationMessages.Ficha.PESO_MINIMO)
    @DecimalMax(value = "999.99", message = ValidationMessages.Ficha.PESO_MAXIMO)
    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Size(max = ValidationMessages.Limites.FICHA_COR_MAX,
          message = ValidationMessages.Ficha.COR_CABELO_TAMANHO)
    @Column(name = "cor_cabelo", length = ValidationMessages.Limites.FICHA_COR_MAX)
    private String corCabelo;

    @Size(max = ValidationMessages.Limites.FICHA_COR_MAX,
          message = ValidationMessages.Ficha.TAMANHO_CABELO_TAMANHO)
    @Column(name = "tamanho_cabelo", length = ValidationMessages.Limites.FICHA_COR_MAX)
    private String tamanhoCabelo;

    @Size(max = ValidationMessages.Limites.FICHA_COR_MAX,
          message = ValidationMessages.Ficha.COR_OLHOS_TAMANHO)
    @Column(name = "cor_olhos", length = ValidationMessages.Limites.FICHA_COR_MAX)
    private String corOlhos;

    // ===== PERSONALIDADE =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indole_id")
    private IndoleConfig indole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presenca_id")
    private PresencaConfig presenca;

    // ===== PROGRESSÃO =====

    @NotNull(message = ValidationMessages.Ficha.NIVEL_OBRIGATORIO)
    @Min(value = ValidationMessages.Limites.FICHA_NIVEL_MIN,
         message = ValidationMessages.Ficha.NIVEL_MINIMO)
    @Max(value = 99, message = ValidationMessages.Ficha.NIVEL_MAXIMO)
    @Builder.Default
    @Column(nullable = false)
    private Integer nivel = 1;

    @NotNull(message = ValidationMessages.Ficha.EXPERIENCIA_OBRIGATORIA)
    @Min(value = 0, message = ValidationMessages.Ficha.EXPERIENCIA_MINIMA)
    @Builder.Default
    @Column(nullable = false)
    private Long experiencia = 0L;

    @NotNull
    @Min(value = 0, message = ValidationMessages.Ficha.RENASCIMENTOS_MINIMO)
    @Builder.Default
    @Column(nullable = false)
    private Integer renascimentos = 0;

    @Size(max = 2000)
    @Column(name = "imagem_url", length = 2000)
    private String imagemUrl;

    // ===== CONTROLE =====

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "compartilhada_com_jogadores", nullable = false)
    private Boolean compartilhadaComJogadores = false;
}
