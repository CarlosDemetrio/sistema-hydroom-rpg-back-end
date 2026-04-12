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
 * Define uma vantagem pré-definida que personagens de uma classe recebem ao atingir determinado nível.
 */
@Entity
@Table(name = "classe_vantagem_predefinida",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_vantagem_nivel",
        columnNames = {"classe_personagem_id", "nivel", "vantagem_config_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "classePersonagem")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseVantagemPreDefinida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_personagem_id", nullable = false)
    private ClassePersonagem classePersonagem;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;
}
