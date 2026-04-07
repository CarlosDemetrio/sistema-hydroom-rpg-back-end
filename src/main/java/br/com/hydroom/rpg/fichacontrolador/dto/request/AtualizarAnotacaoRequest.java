package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para atualização parcial de uma anotação de ficha.
 *
 * <p>Campos nulos são ignorados (PATCH semântico): somente os campos enviados são atualizados.</p>
 *
 * <p>Regras de autorização:</p>
 * <ul>
 *   <li>{@code visivelParaJogador} — apenas MESTRE pode alterar; ignorado silenciosamente se enviado por JOGADOR</li>
 *   <li>{@code visivelParaTodos} — autor ou MESTRE podem alterar</li>
 *   <li>{@code pastaPaiId} — move a anotação para a pasta indicada; {@code null} = não altera a pasta atual</li>
 * </ul>
 *
 * <p>TODO: Para mover uma anotação de volta à raiz (sem pasta), utilizar um endpoint dedicado
 * ou um campo sentinel separado. Atualmente não é possível via este DTO.</p>
 */
public record AtualizarAnotacaoRequest(
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String titulo,

        String conteudo,

        /** Ignorado silenciosamente se enviado por JOGADOR. */
        Boolean visivelParaJogador,

        /** Autor ou MESTRE podem alterar. */
        Boolean visivelParaTodos,

        /**
         * ID da pasta destino. {@code null} = não alterar pasta atual.
         * Para mover para a raiz, use um endpoint dedicado (TODO futuro).
         */
        Long pastaPaiId
) {}
