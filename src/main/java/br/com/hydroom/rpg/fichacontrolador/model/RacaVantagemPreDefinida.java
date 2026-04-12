package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Define uma vantagem pré-definida que personagens de uma raça recebem ao atingir determinado nível.
 */
@Entity
@Table(name = "raca_vantagem_predefinida",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_raca_vantagem_nivel",
        columnNames = {"raca_id", "nivel", "vantagem_config_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "raca")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacaVantagemPreDefinida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;
}
