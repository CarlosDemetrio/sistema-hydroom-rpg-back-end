package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para CategoriaVantagem.
 *
 * @param id                    ID da categoria
 * @param jogoId                ID do jogo
 * @param nome                  Nome da categoria
 * @param descricao             Descrição da categoria
 * @param cor                   Cor em formato #RRGGBB
 * @param ordemExibicao         Ordem de exibição
 * @param dataCriacao           Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record CategoriaVantagemResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String cor,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
