package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request para criação de ItemConfig.
 */
public record ItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 100) String nome,
    @NotNull Long raridadeId,
    @NotNull Long tipoId,
    @NotNull @DecimalMin("0.00") BigDecimal peso,
    @Min(0) Integer valor,
    @Min(1) Integer duracaoPadrao,
    @NotNull @Min(1) Integer nivelMinimo,
    @Size(max = 1000) String propriedades,
    @Size(max = 2000) String descricao,
    @NotNull @Min(1) Integer ordemExibicao
) {}
