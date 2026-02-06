package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Aptidão.
 * Contém todas as informações de uma aptidão para exibição.
 *
 * @param id ID da aptidão
 * @param jogoId ID do jogo
 * @param tipoAptidaoId ID do tipo de aptidão
 * @param tipoAptidaoNome Nome do tipo de aptidão
 * @param nome Nome da aptidão
 * @param descricao Descrição da aptidão
 * @param ordemExibicao Ordem de exibição na interface
 * @param ativo Status da aptidão
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record AptidaoResponse(
    Long id,
    Long jogoId,
    Long tipoAptidaoId,
    String tipoAptidaoNome,
    String nome,
    String descricao,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
