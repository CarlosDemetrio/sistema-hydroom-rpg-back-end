package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Relacionamento N:N entre Raça e ClassePersonagem para definir quais classes são permitidas por raça.
 *
 * <p>Exemplo: Elfo pode ser Mago ou Arqueiro, mas não Guerreiro.</p>
 */
@Entity
@Table(name = "raca_classes_permitidas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_raca_classe_permitida", columnNames = {"raca_id", "classe_id"})
    },
    indexes = {
        @Index(name = "idx_raca_classe_raca", columnList = "raca_id"),
        @Index(name = "idx_raca_classe_classe", columnList = "classe_id")
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "raca")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacaClassePermitida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;
}
