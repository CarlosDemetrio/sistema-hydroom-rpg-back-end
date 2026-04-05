package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Representa uma sigla em uso no jogo, com informações sobre a entidade que a detém.
 *
 * @param tipo     Tipo da entidade (ATRIBUTO, BONUS, VANTAGEM)
 * @param sigla    A sigla em uso
 * @param entityId ID da entidade
 * @param nome     Nome da entidade
 */
public record SiglaEmUsoResponse(String tipo, String sigla, Long entityId, String nome) {}
