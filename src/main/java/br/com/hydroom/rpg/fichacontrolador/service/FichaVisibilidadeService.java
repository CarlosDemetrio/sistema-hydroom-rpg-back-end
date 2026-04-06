package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVisibilidadeRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVisibilidadeResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVisibilidade;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVisibilidadeRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service para gerenciar a visibilidade granular de NPCs para Jogadores.
 *
 * <p>O Mestre pode revelar os stats completos de um NPC para jogadores específicos.
 * Isso é independente de visivelGlobalmente — um NPC pode aparecer na listagem (global)
 * mas ter seus stats revelados apenas para um subconjunto de jogadores.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaVisibilidadeService {

    private final FichaRepository fichaRepository;
    private final FichaVisibilidadeRepository fichaVisibilidadeRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Lista jogadores com acesso aos stats do NPC.
     */
    public FichaVisibilidadeResponse listar(Long fichaId) {
        Ficha ficha = buscarNpcOuLancar(fichaId);
        verificarAcessoMestre(ficha);

        List<FichaVisibilidade> visibilidades = fichaVisibilidadeRepository.findByFichaId(fichaId);
        List<FichaVisibilidadeResponse.JogadorAcessoResponse> acessos = construirAcessos(ficha, visibilidades);

        return new FichaVisibilidadeResponse(fichaId, ficha.isVisivelGlobalmente(), acessos);
    }

    /**
     * Atualiza a lista de jogadores com acesso aos stats de um NPC.
     *
     * <p>Se substituir=true, soft-deleta os registros existentes e recria.
     * Se substituir=false (padrão), apenas adiciona os novos — idempotente por par (fichaId, jogadorId).</p>
     */
    @Transactional
    public FichaVisibilidadeResponse atualizar(Long fichaId, AtualizarVisibilidadeRequest request) {
        Ficha ficha = buscarNpcOuLancar(fichaId);
        verificarAcessoMestre(ficha);

        Long jogoId = ficha.getJogo().getId();

        if (request.substituir()) {
            // Soft-delete todos os registros existentes
            List<FichaVisibilidade> existentes = fichaVisibilidadeRepository.findByFichaId(fichaId);
            existentes.forEach(fv -> {
                fv.delete();
                fichaVisibilidadeRepository.save(fv);
            });
        }

        // Adicionar novos jogadores (idempotente: não duplica se já existir)
        for (Long jogadorId : request.jogadoresIds()) {
            // Validar que jogador é participante aprovado do jogo
            boolean isParticipanteAprovado = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndStatus(
                    jogoId, jogadorId, StatusParticipante.APROVADO);
            if (!isParticipanteAprovado) {
                throw new ValidationException(
                        "Jogador " + jogadorId + " não é participante aprovado do jogo.");
            }

            // Idempotência: verificar se já existe (ativo)
            if (!fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(fichaId, jogadorId)) {
                FichaVisibilidade visibilidade = FichaVisibilidade.builder()
                        .ficha(ficha)
                        .jogadorId(jogadorId)
                        .build();
                fichaVisibilidadeRepository.save(visibilidade);
                log.info("Visibilidade concedida: fichaId={}, jogadorId={}", fichaId, jogadorId);
            }
        }

        // Retornar estado atualizado
        List<FichaVisibilidade> visibilidades = fichaVisibilidadeRepository.findByFichaId(fichaId);
        List<FichaVisibilidadeResponse.JogadorAcessoResponse> acessos = construirAcessos(ficha, visibilidades);
        return new FichaVisibilidadeResponse(fichaId, ficha.isVisivelGlobalmente(), acessos);
    }

    /**
     * Revoga o acesso de um jogador específico aos stats de um NPC.
     */
    @Transactional
    public void revogar(Long fichaId, Long jogadorId) {
        Ficha ficha = buscarNpcOuLancar(fichaId);
        verificarAcessoMestre(ficha);

        FichaVisibilidade visibilidade = fichaVisibilidadeRepository
                .findByFichaIdAndJogadorId(fichaId, jogadorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Visibilidade não encontrada para fichaId=" + fichaId + " e jogadorId=" + jogadorId));

        visibilidade.delete();
        fichaVisibilidadeRepository.save(visibilidade);
        log.info("Visibilidade revogada: fichaId={}, jogadorId={}", fichaId, jogadorId);
    }

    /**
     * Verifica se um jogador tem acesso aos stats de um NPC.
     */
    public boolean temAcesso(Long fichaId, Long jogadorId) {
        return fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(fichaId, jogadorId);
    }

    /**
     * Verifica se o usuário atual tem acesso aos stats de um NPC.
     * Retorna false se não houver autenticação (contexto de sistema).
     */
    public boolean temAcessoUsuarioAtual(Long fichaId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            String email = authentication.getName();
            return usuarioRepository.findByEmail(email)
                    .map(u -> fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(fichaId, u.getId()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private Ficha buscarNpcOuLancar(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));
        if (!ficha.isNpc()) {
            throw new ValidationException("Visibilidade granular é válida apenas para NPCs.");
        }
        return ficha;
    }

    private void verificarAcessoMestre(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                ficha.getJogo().getId(), usuarioAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode gerenciar visibilidade de NPCs.");
        }
    }

    private List<FichaVisibilidadeResponse.JogadorAcessoResponse> construirAcessos(
            Ficha ficha, List<FichaVisibilidade> visibilidades) {

        List<FichaVisibilidadeResponse.JogadorAcessoResponse> acessos = new ArrayList<>();
        for (FichaVisibilidade fv : visibilidades) {
            Long jogadorId = fv.getJogadorId();
            String jogadorNome = usuarioRepository.findById(jogadorId)
                    .map(Usuario::getNome)
                    .orElse("Desconhecido");

            // nomePersonagem: ficha do jogador neste jogo (primeira encontrada)
            String nomePersonagem = fichaRepository
                    .findByJogoIdAndJogadorIdAndIsNpcFalse(ficha.getJogo().getId(), jogadorId)
                    .stream()
                    .findFirst()
                    .map(Ficha::getNome)
                    .orElse(null);

            acessos.add(new FichaVisibilidadeResponse.JogadorAcessoResponse(jogadorId, jogadorNome, nomePersonagem));
        }
        return acessos;
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
