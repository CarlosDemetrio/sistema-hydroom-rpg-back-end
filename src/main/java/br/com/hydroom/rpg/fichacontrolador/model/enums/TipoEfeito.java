package br.com.hydroom.rpg.fichacontrolador.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de efeitos que uma vantagem pode aplicar.
 */
@Getter
@RequiredArgsConstructor
public enum TipoEfeito {
    BONUS_ATRIBUTO("Bônus em Atributo", "Adiciona bônus permanente em um atributo"),
    BONUS_APTIDAO("Bônus em Aptidão", "Adiciona bônus permanente em uma aptidão"),
    BONUS_VIDA("Bônus em Vida", "Aumenta pontos de vida totais"),
    BONUS_ESSENCIA("Bônus em Essência", "Aumenta pontos de essência totais"),
    BONUS_AMEACA("Bônus em Ameaça", "Aumenta nível de ameaça"),
    REDUCAO_CUSTO_XP("Redução de Custo XP", "Reduz XP necessário para subir de nível"),
    HABILIDADE_ESPECIAL("Habilidade Especial", "Concede habilidade única descrita em texto livre");

    private final String nome;
    private final String descricao;
}
