package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

/**
 * Request DTO para atualização parcial de raridade de item.
 * Todos os campos são opcionais — apenas os não-nulos serão atualizados.
 */
public record RaridadeItemConfigUpdateRequest(
    @Size(max = 50) String nome,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve ser hexadecimal ex: #9d9d9d") String cor,
    @Min(1) Integer ordemExibicao,
    Boolean podeJogadorAdicionar,
    @Min(0) Integer bonusAtributoMin,
    @Min(0) Integer bonusAtributoMax,
    @Min(0) Integer bonusDerivadoMin,
    @Min(0) Integer bonusDerivadoMax,
    @Size(max = 500) String descricao
) {}
