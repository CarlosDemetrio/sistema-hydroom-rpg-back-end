package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Entidade que armazena a descrição física de um personagem.
 */
@Entity
@Table(name = "ficha_descricao_fisica", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_descricao_fisica", columnNames = {"ficha_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaDescricaoFisica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ficha é obrigatória")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    /**
     * Altura em centímetros.
     */
    @Column(name = "altura")
    private Integer altura;

    /**
     * Peso em quilogramas.
     */
    @Column(name = "peso", precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "idade")
    private Integer idade;

    @Size(max = 100, message = "Descrição dos olhos deve ter no máximo 100 caracteres")
    @Column(name = "descricao_olhos", length = 100)
    private String descricaoOlhos;

    @Size(max = 100, message = "Descrição dos cabelos deve ter no máximo 100 caracteres")
    @Column(name = "descricao_cabelos", length = 100)
    private String descricaoCabelos;

    @Size(max = 100, message = "Descrição da pele deve ter no máximo 100 caracteres")
    @Column(name = "descricao_pele", length = 100)
    private String descricaoPele;
}
