package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response do inventário completo de uma ficha, separado por equipados e em estoque.
 */
public record FichaInventarioResponse(
        List<FichaItemResponse> equipados,
        List<FichaItemResponse> inventario,
        BigDecimal pesoTotal,
        BigDecimal capacidadeCarga,
        boolean sobrecarregado
) {}
