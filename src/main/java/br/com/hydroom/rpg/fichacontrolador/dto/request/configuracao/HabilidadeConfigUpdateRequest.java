package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.*;

/**
 * Request DTO para atualização parcial de habilidade.
 * Todos os campos são opcionais — apenas os não-nulos serão atualizados.
 */
public record HabilidadeConfigUpdateRequest(
    @Size(max = 100) String nome,
    @Size(max = 1000) String descricao,
    @Size(max = 500) String danoEfeito,
    @Min(0) Integer ordemExibicao
) {}
