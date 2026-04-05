package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsável pela duplicação de jogos com todas as configurações.
 * Copia as 13 configurações do jogo original para um novo jogo.
 * NÃO copia fichas, participantes nem pré-requisitos de vantagens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JogoDuplicacaoService {

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
     * Duplica um jogo com todas as suas configurações.
     * O usuário atual torna-se o MESTRE do novo jogo.
     *
     * @param jogoOrigemId ID do jogo a ser duplicado
     * @param novoNome     Nome do novo jogo
     * @return Novo jogo criado
     */
    @Transactional
    public Jogo duplicar(Long jogoOrigemId, String novoNome) {
        Usuario mestreAtual = getUsuarioAtual();

        // 1. Verificar que o jogo origem existe e o usuário tem acesso
        Jogo jogoOrigem = jogoRepository.findById(jogoOrigemId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoOrigemId));

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoOrigemId, mestreAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre do jogo pode duplicá-lo.");
        }

        // 2. Criar o novo jogo
        Jogo novoJogo = Jogo.builder()
                .nome(novoNome)
                .descricao(jogoOrigem.getDescricao())
                .jogoAtivo(false)
                .build();
        novoJogo = jogoRepository.save(novoJogo);
        log.info("Novo jogo criado: ID={}, nome='{}'", novoJogo.getId(), novoJogo.getNome());

        // 3. Criar participação do mestre no novo jogo
        JogoParticipante participacao = JogoParticipante.builder()
                .jogo(novoJogo)
                .usuario(mestreAtual)
                .role(RoleJogo.MESTRE)
                .status(StatusParticipante.APROVADO)
                .build();
        jogoParticipanteRepository.save(participacao);

        // 4. Copiar todas as configurações
        copiarAtributos(jogoOrigemId, novoJogo);
        copiarTiposAptidao(jogoOrigemId, novoJogo);
        copiarAptidoes(jogoOrigemId, novoJogo);
        copiarBonus(jogoOrigemId, novoJogo);
        copiarClasses(jogoOrigemId, novoJogo);
        copiarDadosProspeccao(jogoOrigemId, novoJogo);
        copiarGeneros(jogoOrigemId, novoJogo);
        copiarIndoles(jogoOrigemId, novoJogo);
        copiarMembrosCorpo(jogoOrigemId, novoJogo);
        copiarNiveis(jogoOrigemId, novoJogo);
        copiarPresencas(jogoOrigemId, novoJogo);
        copiarRacas(jogoOrigemId, novoJogo);
        copiarVantagens(jogoOrigemId, novoJogo);

        log.info("Jogo duplicado com sucesso: origem={}, novo={}", jogoOrigemId, novoJogo.getId());
        return novoJogo;
    }

    private void copiarAtributos(Long jogoOrigemId, Jogo novoJogo) {
        List<AtributoConfig> lista = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (AtributoConfig original : lista) {
            AtributoConfig copia = AtributoConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .abreviacao(original.getAbreviacao())
                    .descricao(original.getDescricao())
                    .formulaImpeto(original.getFormulaImpeto())
                    .descricaoImpeto(original.getDescricaoImpeto())
                    .valorMinimo(original.getValorMinimo())
                    .valorMaximo(original.getValorMaximo())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            atributoRepository.save(copia);
        }
    }

    private void copiarTiposAptidao(Long jogoOrigemId, Jogo novoJogo) {
        List<TipoAptidao> lista = tipoAptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (TipoAptidao original : lista) {
            TipoAptidao copia = TipoAptidao.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            tipoAptidaoRepository.save(copia);
        }
    }

    private void copiarAptidoes(Long jogoOrigemId, Jogo novoJogo) {
        List<AptidaoConfig> lista = aptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (AptidaoConfig original : lista) {
            // Buscar TipoAptidao correspondente no novo jogo pelo nome
            List<TipoAptidao> tiposNovoJogo = tipoAptidaoRepository.findByJogoIdOrderByOrdemExibicao(novoJogo.getId());
            TipoAptidao tipoNovoJogo = tiposNovoJogo.stream()
                    .filter(t -> t.getNome().equalsIgnoreCase(original.getTipoAptidao().getNome()))
                    .findFirst()
                    .orElse(null);

            if (tipoNovoJogo == null) {
                log.warn("TipoAptidao '{}' não encontrado no novo jogo, aptidão '{}' ignorada.",
                        original.getTipoAptidao().getNome(), original.getNome());
                continue;
            }

            AptidaoConfig copia = AptidaoConfig.builder()
                    .jogo(novoJogo)
                    .tipoAptidao(tipoNovoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            aptidaoRepository.save(copia);
        }
    }

    private void copiarBonus(Long jogoOrigemId, Jogo novoJogo) {
        List<BonusConfig> lista = bonusRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (BonusConfig original : lista) {
            BonusConfig copia = BonusConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .sigla(original.getSigla())
                    .formulaBase(original.getFormulaBase())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            bonusRepository.save(copia);
        }
    }

    private void copiarClasses(Long jogoOrigemId, Jogo novoJogo) {
        List<ClassePersonagem> lista = classeRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (ClassePersonagem original : lista) {
            ClassePersonagem copia = ClassePersonagem.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            classeRepository.save(copia);
        }
    }

    private void copiarDadosProspeccao(Long jogoOrigemId, Jogo novoJogo) {
        List<DadoProspeccaoConfig> lista = dadoProspeccaoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (DadoProspeccaoConfig original : lista) {
            DadoProspeccaoConfig copia = DadoProspeccaoConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .numeroFaces(original.getNumeroFaces())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            dadoProspeccaoRepository.save(copia);
        }
    }

    private void copiarGeneros(Long jogoOrigemId, Jogo novoJogo) {
        List<GeneroConfig> lista = generoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (GeneroConfig original : lista) {
            GeneroConfig copia = GeneroConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            generoRepository.save(copia);
        }
    }

    private void copiarIndoles(Long jogoOrigemId, Jogo novoJogo) {
        List<IndoleConfig> lista = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (IndoleConfig original : lista) {
            IndoleConfig copia = IndoleConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            indoleRepository.save(copia);
        }
    }

    private void copiarMembrosCorpo(Long jogoOrigemId, Jogo novoJogo) {
        List<MembroCorpoConfig> lista = membroCorpoRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (MembroCorpoConfig original : lista) {
            MembroCorpoConfig copia = MembroCorpoConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .porcentagemVida(original.getPorcentagemVida())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            membroCorpoRepository.save(copia);
        }
    }

    private void copiarNiveis(Long jogoOrigemId, Jogo novoJogo) {
        List<NivelConfig> lista = nivelRepository.findByJogoIdOrderByNivel(jogoOrigemId);
        for (NivelConfig original : lista) {
            NivelConfig copia = NivelConfig.builder()
                    .jogo(novoJogo)
                    .nivel(original.getNivel())
                    .xpNecessaria(original.getXpNecessaria())
                    .pontosAtributo(original.getPontosAtributo())
                    .pontosAptidao(original.getPontosAptidao())
                    .limitadorAtributo(original.getLimitadorAtributo())
                    .build();
            nivelRepository.save(copia);
        }
    }

    private void copiarPresencas(Long jogoOrigemId, Jogo novoJogo) {
        List<PresencaConfig> lista = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (PresencaConfig original : lista) {
            PresencaConfig copia = PresencaConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            presencaRepository.save(copia);
        }
    }

    private void copiarRacas(Long jogoOrigemId, Jogo novoJogo) {
        List<Raca> lista = racaRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (Raca original : lista) {
            Raca copia = Raca.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            racaRepository.save(copia);
        }
    }

    private void copiarVantagens(Long jogoOrigemId, Jogo novoJogo) {
        List<VantagemConfig> lista = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoOrigemId);
        for (VantagemConfig original : lista) {
            VantagemConfig copia = VantagemConfig.builder()
                    .jogo(novoJogo)
                    .nome(original.getNome())
                    .descricao(original.getDescricao())
                    .sigla(original.getSigla())
                    .nivelMaximo(original.getNivelMaximo())
                    .formulaCusto(original.getFormulaCusto())
                    .descricaoEfeito(original.getDescricaoEfeito())
                    .ordemExibicao(original.getOrdemExibicao())
                    .build();
            vantagemRepository.save(copia);
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
