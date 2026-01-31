package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.converter.AtributosConverter;
import br.com.hydroom.rpg.fichacontrolador.model.embedded.Atributos;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fichas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ficha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do personagem é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s'-]+$", message = "Nome contém caracteres inválidos")
    @Column(nullable = false, length = 100)
    private String nomePersonagem;

    @NotBlank(message = "Classe é obrigatória")
    @Size(max = 50, message = "Classe deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String classe;

    @Min(value = 1, message = "Nível mínimo é 1")
    @Max(value = 20, message = "Nível máximo é 20")
    private Integer nivel;

    @Size(max = 50, message = "Raça deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String raca;

    @Size(max = 2000, message = "História deve ter no máximo 2000 caracteres")
    @Column(length = 2000)
    private String historia;

    @Valid
    @Convert(converter = AtributosConverter.class)
    @Column(columnDefinition = "TEXT")
    private Atributos atributos; // Atributos estruturados com validação

    @Size(max = 65535, message = "Habilidades excedem tamanho máximo")
    @Column(columnDefinition = "TEXT")
    private String habilidades; // JSON com habilidades (TODO: criar classe estruturada)

    @Size(max = 65535, message = "Equipamentos excedem tamanho máximo")
    @Column(columnDefinition = "TEXT")
    private String equipamentos; // JSON com equipamentos (TODO: criar classe estruturada)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "Usuário é obrigatório")
    private Usuario usuario;

    @Column(nullable = false)
    private Boolean ativa = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime atualizadoEm;
}
