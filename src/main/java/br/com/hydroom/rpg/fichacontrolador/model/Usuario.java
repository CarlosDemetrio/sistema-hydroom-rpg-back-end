package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um usuário autenticado via OAuth2.
 * Estende AuditableEntity para ter campos de auditoria automáticos.
 */
@Entity
@Table(name = "usuarios")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = ValidationMessages.Usuario.EMAIL_OBRIGATORIO)
    @Email(message = ValidationMessages.Usuario.EMAIL_INVALIDO)
    @Size(max = ValidationMessages.Limites.USUARIO_EMAIL_MAX, message = ValidationMessages.Usuario.EMAIL_TAMANHO)
    @Column(nullable = false, unique = true, length = ValidationMessages.Limites.USUARIO_EMAIL_MAX)
    private String email;

    @NotBlank(message = ValidationMessages.Usuario.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.USUARIO_NOME_MIN,
          max = ValidationMessages.Limites.USUARIO_NOME_MAX,
          message = ValidationMessages.Usuario.NOME_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.USUARIO_NOME_MAX)
    private String nome;

    @Size(max = ValidationMessages.Limites.USUARIO_IMAGEM_URL_MAX,
          message = ValidationMessages.Usuario.IMAGEM_URL_TAMANHO)
    @Column(length = ValidationMessages.Limites.USUARIO_IMAGEM_URL_MAX)
    private String imagemUrl;

    @NotBlank(message = ValidationMessages.Usuario.PROVIDER_OBRIGATORIO)
    @Size(max = ValidationMessages.Limites.USUARIO_PROVIDER_MAX,
          message = ValidationMessages.Usuario.PROVIDER_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.USUARIO_PROVIDER_MAX)
    private String provider; // GOOGLE, FACEBOOK, etc

    @NotBlank(message = ValidationMessages.Usuario.PROVIDER_ID_OBRIGATORIO)
    @Size(max = ValidationMessages.Limites.USUARIO_PROVIDER_ID_MAX,
          message = ValidationMessages.Usuario.PROVIDER_ID_TAMANHO)
    @Column(nullable = false, unique = true, length = ValidationMessages.Limites.USUARIO_PROVIDER_ID_MAX)
    private String providerId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "JOGADOR"; // JOGADOR (padrão) ou MESTRE (apenas via banco)
}
