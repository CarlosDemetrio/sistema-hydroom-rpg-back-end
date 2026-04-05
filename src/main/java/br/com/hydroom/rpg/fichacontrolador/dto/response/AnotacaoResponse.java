package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;

import java.time.LocalDateTime;

/**
 * DTO de resposta para uma anotação de ficha.
 */
public record AnotacaoResponse(
    Long id,
    Long fichaId,
    Long autorId,
    String autorNome,
    String titulo,
    String conteudo,
    TipoAnotacao tipoAnotacao,
    Boolean visivelParaJogador,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
