package br.com.hydroom.rpg.fichacontrolador.dto.request;

import java.util.Map;

/**
 * DTO para solicitar um preview de cálculos da ficha sem persistir.
 *
 * <p>Permite simular mudanças nos valores de atributos e aptidões
 * e verificar os totais calculados antes de confirmar.</p>
 */
public record FichaPreviewRequest(
        /** Map de fichaAtributoId → novo valor de base */
        Map<Long, Integer> atributoBase,

        /** Map de fichaAptidaoId → novo valor de base */
        Map<Long, Integer> aptidaoBase,

        /** Nova XP (para simular avanço de nível) */
        Long xp
) {}
