package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO de default para RaridadeItemConfig.
 * Utilizado pelo DefaultGameConfigProviderImpl para seed do dataset de raridades.
 */
public record RaridadeItemConfigDefault(
        String nome,
        String cor,
        int ordemExibicao,
        boolean podeJogadorAdicionar,
        Integer bonusAtributoMin,
        Integer bonusAtributoMax,
        Integer bonusDerivadoMin,
        Integer bonusDerivadoMax,
        String descricao
) {}
