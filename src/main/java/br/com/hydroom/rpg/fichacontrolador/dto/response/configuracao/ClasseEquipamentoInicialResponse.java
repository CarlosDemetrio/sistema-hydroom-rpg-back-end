package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response para equipamento inicial de classe.
 */
public record ClasseEquipamentoInicialResponse(
        Long id,
        Long classeId,
        String classeNome,
        Long itemConfigId,
        String itemConfigNome,
        String itemRaridade,
        String itemRaridadeCor,
        String itemCategoria,
        boolean obrigatorio,
        Integer grupoEscolha,
        int quantidade,
        LocalDateTime dataCriacao
) {}
