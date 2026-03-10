package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.util.List;

/**
 * DTO de resposta para preview de cálculos da ficha.
 *
 * <p>Contém os valores recalculados em memória, sem persistir.</p>
 */
public record FichaPreviewResponse(
        Long fichaId,
        Integer nivel,
        Long xp,

        List<AtributoPreview> atributos,
        List<BonusPreview> bonus,
        VidaPreview vida,
        EssenciaPreview essencia,
        AmeacaPreview ameaca
) {

    public record AtributoPreview(
            Long fichaAtributoId,
            Long atributoConfigId,
            String nome,
            String abreviacao,
            Integer base,
            Integer nivel,
            Integer outros,
            Integer total,
            Double impeto
    ) {}

    public record BonusPreview(
            Long fichaBonusId,
            Long bonusConfigId,
            String nome,
            String sigla,
            Integer base,
            Integer vantagens,
            Integer classe,
            Integer itens,
            Integer gloria,
            Integer outros,
            Integer total
    ) {}

    public record VidaPreview(
            Integer vt,
            Integer outros,
            Integer vidaTotal,
            List<MembroPreview> membros
    ) {}

    public record MembroPreview(
            Long fichaVidaMembroId,
            Long membroConfigId,
            String nome,
            Integer vida
    ) {}

    public record EssenciaPreview(
            Integer vantagens,
            Integer outros,
            Integer total
    ) {}

    public record AmeacaPreview(
            Integer itens,
            Integer titulos,
            Integer renascimentos,
            Integer outros,
            Integer total
    ) {}
}
