package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

/**
 * Request DTO para criar uma nova raridade de item.
 */
public record RaridadeItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 50) String nome,
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve ser hexadecimal ex: #9d9d9d") String cor,
    @NotNull @Min(1) Integer ordemExibicao,
    @NotNull Boolean podeJogadorAdicionar,
    @Min(0) Integer bonusAtributoMin,
    @Min(0) Integer bonusAtributoMax,
    @Min(0) Integer bonusDerivadoMin,
    @Min(0) Integer bonusDerivadoMax,
    @Size(max = 500) String descricao
) {}
