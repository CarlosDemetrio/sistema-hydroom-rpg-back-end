package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service para preview de cálculos da ficha sem persistir.
 *
 * <p>Aplica as mudanças informadas no request em memória, recalcula todos
 * os valores derivados e retorna o resultado sem gravar no banco.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaPreviewService {

    private final FichaRepository fichaRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final FichaAptidaoRepository fichaAptidaoRepository;
    private final FichaBonusRepository fichaBonusRepository;
    private final FichaVidaRepository fichaVidaRepository;
    private final FichaVidaMembroRepository fichaVidaMembroRepository;
    private final FichaEssenciaRepository fichaEssenciaRepository;
    private final FichaAmeacaRepository fichaAmeacaRepository;
    private final ConfiguracaoNivelRepository nivelConfigRepository;

    private final FichaCalculationService calculationService;

    /**
     * Simula o recálculo da ficha com as mudanças informadas, sem persistir.
     *
     * @param fichaId ID da ficha base
     * @param request mudanças a aplicar em memória
     * @return valores recalculados
     */
    public FichaPreviewResponse simular(Long fichaId, FichaPreviewRequest request) {
        // 1. Carregar ficha
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        // 2. Carregar sub-registros
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaId(fichaId);
        FichaVida vida = fichaVidaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("FichaVida não encontrada para ficha: " + fichaId));
        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaId(fichaId);
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("FichaEssencia não encontrada para ficha: " + fichaId));
        FichaAmeaca ameaca = fichaAmeacaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("FichaAmeaca não encontrada para ficha: " + fichaId));

        // 3. Criar cópias em memória para não contaminar o contexto JPA
        Ficha fichaSimulada = clonarFicha(ficha);
        List<FichaAtributo> atributosSimulados = atributos.stream().map(this::clonarAtributo).toList();
        List<FichaBonus> bonusSimulados = bonus.stream().map(this::clonarBonus).toList();
        FichaVida vidaSimulada = clonarVida(vida);
        List<FichaVidaMembro> membrosSimulados = membros.stream().map(this::clonarVidaMembro).toList();
        FichaEssencia essenciaSimulada = clonarEssencia(essencia);
        FichaAmeaca ameacaSimulada = clonarAmeaca(ameaca);

        // 4. Aplicar mudanças da request em memória
        if (request != null) {
            // Atualizar bases de atributos
            Map<Long, Integer> atributoBase = request.atributoBase();
            if (atributoBase != null && !atributoBase.isEmpty()) {
                for (FichaAtributo atributo : atributosSimulados) {
                    Integer novaBase = atributoBase.get(atributo.getId());
                    if (novaBase != null) {
                        atributo.setBase(novaBase);
                    }
                }
            }

            // Atualizar bases de aptidões (não afetam cálculos de vida/essência/ameaça diretamente)
            // mas afetam validações - tratado na validação, não no preview de cálculo

            // Atualizar XP e nível
            if (request.xp() != null) {
                fichaSimulada.setXp(request.xp());
                // Recalcular nível com base na nova XP
                Optional<NivelConfig> nivelConfig = nivelConfigRepository.findNivelPorExperiencia(
                        ficha.getJogo().getId(), request.xp());
                nivelConfig.ifPresent(nc -> fichaSimulada.setNivel(nc.getNivel()));
            }
        }

        // 5. Recalcular tudo em memória
        calculationService.recalcular(
                fichaSimulada, atributosSimulados, bonusSimulados,
                vidaSimulada, membrosSimulados, essenciaSimulada, ameacaSimulada);

        // 6. Montar resposta
        return montarResposta(fichaSimulada, atributosSimulados, bonusSimulados,
                vidaSimulada, membrosSimulados, essenciaSimulada, ameacaSimulada);
    }

    // ==================== CLONAGEM EM MEMÓRIA ====================

    private Ficha clonarFicha(Ficha original) {
        return Ficha.builder()
                .id(original.getId())
                .jogo(original.getJogo())
                .nome(original.getNome())
                .jogadorId(original.getJogadorId())
                .raca(original.getRaca())
                .classe(original.getClasse())
                .genero(original.getGenero())
                .indole(original.getIndole())
                .presenca(original.getPresenca())
                .nivel(original.getNivel())
                .xp(original.getXp())
                .renascimentos(original.getRenascimentos())
                .isNpc(original.isNpc())
                .build();
    }

    private FichaAtributo clonarAtributo(FichaAtributo original) {
        return FichaAtributo.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .atributoConfig(original.getAtributoConfig())
                .base(original.getBase())
                .nivel(original.getNivel())
                .outros(original.getOutros())
                .total(original.getTotal())
                .impeto(original.getImpeto())
                .build();
    }

    private FichaBonus clonarBonus(FichaBonus original) {
        return FichaBonus.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .bonusConfig(original.getBonusConfig())
                .base(original.getBase())
                .vantagens(original.getVantagens())
                .classe(original.getClasse())
                .itens(original.getItens())
                .gloria(original.getGloria())
                .outros(original.getOutros())
                .total(original.getTotal())
                .build();
    }

    private FichaVida clonarVida(FichaVida original) {
        return FichaVida.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .vt(original.getVt())
                .outros(original.getOutros())
                .vidaTotal(original.getVidaTotal())
                .build();
    }

    private FichaVidaMembro clonarVidaMembro(FichaVidaMembro original) {
        return FichaVidaMembro.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .membroCorpoConfig(original.getMembroCorpoConfig())
                .vida(original.getVida())
                .danoRecebido(original.getDanoRecebido())
                .build();
    }

    private FichaEssencia clonarEssencia(FichaEssencia original) {
        return FichaEssencia.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .renascimentos(original.getRenascimentos())
                .vantagens(original.getVantagens())
                .outros(original.getOutros())
                .total(original.getTotal())
                .build();
    }

    private FichaAmeaca clonarAmeaca(FichaAmeaca original) {
        return FichaAmeaca.builder()
                .id(original.getId())
                .ficha(original.getFicha())
                .itens(original.getItens())
                .titulos(original.getTitulos())
                .renascimentos(original.getRenascimentos())
                .outros(original.getOutros())
                .total(original.getTotal())
                .build();
    }

    // ==================== MONTAR RESPOSTA ====================

    private FichaPreviewResponse montarResposta(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            FichaAmeaca ameaca) {

        List<FichaPreviewResponse.AtributoPreview> atributosPrev = atributos.stream()
                .map(a -> new FichaPreviewResponse.AtributoPreview(
                        a.getId(),
                        a.getAtributoConfig() != null ? a.getAtributoConfig().getId() : null,
                        a.getAtributoConfig() != null ? a.getAtributoConfig().getNome() : null,
                        a.getAtributoConfig() != null ? a.getAtributoConfig().getAbreviacao() : null,
                        a.getBase(),
                        a.getNivel(),
                        a.getOutros(),
                        a.getTotal(),
                        a.getImpeto()
                ))
                .toList();

        List<FichaPreviewResponse.BonusPreview> bonusPrev = bonus.stream()
                .map(b -> new FichaPreviewResponse.BonusPreview(
                        b.getId(),
                        b.getBonusConfig() != null ? b.getBonusConfig().getId() : null,
                        b.getBonusConfig() != null ? b.getBonusConfig().getNome() : null,
                        b.getBonusConfig() != null ? b.getBonusConfig().getSigla() : null,
                        b.getBase(),
                        b.getVantagens(),
                        b.getClasse(),
                        b.getItens(),
                        b.getGloria(),
                        b.getOutros(),
                        b.getTotal()
                ))
                .toList();

        List<FichaPreviewResponse.MembroPreview> membrosPrev = membros.stream()
                .map(m -> new FichaPreviewResponse.MembroPreview(
                        m.getId(),
                        m.getMembroCorpoConfig() != null ? m.getMembroCorpoConfig().getId() : null,
                        m.getMembroCorpoConfig() != null ? m.getMembroCorpoConfig().getNome() : null,
                        m.getVida()
                ))
                .toList();

        FichaPreviewResponse.VidaPreview vidaPrev = new FichaPreviewResponse.VidaPreview(
                vida.getVt(),
                vida.getOutros(),
                vida.getVidaTotal(),
                membrosPrev
        );

        FichaPreviewResponse.EssenciaPreview essenciaPrev = new FichaPreviewResponse.EssenciaPreview(
                essencia.getVantagens(),
                essencia.getOutros(),
                essencia.getTotal()
        );

        FichaPreviewResponse.AmeacaPreview ameacaPrev = new FichaPreviewResponse.AmeacaPreview(
                ameaca.getItens(),
                ameaca.getTitulos(),
                ameaca.getRenascimentos(),
                ameaca.getOutros(),
                ameaca.getTotal()
        );

        return new FichaPreviewResponse(
                ficha.getId(),
                ficha.getNivel(),
                ficha.getXp(),
                atributosPrev,
                bonusPrev,
                vidaPrev,
                essenciaPrev,
                ameacaPrev
        );
    }
}
