package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AtualizarVisibilidadeRequest(
    @NotNull(message = "Lista de jogadores é obrigatória")
    List<Long> jogadoresIds,

    /**
     * Se true, substitui completamente a lista de jogadores com acesso (soft-deleta existentes).
     * Se false (padrão), apenas adiciona novos jogadores à lista existente.
     */
    boolean substituir
) {}
