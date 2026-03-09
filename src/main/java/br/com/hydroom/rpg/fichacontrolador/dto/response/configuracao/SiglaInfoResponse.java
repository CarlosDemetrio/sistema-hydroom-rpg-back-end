package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Informações básicas de uma sigla em uso.
 *
 * @param sigla a sigla/abreviação
 * @param nome  nome da configuração que a detém
 */
public record SiglaInfoResponse(String sigla, String nome) {}
