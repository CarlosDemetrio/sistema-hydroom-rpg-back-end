package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaEssencia;
import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVidaMembro;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaEssenciaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaProspeccaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaMembroRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVisibilidadeRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsável por atualizar o estado de vida, essência e prospecção de uma ficha.
 *
 * <p>Estes endpoints gerenciam o estado de combate da ficha (dano recebido, essência gasta)
 * sem recalcular os valores derivados de atributos.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaVidaService {

    private final FichaRepository fichaRepository;
    private final FichaVidaRepository fichaVidaRepository;
    private final FichaVidaMembroRepository fichaVidaMembroRepository;
    private final FichaEssenciaRepository fichaEssenciaRepository;
    private final FichaProspeccaoRepository fichaProspeccaoRepository;
    private final FichaVisibilidadeRepository fichaVisibilidadeRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Atualiza o estado de vida atual, essência atual e danos nos membros da ficha.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Mestre pode atualizar qualquer ficha do seu jogo.</li>
     *   <li>Jogador só pode atualizar suas próprias fichas.</li>
     *   <li>Não recalcula atributos derivados.</li>
     *   <li>Membros informados são atualizados; membros não informados mantêm estado atual.</li>
     * </ul>
     */
    @Transactional
    public Ficha atualizarVida(Long fichaId, AtualizarVidaRequest request) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        // Atualizar FichaVida
        FichaVida fichaVida = fichaVidaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dados de vida não encontrados para ficha: " + fichaId));

        fichaVida.setVidaAtual(request.vidaAtual());
        fichaVidaRepository.save(fichaVida);

        // Atualizar FichaEssencia
        FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dados de essência não encontrados para ficha: " + fichaId));

        fichaEssencia.setEssenciaAtual(request.essenciaAtual());
        fichaEssenciaRepository.save(fichaEssencia);

        // Atualizar membros (apenas os informados)
        if (request.membros() != null && !request.membros().isEmpty()) {
            for (AtualizarVidaRequest.MembroVidaRequest membroRequest : request.membros()) {
                FichaVidaMembro membro = fichaVidaMembroRepository
                        .findByFichaIdAndMembroCorpoConfigId(fichaId, membroRequest.membroCorpoConfigId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Membro do corpo não encontrado para ficha " + fichaId
                                        + " e configuração " + membroRequest.membroCorpoConfigId()));

                membro.setDanoRecebido(membroRequest.danoRecebido());
                fichaVidaMembroRepository.save(membro);
            }
        }

        log.info("Vida da ficha {} atualizada: vidaAtual={}, essenciaAtual={}",
                fichaId, request.vidaAtual(), request.essenciaAtual());

        return ficha;
    }

    /**
     * Atualiza a quantidade de um dado de prospecção específico da ficha.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Mestre pode atualizar qualquer ficha do seu jogo.</li>
     *   <li>Jogador só pode atualizar suas próprias fichas.</li>
     * </ul>
     */
    @Transactional
    public Ficha atualizarProspeccao(Long fichaId, AtualizarProspeccaoRequest request) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        FichaProspeccao fichaProspeccao = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaId, request.dadoProspeccaoConfigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prospecção não encontrada para ficha " + fichaId
                                + " e dado de prospecção " + request.dadoProspeccaoConfigId()));

        fichaProspeccao.setQuantidade(request.quantidade());
        fichaProspeccaoRepository.save(fichaProspeccao);

        log.info("Prospecção da ficha {} atualizada: dadoId={}, quantidade={}",
                fichaId, request.dadoProspeccaoConfigId(), request.quantidade());

        return ficha;
    }

    /**
     * Lista todos os dados de prospecção de uma ficha.
     */
    public List<FichaProspeccao> listarProspeccoes(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        return fichaProspeccaoRepository.findByFichaId(fichaId);
    }

    /**
     * Reseta o estado de combate da ficha ao estado base em uma operação atômica.
     *
     * <p>Campos resetados:</p>
     * <ul>
     *   <li>FichaVida.vidaAtual → FichaVida.vidaTotal</li>
     *   <li>FichaEssencia.essenciaAtual → FichaEssencia.total</li>
     *   <li>FichaVidaMembro.danoRecebido → 0 (todos os membros)</li>
     * </ul>
     *
     * <p>Não resetado: FichaProspeccao.quantidade, atributos, XP, nível.</p>
     */
    @Transactional
    public Ficha resetarEstado(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoMestre(ficha);

        FichaVida fichaVida = fichaVidaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dados de vida não encontrados para ficha: " + fichaId));
        fichaVida.setVidaAtual(fichaVida.getVidaTotal());
        fichaVidaRepository.save(fichaVida);

        FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dados de essência não encontrados para ficha: " + fichaId));
        fichaEssencia.setEssenciaAtual(fichaEssencia.getTotal());
        fichaEssenciaRepository.save(fichaEssencia);

        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaId(fichaId);
        membros.forEach(m -> m.setDanoRecebido(0));
        fichaVidaMembroRepository.saveAll(membros);

        log.info("Estado resetado para ficha {}: vidaAtual={}, essenciaAtual={}",
                fichaId, fichaVida.getVidaTotal(), fichaEssencia.getTotal());

        return ficha;
    }

    private void verificarAcessoMestre(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                ficha.getJogo().getId(), usuarioAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode resetar o estado de uma ficha.");
        }
    }

    private void verificarAcessoEscrita(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        // NPCs só podem ser editados pelo Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só podem ser editados pelo Mestre.");
        }

        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você só pode editar suas próprias fichas.");
        }
    }

    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        // NPCs: apenas Jogadores com FichaVisibilidade ativa podem acessar stats
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
