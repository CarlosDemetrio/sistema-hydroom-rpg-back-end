package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Status de uma ficha de personagem.
 *
 * <ul>
 *   <li>RASCUNHO — ficha recém-criada, ainda sem todos os campos obrigatórios preenchidos</li>
 *   <li>ATIVA — ficha validada e pronta para uso em sessão</li>
 *   <li>MORTA — personagem morto; estado final (irreversível)</li>
 *   <li>ABANDONADA — ficha abandonada pelo jogador; estado final (irreversível)</li>
 *   <li>COMPLETA — @deprecated use {@link #ATIVA}; mantido apenas para compatibilidade com registros legados no banco</li>
 * </ul>
 */
public enum FichaStatus {
    RASCUNHO,
    ATIVA,
    MORTA,
    ABANDONADA,
    /** @deprecated use ATIVA — mantido para compatibilidade com registros legados */
    @Deprecated
    COMPLETA
}
