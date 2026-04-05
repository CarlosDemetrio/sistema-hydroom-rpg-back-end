package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.ConfigExportResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request para importação de configurações para um jogo.
 * Aceita o mesmo formato do ConfigExportResponse.
 */
public record ConfigImportRequest(
        @NotNull @Valid List<ConfigExportResponse.AtributoExport> atributos,
        @NotNull @Valid List<ConfigExportResponse.TipoAptidaoExport> tiposAptidao,
        @NotNull @Valid List<ConfigExportResponse.AptidaoExport> aptidoes,
        @NotNull @Valid List<ConfigExportResponse.BonusExport> bonus,
        @NotNull @Valid List<ConfigExportResponse.ClasseExport> classes,
        @NotNull @Valid List<ConfigExportResponse.DadoProspeccaoExport> dadosProspeccao,
        @NotNull @Valid List<ConfigExportResponse.GeneroExport> generos,
        @NotNull @Valid List<ConfigExportResponse.IndoleExport> indoles,
        @NotNull @Valid List<ConfigExportResponse.MembroCorpoExport> membrosCorpo,
        @NotNull @Valid List<ConfigExportResponse.NivelExport> niveis,
        @NotNull @Valid List<ConfigExportResponse.PresencaExport> presencas,
        @NotNull @Valid List<ConfigExportResponse.RacaExport> racas,
        @NotNull @Valid List<ConfigExportResponse.VantagemExport> vantagens
) {}
