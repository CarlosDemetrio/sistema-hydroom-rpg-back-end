package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta do export de configurações de um jogo.
 * Contém todas as 13 configurações sem IDs (portável entre jogos).
 */
public record ConfigExportResponse(
        String jogoNome,
        List<AtributoExport> atributos,
        List<TipoAptidaoExport> tiposAptidao,
        List<AptidaoExport> aptidoes,
        List<BonusExport> bonus,
        List<ClasseExport> classes,
        List<DadoProspeccaoExport> dadosProspeccao,
        List<GeneroExport> generos,
        List<IndoleExport> indoles,
        List<MembroCorpoExport> membrosCorpo,
        List<NivelExport> niveis,
        List<PresencaExport> presencas,
        List<RacaExport> racas,
        List<VantagemExport> vantagens
) {
    public record AtributoExport(
            String nome,
            String abreviacao,
            String descricao,
            String formulaImpeto,
            String descricaoImpeto,
            Integer valorMinimo,
            Integer valorMaximo,
            Integer ordemExibicao
    ) {}

    public record TipoAptidaoExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record AptidaoExport(
            String nome,
            String descricao,
            String tipoAptidaoNome,
            Integer ordemExibicao
    ) {}

    public record BonusExport(
            String nome,
            String descricao,
            String sigla,
            String formulaBase,
            Integer ordemExibicao
    ) {}

    public record ClasseExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record DadoProspeccaoExport(
            String nome,
            String descricao,
            Integer numeroFaces,
            Integer ordemExibicao
    ) {}

    public record GeneroExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record IndoleExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record MembroCorpoExport(
            String nome,
            BigDecimal porcentagemVida,
            Integer ordemExibicao
    ) {}

    public record NivelExport(
            Integer nivel,
            Long xpNecessaria,
            Integer pontosAtributo,
            Integer pontosAptidao,
            Integer limitadorAtributo
    ) {}

    public record PresencaExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record RacaExport(
            String nome,
            String descricao,
            Integer ordemExibicao
    ) {}

    public record VantagemExport(
            String nome,
            String descricao,
            String sigla,
            Integer nivelMaximo,
            String formulaCusto,
            String descricaoEfeito,
            Integer ordemExibicao
    ) {}
}
