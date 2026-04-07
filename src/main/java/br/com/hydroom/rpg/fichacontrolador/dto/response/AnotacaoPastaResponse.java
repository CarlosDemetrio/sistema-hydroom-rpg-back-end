package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response de uma pasta de anotações, com sub-pastas aninhadas.
 */
public record AnotacaoPastaResponse(
    Long id,
    Long fichaId,
    String nome,
    Long pastaPaiId,
    Integer ordemExibicao,
    List<AnotacaoPastaResponse> subPastas,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
