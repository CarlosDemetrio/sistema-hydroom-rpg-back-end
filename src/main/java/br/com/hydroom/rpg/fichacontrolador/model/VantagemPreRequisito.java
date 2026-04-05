package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Pré-requisito de vantagem: para comprar a vantagem A, o personagem precisa ter a vantagem B
 * no nível mínimo especificado.
 */
@Entity
@Table(name = "vantagem_pre_requisitos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vantagem_prerequisito", columnNames = {"vantagem_id", "vantagem_requisito_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemPreRequisito extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A vantagem que TEM o pré-requisito. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id", nullable = false)
    private VantagemConfig vantagem;

    /** A vantagem que É EXIGIDA como pré-requisito. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_requisito_id", nullable = false)
    private VantagemConfig requisito;

    /** Nível mínimo que o pré-requisito deve estar (padrão: 1). */
    @NotNull
    @Min(1)
    @Builder.Default
    @Column(name = "nivel_minimo", nullable = false)
    private Integer nivelMinimo = 1;
}
