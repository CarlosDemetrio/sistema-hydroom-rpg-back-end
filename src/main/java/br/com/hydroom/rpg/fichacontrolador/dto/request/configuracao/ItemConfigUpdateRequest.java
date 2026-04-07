package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request para atualização parcial de ItemConfig.
 * Todos os campos são opcionais — apenas os não-nulos serão atualizados.
 */
public record ItemConfigUpdateRequest(
    @Size(max = 100) String nome,
    Long raridadeId,
    Long tipoId,
    @DecimalMin("0.00") BigDecimal peso,
    @Min(0) Integer valor,
    @Min(1) Integer duracaoPadrao,
    @Min(1) Integer nivelMinimo,
    @Size(max = 1000) String propriedades,
    @Size(max = 2000) String descricao,
    @Min(1) Integer ordemExibicao
) {}
