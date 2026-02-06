package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Classe de Personagem.
 * Contém todas as informações de uma classe para exibição.
 *
 * @param id ID da classe
 * @param jogoId ID do jogo
 * @param nome Nome da classe
 * @param descricao Descrição da classe
 * @param ordemExibicao Ordem de exibição na interface
 * @param ativo Status da classe
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record ClasseResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
