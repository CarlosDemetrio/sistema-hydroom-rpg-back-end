package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response de um item do inventário de uma ficha.
 */
public record FichaItemResponse(
        Long id,
        Long fichaId,
        Long itemConfigId,
        String nome,
        boolean equipado,
        Integer duracaoAtual,
        Integer duracaoPadrao,
        int quantidade,
        BigDecimal peso,
        BigDecimal pesoEfetivo,
        String notas,
        String adicionadoPor,
        Long raridadeId,
        String raridadeNome,
        String raridadeCor,
        LocalDateTime dataCriacao
) {}
