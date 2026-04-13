package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

/**
 * Request DTO para criar uma nova habilidade.
 */
public record HabilidadeConfigRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 1000) String descricao,
    @Size(max = 500) String danoEfeito,
    @NotNull @Min(0) Integer ordemExibicao
) {}
