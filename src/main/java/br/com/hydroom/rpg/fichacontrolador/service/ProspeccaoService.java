package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UsarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ProspeccaoUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import br.com.hydroom.rpg.fichacontrolador.model.ProspeccaoUso;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.ProspeccaoUsoStatus;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaProspeccaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ProspeccaoUsoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsável pela semântica de uso de dados de prospecção.
 *
 * <p>Fluxo principal:</p>
 * <ol>
 *   <li>Jogador usa dado → status PENDENTE, quantidade decrementada</li>
 *   <li>Mestre confirma → status CONFIRMADO (quantidade inalterada)</li>
 *   <li>Mestre reverte → status REVERTIDO, quantidade restaurada</li>
 * </ol>
 *
 * <p>O Mestre também pode conceder dados diretamente (sem registro de uso).</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProspeccaoService {

    private final FichaRepository fichaRepository;
    private final FichaProspeccaoRepository fichaProspeccaoRepository;
    private final ProspeccaoUsoRepository prospeccaoUsoRepository;
    private final DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FichaResumoService fichaResumoService;

    /**
     * Registra o uso de um dado de prospecção.
     *
     * <p>Decrementa a quantidade disponível e cria um registro com status PENDENTE.
     * O Mestre pode usar o dado de qualquer ficha; o Jogador apenas da própria ficha.</p>
     */
    @Transactional
    public ProspeccaoUsoResponse usar(Long fichaId, UsarProspeccaoRequest request) {
        Ficha ficha = buscarFicha(fichaId);
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            // Jogador só pode usar dado da própria ficha
            if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
                throw new ForbiddenException("Acesso negado: você só pode usar dados de prospecção das suas próprias fichas.");
            }
        }

        FichaProspeccao fichaProspeccao = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaId, request.dadoProspeccaoConfigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prospecção não encontrada para ficha " + fichaId
                                + " e dado " + request.dadoProspeccaoConfigId()));

        if (fichaProspeccao.getQuantidade() <= 0) {
            throw new ValidationException("Quantidade insuficiente de dados de prospecção para usar.");
        }

        fichaProspeccao.setQuantidade(fichaProspeccao.getQuantidade() - 1);
        fichaProspeccaoRepository.save(fichaProspeccao);

        ProspeccaoUso uso = ProspeccaoUso.builder()
                .fichaProspeccao(fichaProspeccao)
                .status(ProspeccaoUsoStatus.PENDENTE)
                .build();
        uso = prospeccaoUsoRepository.save(uso);

        log.info("Dado de prospecção usado: fichaId={}, dadoId={}, usoId={}",
                fichaId, request.dadoProspeccaoConfigId(), uso.getId());

        return toResponse(uso, ficha);
    }

    /**
     * Reverte um uso de prospecção com status PENDENTE.
     *
     * <p>Restaura a quantidade da FichaProspeccao. Apenas o Mestre pode reverter.</p>
     */
    @Transactional
    public ProspeccaoUsoResponse reverter(Long fichaId, Long usoId) {
        ProspeccaoUso uso = buscarUso(usoId);
        Ficha ficha = uso.getFichaProspeccao().getFicha();

        verificarUsoPertencenteAFicha(uso, fichaId);
        verificarAcessoMestre(ficha);

        if (uso.getStatus() != ProspeccaoUsoStatus.PENDENTE) {
            throw new ValidationException(
                    "Não é possível reverter um uso com status " + uso.getStatus() + ". Apenas usos PENDENTES podem ser revertidos.");
        }

        uso.setStatus(ProspeccaoUsoStatus.REVERTIDO);
        prospeccaoUsoRepository.save(uso);

        FichaProspeccao fichaProspeccao = uso.getFichaProspeccao();
        fichaProspeccao.setQuantidade(fichaProspeccao.getQuantidade() + 1);
        fichaProspeccaoRepository.save(fichaProspeccao);

        log.info("Uso de prospecção revertido: usoId={}, fichaId={}", usoId, fichaId);

        return toResponse(uso, ficha);
    }

    /**
     * Confirma um uso de prospecção com status PENDENTE.
     *
     * <p>Não altera a quantidade. Apenas o Mestre pode confirmar.</p>
     */
    @Transactional
    public ProspeccaoUsoResponse confirmar(Long fichaId, Long usoId) {
        ProspeccaoUso uso = buscarUso(usoId);
        Ficha ficha = uso.getFichaProspeccao().getFicha();

        verificarUsoPertencenteAFicha(uso, fichaId);
        verificarAcessoMestre(ficha);

        if (uso.getStatus() != ProspeccaoUsoStatus.PENDENTE) {
            throw new ValidationException(
                    "Não é possível confirmar um uso com status " + uso.getStatus() + ". Apenas usos PENDENTES podem ser confirmados.");
        }

        uso.setStatus(ProspeccaoUsoStatus.CONFIRMADO);
        prospeccaoUsoRepository.save(uso);

        log.info("Uso de prospecção confirmado: usoId={}, fichaId={}", usoId, fichaId);

        return toResponse(uso, ficha);
    }

    /**
     * Concede dados de prospecção a uma ficha.
     *
     * <p>Se não existir registro, cria com quantidade=0 e então incrementa.
     * Apenas o Mestre pode conceder.</p>
     */
    @Transactional
    public FichaResumoResponse conceder(Long fichaId, ConcederProspeccaoRequest request) {
        Ficha ficha = buscarFicha(fichaId);
        verificarAcessoMestre(ficha);

        DadoProspeccaoConfig dadoConfig = dadoProspeccaoConfigRepository.findById(request.dadoProspeccaoConfigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DadoProspeccaoConfig não encontrado: " + request.dadoProspeccaoConfigId()));

        FichaProspeccao fichaProspeccao = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaId, request.dadoProspeccaoConfigId())
                .orElseGet(() -> {
                    FichaProspeccao novo = FichaProspeccao.builder()
                            .ficha(ficha)
                            .dadoProspeccaoConfig(dadoConfig)
                            .quantidade(0)
                            .build();
                    return fichaProspeccaoRepository.save(novo);
                });

        fichaProspeccao.setQuantidade(fichaProspeccao.getQuantidade() + request.quantidade());
        fichaProspeccaoRepository.save(fichaProspeccao);

        log.info("Prospecção concedida: fichaId={}, dadoId={}, quantidade={}",
                fichaId, request.dadoProspeccaoConfigId(), request.quantidade());

        return fichaResumoService.getResumo(fichaId);
    }

    /**
     * Lista todos os usos de prospecção de uma ficha.
     *
     * <p>Mestre vê todos os usos; Jogador vê apenas os usos da própria ficha.</p>
     */
    public List<ProspeccaoUsoResponse> listarUsos(Long fichaId) {
        Ficha ficha = buscarFicha(fichaId);
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre && !usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para ver os usos desta ficha.");
        }

        return prospeccaoUsoRepository.findByFichaProspeccaoFichaId(fichaId)
                .stream()
                .map(uso -> toResponse(uso, ficha))
                .toList();
    }

    /**
     * Lista todos os usos PENDENTES de prospecção de um jogo.
     * Apenas o Mestre pode acessar.
     */
    public List<ProspeccaoUsoResponse> listarPendentesJogo(Long jogoId) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode listar usos pendentes do jogo.");
        }

        return prospeccaoUsoRepository
                .findByFichaProspeccaoFichaJogoIdAndStatus(jogoId, ProspeccaoUsoStatus.PENDENTE)
                .stream()
                .map(uso -> toResponse(uso, uso.getFichaProspeccao().getFicha()))
                .toList();
    }

    private ProspeccaoUsoResponse toResponse(ProspeccaoUso uso, Ficha ficha) {
        FichaProspeccao fp = uso.getFichaProspeccao();
        DadoProspeccaoConfig dado = fp.getDadoProspeccaoConfig();
        return new ProspeccaoUsoResponse(
                uso.getId(),
                dado != null ? dado.getNome() : null,
                dado != null ? dado.getId() : null,
                ficha.getId(),
                ficha.getNome(),
                uso.getStatus(),
                uso.getCreatedAt()
        );
    }

    private Ficha buscarFicha(Long fichaId) {
        return fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));
    }

    private ProspeccaoUso buscarUso(Long usoId) {
        return prospeccaoUsoRepository.findById(usoId)
                .orElseThrow(() -> new ResourceNotFoundException("Uso de prospecção não encontrado: " + usoId));
    }

    private void verificarUsoPertencenteAFicha(ProspeccaoUso uso, Long fichaId) {
        Long fichaDoUso = uso.getFichaProspeccao().getFicha().getId();
        if (!fichaDoUso.equals(fichaId)) {
            throw new ResourceNotFoundException("Uso " + uso.getId() + " não pertence à ficha " + fichaId);
        }
    }

    private void verificarAcessoMestre(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                ficha.getJogo().getId(), usuarioAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode executar esta operação de prospecção.");
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
