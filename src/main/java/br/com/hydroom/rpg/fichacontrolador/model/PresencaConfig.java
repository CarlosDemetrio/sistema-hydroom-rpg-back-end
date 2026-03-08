package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Configuração de presenças disponíveis (Leal, Caótico, Neutro, etc).
 * Totalmente configurável pelo Mestre.
 */
@Entity
@Table(name = "presencas_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_presenca_nome_jogo", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresencaConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Nome da presença é obrigatório")
    @Size(max = 50, message = "Nome da presença não pode ter mais de 50 caracteres")
    private String nome;

    @Column(length = 200)
    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    private String descricao;

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
