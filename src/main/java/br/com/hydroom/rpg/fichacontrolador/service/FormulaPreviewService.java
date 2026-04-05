package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.FormulaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaInfoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VariaveisDisponiveisResponse;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoFormula;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serviço para preview e listagem de variáveis disponíveis para fórmulas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FormulaPreviewService {

    private static final List<String> VARIAVEIS_FIXAS =
        List.of("total", "nivel", "base", "custo_base", "nivel_vantagem");

    private final FormulaEvaluatorService formulaEvaluator;
    private final ConfiguracaoAtributoRepository atributoRepo;
    private final BonusConfigRepository bonusRepo;
    private final VantagemConfigRepository vantagemRepo;

    /**
     * Valida e calcula uma fórmula com valores de teste.
     *
     * @param jogoId  ID do jogo (para resolver variáveis dinâmicas)
     * @param request dados da fórmula e valores de teste
     * @return resultado da validação e cálculo
     */
    public FormulaPreviewResponse preview(Long jogoId, FormulaPreviewRequest request) {
        Set<String> permitidas = resolverVariaveisPermitidas(jogoId, request.tipo());

        FormulaValidationResult validation = formulaEvaluator.validarFormula(request.formula(), permitidas);
        if (!validation.valid()) {
            List<String> erros = new ArrayList<>();
            if (validation.erroSintaxe() != null) {
                erros.add(validation.erroSintaxe());
            }
            validation.variaveisInvalidas().forEach(v -> erros.add("Variável não reconhecida: " + v));
            return new FormulaPreviewResponse(false, null, erros, Set.of());
        }

        // Calcular com valores fornecidos, usando 0.0 como default para variáveis não informadas
        Map<String, Double> valores = new HashMap<>();
        permitidas.forEach(v -> valores.put(v, 0.0));
        if (request.valores() != null) {
            valores.putAll(request.valores());
        }

        try {
            double resultado = formulaEvaluator.evaluate(request.formula(), valores);
            return new FormulaPreviewResponse(true, resultado, List.of(), permitidas);
        } catch (Exception e) {
            log.warn("Erro ao calcular preview de fórmula '{}': {}", request.formula(), e.getMessage());
            return new FormulaPreviewResponse(false, null,
                List.of("Erro ao calcular: " + e.getMessage()), Set.of());
        }
    }

    /**
     * Lista variáveis disponíveis para fórmulas do jogo, agrupadas por tipo.
     *
     * @param jogoId ID do jogo
     * @return variáveis agrupadas por tipo
     */
    public VariaveisDisponiveisResponse listarVariaveis(Long jogoId) {
        List<SiglaInfoResponse> atributos = atributoRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        List<SiglaInfoResponse> bonus = bonusRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        List<SiglaInfoResponse> vantagens = vantagemRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        return new VariaveisDisponiveisResponse(atributos, bonus, vantagens, VARIAVEIS_FIXAS);
    }

    private Set<String> resolverVariaveisPermitidas(Long jogoId, TipoFormula tipo) {
        return switch (tipo) {
            case IMPETO -> Set.of("total");
            case BONUS -> {
                List<String> siglas = atributoRepo.findAbreviacoesByJogoId(jogoId);
                Set<String> vars = new HashSet<>(siglas);
                vars.addAll(Set.of("nivel", "base"));
                yield vars;
            }
            case CUSTO_VANTAGEM -> Set.of("custo_base", "nivel_vantagem");
        };
    }
}
