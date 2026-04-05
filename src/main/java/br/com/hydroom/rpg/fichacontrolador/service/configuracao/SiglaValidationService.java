package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço responsável pela validação de unicidade de siglas cross-entity por jogo.
 *
 * <p>Regra crítica: siglas devem ser únicas por jogo, independentemente do tipo de entidade.
 * Se "FOR" existe como abreviação de atributo, nenhum bônus ou vantagem do mesmo jogo pode usar "FOR".</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiglaValidationService {

    public enum TipoSigla {
        ATRIBUTO, BONUS, VANTAGEM
    }

    private final ConfiguracaoAtributoRepository atributoRepository;
    private final BonusConfigRepository bonusRepository;
    private final VantagemConfigRepository vantagemRepository;

    /**
     * Valida que a sigla não está em uso em nenhuma entidade do jogo,
     * exceto a entidade sendo editada (identificada por excludeId + tipo).
     *
     * @param sigla     sigla a validar (case-insensitive); null ou blank é aceito sem validação
     * @param jogoId    ID do jogo
     * @param excludeId ID da entidade atual para exclusão da busca (null se create)
     * @param tipo      tipo da entidade chamadora (para excluir ela mesma corretamente)
     * @throws ConflictException se a sigla já estiver em uso em qualquer entidade do jogo
     */
    public void validarSiglaDisponivel(String sigla, Long jogoId, Long excludeId, TipoSigla tipo) {
        if (sigla == null || sigla.isBlank()) return;

        String siglaUpper = sigla.toUpperCase();
        log.debug("Validando sigla '{}' para jogo {} (excludeId={}, tipo={})", siglaUpper, jogoId, excludeId, tipo);

        // Verificar atributos
        if (tipo == TipoSigla.ATRIBUTO && excludeId != null) {
            if (atributoRepository.existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot(jogoId, sigla, excludeId)) {
                lançarConflito(siglaUpper, "atributo");
            }
        } else {
            if (atributoRepository.existsByJogoIdAndAbreviacaoIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "atributo");
            }
        }

        // Verificar bônus
        if (tipo == TipoSigla.BONUS && excludeId != null) {
            if (bonusRepository.existsByJogoIdAndSiglaIgnoreCaseAndIdNot(jogoId, sigla, excludeId)) {
                lançarConflito(siglaUpper, "bônus");
            }
        } else {
            if (bonusRepository.existsByJogoIdAndSiglaIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "bônus");
            }
        }

        // Verificar vantagens
        if (tipo == TipoSigla.VANTAGEM && excludeId != null) {
            if (vantagemRepository.existsByJogoIdAndSiglaIgnoreCaseAndIdNot(jogoId, sigla, excludeId)) {
                lançarConflito(siglaUpper, "vantagem");
            }
        } else {
            if (vantagemRepository.existsByJogoIdAndSiglaIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "vantagem");
            }
        }
    }

    /**
     * Lista todas as siglas em uso no jogo, combinando atributos, bônus e vantagens,
     * ordenadas alfabeticamente pela sigla.
     *
     * @param jogoId ID do jogo
     * @return lista ordenada de siglas em uso
     */
    public List<SiglaEmUsoResponse> listarSiglasDoJogo(Long jogoId) {
        List<SiglaEmUsoResponse> todas = new ArrayList<>();
        todas.addAll(atributoRepository.findSiglasComInfoByJogoId(jogoId));
        todas.addAll(bonusRepository.findSiglasComInfoByJogoId(jogoId));
        todas.addAll(vantagemRepository.findSiglasComInfoByJogoId(jogoId));
        return todas.stream()
                .sorted(Comparator.comparing(SiglaEmUsoResponse::sigla))
                .toList();
    }

    private void lançarConflito(String sigla, String entidade) {
        throw new ConflictException(
            ValidationMessages.Sigla.SIGLA_JA_EM_USO.formatted(sigla, entidade)
        );
    }
}
