package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVisibilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsável por gerar o resumo completo de uma ficha.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaResumoService {

    private final FichaRepository fichaRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final FichaAptidaoRepository fichaAptidaoRepository;
    private final FichaBonusRepository fichaBonusRepository;
    private final FichaVidaRepository fichaVidaRepository;
    private final FichaEssenciaRepository fichaEssenciaRepository;
    private final FichaAmeacaRepository fichaAmeacaRepository;
    private final FichaVantagemRepository fichaVantagemRepository;
    private final ConfiguracaoNivelRepository nivelConfigRepository;
    private final PontosVantagemConfigRepository pontosVantagemConfigRepository;
    private final ClassePontosConfigRepository classePontosConfigRepository;
    private final RacaPontosConfigRepository racaPontosConfigRepository;
    private final FichaVisibilidadeRepository fichaVisibilidadeRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    public FichaResumoResponse getResumo(Long fichaId) {
        Ficha ficha = fichaRepository.findByIdWithRelationships(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        // Atributos: abreviação → total (JOIN FETCH evita N+1 ao acessar atributoConfig.abreviacao)
        List<FichaAtributo> fichaAtributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
        Map<String, Integer> atributosTotais = new LinkedHashMap<>();
        for (FichaAtributo fa : fichaAtributos) {
            String abreviacao = fa.getAtributoConfig() != null ? fa.getAtributoConfig().getAbreviacao() : null;
            if (abreviacao != null) {
                atributosTotais.put(abreviacao, fa.getTotal() != null ? fa.getTotal() : 0);
            }
        }

        // Bônus: nome → total (JOIN FETCH evita N+1 ao acessar bonusConfig.nome)
        List<FichaBonus> fichaBonus = fichaBonusRepository.findByFichaIdWithConfig(fichaId);
        Map<String, Integer> bonusTotais = new LinkedHashMap<>();
        for (FichaBonus fb : fichaBonus) {
            String nome = fb.getBonusConfig() != null ? fb.getBonusConfig().getNome() : null;
            if (nome != null) {
                bonusTotais.put(nome, fb.getTotal() != null ? fb.getTotal() : 0);
            }
        }

        // Vida
        FichaVida vida = fichaVidaRepository.findByFichaId(fichaId).orElse(null);
        int vidaTotal = vida != null && vida.getVidaTotal() != null ? vida.getVidaTotal() : 0;
        int vidaAtual = vida != null && vida.getVidaAtual() != null ? vida.getVidaAtual() : 0;

        // Essência
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId).orElse(null);
        int essenciaTotal = essencia != null && essencia.getTotal() != null ? essencia.getTotal() : 0;
        int essenciaAtual = essencia != null && essencia.getEssenciaAtual() != null ? essencia.getEssenciaAtual() : 0;

        // Ameaça
        FichaAmeaca ameaca = fichaAmeacaRepository.findByFichaId(fichaId).orElse(null);
        int ameacaTotal = ameaca != null && ameaca.getTotal() != null ? ameaca.getTotal() : 0;

        String racaNome = ficha.getRaca() != null ? ficha.getRaca().getNome() : null;
        String classeNome = ficha.getClasse() != null ? ficha.getClasse().getNome() : null;

        // Calcular pontos disponiveis
        int nivelAtual = ficha.getNivel() != null ? ficha.getNivel() : 1;
        Long jogoId = ficha.getJogo().getId();

        // Pontos de atributo: concedidos pelo NivelConfig - gastos (SUM(base + nivel) de cada FichaAtributo)
        NivelConfig nivelConfig = nivelConfigRepository
                .findByJogoIdAndNivel(jogoId, nivelAtual)
                .orElse(null);

        // --- Pontos base do NivelConfig ---
        int pontosAtributoBase = nivelConfig != null && nivelConfig.getPontosAtributo() != null
                ? nivelConfig.getPontosAtributo() : 0;

        // --- Pontos extras de ClassePontosConfig (para niveis <= nivelAtual) ---
        int classePontosAtributo = 0;
        int classePontosVantagem = 0;
        if (ficha.getClasse() != null) {
            List<ClassePontosConfig> classePontos = classePontosConfigRepository
                    .findByClassePersonagemIdAndNivelLessThanEqual(ficha.getClasse().getId(), nivelAtual);
            classePontosAtributo = classePontos.stream()
                    .mapToInt(c -> c.getPontosAtributo() != null ? c.getPontosAtributo() : 0).sum();
            classePontosVantagem = classePontos.stream()
                    .mapToInt(c -> c.getPontosVantagem() != null ? c.getPontosVantagem() : 0).sum();
        }

        // --- Pontos extras de RacaPontosConfig (para niveis <= nivelAtual) ---
        int racaPontosAtributo = 0;
        int racaPontosVantagem = 0;
        if (ficha.getRaca() != null) {
            List<RacaPontosConfig> racaPontos = racaPontosConfigRepository
                    .findByRacaIdAndNivelLessThanEqual(ficha.getRaca().getId(), nivelAtual);
            racaPontosAtributo = racaPontos.stream()
                    .mapToInt(r -> r.getPontosAtributo() != null ? r.getPontosAtributo() : 0).sum();
            racaPontosVantagem = racaPontos.stream()
                    .mapToInt(r -> r.getPontosVantagem() != null ? r.getPontosVantagem() : 0).sum();
        }

        // Total pontos atributo = NivelConfig + ClassePontosConfig + RacaPontosConfig
        int pontosAtributoTotais = pontosAtributoBase + classePontosAtributo + racaPontosAtributo;
        int pontosAtributoGastos = fichaAtributos.stream()
                .mapToInt(a -> (a.getBase() != null ? a.getBase() : 0)
                        + (a.getNivel() != null ? a.getNivel() : 0))
                .sum();
        int pontosAtributoDisponiveis = Math.max(0, pontosAtributoTotais - pontosAtributoGastos);

        // Pontos de aptidao: APENAS NivelConfig (aptidoes independentes de classe/raca - decisao PO)
        int pontosAptidaoTotais = nivelConfig != null && nivelConfig.getPontosAptidao() != null
                ? nivelConfig.getPontosAptidao() : 0;
        List<FichaAptidao> fichaAptidoes = fichaAptidaoRepository.findByFichaId(fichaId);
        int pontosAptidaoGastos = fichaAptidoes.stream()
                .mapToInt(a -> a.getBase() != null ? a.getBase() : 0)
                .sum();
        int pontosAptidaoDisponiveis = Math.max(0, pontosAptidaoTotais - pontosAptidaoGastos);

        // Pontos de vantagem: PontosVantagemConfig + ClassePontosConfig + RacaPontosConfig - gastos
        List<PontosVantagemConfig> pontosVantagemConfigs = pontosVantagemConfigRepository
                .findByJogoIdOrderByNivel(jogoId);
        int pontosVantagemBase = pontosVantagemConfigs.stream()
                .filter(p -> p.getNivel() != null && p.getNivel() <= nivelAtual)
                .mapToInt(p -> p.getPontosGanhos() != null ? p.getPontosGanhos() : 0)
                .sum();
        int pontosVantagemTotais = pontosVantagemBase + classePontosVantagem + racaPontosVantagem;
        List<FichaVantagem> fichaVantagens = fichaVantagemRepository.findByFichaIdWithConfig(fichaId);
        int pontosVantagemGastos = fichaVantagens.stream()
                .mapToInt(v -> v.getCustoPago() != null ? v.getCustoPago() : 0)
                .sum();
        int pontosVantagemDisponiveis = Math.max(0, pontosVantagemTotais - pontosVantagemGastos);

        return new FichaResumoResponse(
                ficha.getId(),
                ficha.getNome(),
                nivelAtual,
                ficha.getXp() != null ? ficha.getXp() : 0L,
                racaNome,
                classeNome,
                atributosTotais,
                bonusTotais,
                vidaAtual,
                vidaTotal,
                essenciaAtual,
                essenciaTotal,
                ameacaTotal,
                pontosAtributoDisponiveis,
                pontosAptidaoDisponiveis,
                pontosVantagemDisponiveis
        );
    }

    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        // NPCs: Jogadores com FichaVisibilidade ativa podem acessar o resumo
        if (ficha.isNpc()) {
            if (!fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(
                    ficha.getId(), usuarioAtual.getId())) {
                throw new ForbiddenException("Acesso negado: você não tem acesso às estatísticas deste NPC.");
            }
            return;
        }

        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
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
