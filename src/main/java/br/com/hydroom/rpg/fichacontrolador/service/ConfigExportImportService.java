package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ConfigImportRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ConfigExportResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para exportação e importação de configurações de jogos.
 * A exportação serializa todas as 13 configurações.
 * A importação cria novas configurações no jogo destino (sem substituir as existentes).
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConfigExportImportService {

    private final JogoRepository jogoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final BonusConfigRepository bonusRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final DadoProspeccaoConfigRepository dadoProspeccaoRepository;
    private final GeneroConfigRepository generoRepository;
    private final IndoleConfigRepository indoleRepository;
    private final MembroCorpoConfigRepository membroCorpoRepository;
    private final ConfiguracaoNivelRepository nivelRepository;
    private final PresencaConfigRepository presencaRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final TipoAptidaoRepository tipoAptidaoRepository;
    private final VantagemConfigRepository vantagemRepository;

    /**
     * Exporta todas as configurações de um jogo.
     */
    public ConfigExportResponse exportar(Long jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        verificarAcessoMestre(jogoId);

        List<ConfigExportResponse.AtributoExport> atributos = atributoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(a -> new ConfigExportResponse.AtributoExport(
                        a.getNome(), a.getAbreviacao(), a.getDescricao(),
                        a.getFormulaImpeto(), a.getDescricaoImpeto(),
                        a.getValorMinimo(), a.getValorMaximo(), a.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.TipoAptidaoExport> tiposAptidao = tipoAptidaoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(t -> new ConfigExportResponse.TipoAptidaoExport(
                        t.getNome(), t.getDescricao(), t.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.AptidaoExport> aptidoes = aptidaoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(a -> new ConfigExportResponse.AptidaoExport(
                        a.getNome(), a.getDescricao(),
                        a.getTipoAptidao() != null ? a.getTipoAptidao().getNome() : null,
                        a.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.BonusExport> bonus = bonusRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(b -> new ConfigExportResponse.BonusExport(
                        b.getNome(), b.getDescricao(), b.getSigla(),
                        b.getFormulaBase(), b.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.ClasseExport> classes = classeRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(c -> new ConfigExportResponse.ClasseExport(
                        c.getNome(), c.getDescricao(), c.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.DadoProspeccaoExport> dadosProspeccao = dadoProspeccaoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(d -> new ConfigExportResponse.DadoProspeccaoExport(
                        d.getNome(), d.getDescricao(), d.getNumeroFaces(), d.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.GeneroExport> generos = generoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(g -> new ConfigExportResponse.GeneroExport(
                        g.getNome(), g.getDescricao(), g.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.IndoleExport> indoles = indoleRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(i -> new ConfigExportResponse.IndoleExport(
                        i.getNome(), i.getDescricao(), i.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.MembroCorpoExport> membrosCorpo = membroCorpoRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(m -> new ConfigExportResponse.MembroCorpoExport(
                        m.getNome(), m.getPorcentagemVida(), m.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.NivelExport> niveis = nivelRepository
                .findByJogoIdOrderByNivel(jogoId).stream()
                .map(n -> new ConfigExportResponse.NivelExport(
                        n.getNivel(), n.getXpNecessaria(), n.getPontosAtributo(),
                        n.getPontosAptidao(), n.getLimitadorAtributo()))
                .toList();

        List<ConfigExportResponse.PresencaExport> presencas = presencaRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(p -> new ConfigExportResponse.PresencaExport(
                        p.getNome(), p.getDescricao(), p.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.RacaExport> racas = racaRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(r -> new ConfigExportResponse.RacaExport(
                        r.getNome(), r.getDescricao(), r.getOrdemExibicao()))
                .toList();

        List<ConfigExportResponse.VantagemExport> vantagens = vantagemRepository
                .findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .map(v -> new ConfigExportResponse.VantagemExport(
                        v.getNome(), v.getDescricao(), v.getSigla(),
                        v.getNivelMaximo(), v.getFormulaCusto(),
                        v.getDescricaoEfeito(), v.getOrdemExibicao()))
                .toList();

        log.info("Configurações exportadas do jogo ID={}", jogoId);
        return new ConfigExportResponse(
                jogo.getNome(),
                atributos, tiposAptidao, aptidoes, bonus, classes,
                dadosProspeccao, generos, indoles, membrosCorpo, niveis,
                presencas, racas, vantagens
        );
    }

    /**
     * Importa configurações para um jogo destino.
     * Ignora itens cujo nome já existe no jogo (sem sobrescrever).
     */
    @Transactional
    public void importar(Long jogoId, ConfigImportRequest request) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        verificarAcessoMestre(jogoId);

        // Atributos
        for (ConfigExportResponse.AtributoExport item : request.atributos()) {
            if (!atributoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                atributoRepository.save(AtributoConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .abreviacao(item.abreviacao())
                        .descricao(item.descricao())
                        .formulaImpeto(item.formulaImpeto())
                        .descricaoImpeto(item.descricaoImpeto())
                        .valorMinimo(item.valorMinimo() != null ? item.valorMinimo() : 0)
                        .valorMaximo(item.valorMaximo() != null ? item.valorMaximo() : 999)
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // TiposAptidao
        for (ConfigExportResponse.TipoAptidaoExport item : request.tiposAptidao()) {
            if (!tipoAptidaoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                tipoAptidaoRepository.save(TipoAptidao.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Aptidoes: precisa resolver tipoAptidao pelo nome
        for (ConfigExportResponse.AptidaoExport item : request.aptidoes()) {
            if (!aptidaoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                TipoAptidao tipo = tipoAptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                        .filter(t -> t.getNome().equalsIgnoreCase(item.tipoAptidaoNome()))
                        .findFirst()
                        .orElse(null);
                if (tipo == null) {
                    log.warn("TipoAptidao '{}' não encontrado no jogo {}, aptidão '{}' ignorada.",
                            item.tipoAptidaoNome(), jogoId, item.nome());
                    continue;
                }
                aptidaoRepository.save(AptidaoConfig.builder()
                        .jogo(jogo)
                        .tipoAptidao(tipo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Bonus
        for (ConfigExportResponse.BonusExport item : request.bonus()) {
            if (!bonusRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                bonusRepository.save(BonusConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .sigla(item.sigla())
                        .formulaBase(item.formulaBase())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Classes
        for (ConfigExportResponse.ClasseExport item : request.classes()) {
            if (!classeRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                classeRepository.save(ClassePersonagem.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // DadosProspeccao
        for (ConfigExportResponse.DadoProspeccaoExport item : request.dadosProspeccao()) {
            if (!dadoProspeccaoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                dadoProspeccaoRepository.save(DadoProspeccaoConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .numeroFaces(item.numeroFaces())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Generos
        for (ConfigExportResponse.GeneroExport item : request.generos()) {
            if (!generoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                generoRepository.save(GeneroConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Indoles
        for (ConfigExportResponse.IndoleExport item : request.indoles()) {
            if (!indoleRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                indoleRepository.save(IndoleConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // MembrosCorpo
        for (ConfigExportResponse.MembroCorpoExport item : request.membrosCorpo()) {
            if (!membroCorpoRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                membroCorpoRepository.save(MembroCorpoConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .porcentagemVida(item.porcentagemVida())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Niveis
        for (ConfigExportResponse.NivelExport item : request.niveis()) {
            if (!nivelRepository.existsByJogoIdAndNivel(jogoId, item.nivel())) {
                nivelRepository.save(NivelConfig.builder()
                        .jogo(jogo)
                        .nivel(item.nivel())
                        .xpNecessaria(item.xpNecessaria())
                        .pontosAtributo(item.pontosAtributo() != null ? item.pontosAtributo() : 3)
                        .pontosAptidao(item.pontosAptidao() != null ? item.pontosAptidao() : 3)
                        .limitadorAtributo(item.limitadorAtributo())
                        .build());
            }
        }

        // Presencas
        for (ConfigExportResponse.PresencaExport item : request.presencas()) {
            if (!presencaRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                presencaRepository.save(PresencaConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Racas
        for (ConfigExportResponse.RacaExport item : request.racas()) {
            if (!racaRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                racaRepository.save(Raca.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        // Vantagens
        for (ConfigExportResponse.VantagemExport item : request.vantagens()) {
            if (!vantagemRepository.existsByJogoIdAndNomeIgnoreCase(jogoId, item.nome())) {
                vantagemRepository.save(VantagemConfig.builder()
                        .jogo(jogo)
                        .nome(item.nome())
                        .descricao(item.descricao())
                        .sigla(item.sigla())
                        .nivelMaximo(item.nivelMaximo() != null ? item.nivelMaximo() : 10)
                        .formulaCusto(item.formulaCusto())
                        .descricaoEfeito(item.descricaoEfeito())
                        .ordemExibicao(item.ordemExibicao() != null ? item.ordemExibicao() : 0)
                        .build());
            }
        }

        log.info("Configurações importadas para o jogo ID={}", jogoId);
    }

    private void verificarAcessoMestre(Long jogoId) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode exportar/importar configurações.");
        }
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }
}
