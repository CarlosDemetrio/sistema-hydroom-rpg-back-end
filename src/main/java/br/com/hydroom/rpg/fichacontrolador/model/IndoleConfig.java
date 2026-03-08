package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Configuração de índoles disponíveis (Bom, Mau, Neutro, etc).
 * Totalmente configurável pelo Mestre.
 */
@Entity
@Table(name = "indoles_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_indole_nome_jogo", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoleConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @NotBlank(message = "Nome da índole é obrigatório")
    @Size(max = 50, message = "Nome da índole não pode ter mais de 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nome;

    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    @Column(length = 200)
    private String descricao;

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
