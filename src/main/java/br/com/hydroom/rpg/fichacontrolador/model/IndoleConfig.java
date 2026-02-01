package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Configuração de índoles disponíveis (Bom, Mau, Neutro, etc).
 * Totalmente configurável pelo Mestre.
 */
@Entity
@Table(name = "indoles_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_indole_nome_jogo", columnNames = {"jogo_id", "nome"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class IndoleConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Nome da índole é obrigatório")
    @Size(max = 50, message = "Nome da índole não pode ter mais de 50 caracteres")
    private String nome;

    @Column(length = 200)
    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    private String descricao;

    @Column(nullable = false)
    @NotNull(message = "Campo ativo é obrigatório")
    private Boolean ativo = true;

    @Column(name = "ordem", nullable = false)
    @NotNull(message = "Ordem de exibição é obrigatória")
    private Integer ordem;
}
