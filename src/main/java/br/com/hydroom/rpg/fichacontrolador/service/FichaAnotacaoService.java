package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAnotacao;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAnotacaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
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
 * Service para gerenciamento de anotações de fichas.
 *
 * <p>Regras de visibilidade:</p>
 * <ul>
 *   <li>MESTRE vê todas as anotações da ficha (tipo JOGADOR e MESTRE)</li>
 *   <li>JOGADOR vê apenas as próprias anotações (tipo JOGADOR) + anotações do Mestre com visivelParaJogador=true</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaAnotacaoService {

    private final FichaAnotacaoRepository fichaAnotacaoRepository;
    private final FichaRepository fichaRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Lista anotações de uma ficha respeitando o controle de acesso.
     *
     * <p>MESTRE: vê todas as anotações. JOGADOR: vê as próprias + as do Mestre visíveis.</p>
     *
     * @param fichaId ID da ficha
     * @return lista de anotações visíveis para o usuário atual
     */
    public List<FichaAnotacao> listar(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            // Mestre vê todas as anotações
            return fichaAnotacaoRepository.findByFichaIdOrderByCreatedAtDesc(fichaId);
        }

        // NPCs só são acessíveis pelo Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
        }

        // Jogador: verificar acesso à ficha
        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }

        // Jogador vê as próprias anotações (tipo JOGADOR do autor atual)
        List<FichaAnotacao> todasJogador = fichaAnotacaoRepository
                .findByFichaIdAndTipoAnotacaoOrderByCreatedAtDesc(fichaId, TipoAnotacao.JOGADOR);

        List<FichaAnotacao> proprias = todasJogador.stream()
                .filter(a -> a.getAutor() != null && usuarioAtual.getId().equals(a.getAutor().getId()))
                .toList();

        // Jogador também vê anotações do Mestre marcadas como visíveis
        List<FichaAnotacao> mestrevisiveis = fichaAnotacaoRepository
                .findByFichaIdAndVisivelParaJogadorTrueOrderByCreatedAtDesc(fichaId);

        return mergeOrdenado(proprias, mestrevisiveis);
    }

    /**
     * Cria uma nova anotação na ficha.
     *
     * <p>MESTRE pode criar qualquer tipo. JOGADOR só pode criar do tipo JOGADOR.</p>
     *
     * @param fichaId  ID da ficha
     * @param request  dados da anotação
     * @param autorId  ID do usuário autor
     * @return anotação criada
     */
    @Transactional
    public FichaAnotacao criar(Long fichaId, CriarAnotacaoRequest request, Long autorId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + autorId));

        Long jogoId = ficha.getJogo().getId();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, autor.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            // Jogador não pode criar anotações em fichas de NPC
            if (ficha.isNpc()) {
                throw new ForbiddenException("Acesso negado: Jogadores não podem criar anotações em fichas de NPC.");
            }
            // Jogador só pode criar anotação para suas próprias fichas
            if (!autor.getId().equals(ficha.getJogadorId())) {
                throw new ForbiddenException("Acesso negado: você só pode criar anotações nas suas próprias fichas.");
            }
            // Jogador só pode criar anotações do tipo JOGADOR
            if (TipoAnotacao.MESTRE.equals(request.tipoAnotacao())) {
                throw new ForbiddenException("Jogadores não podem criar anotações do tipo MESTRE.");
            }
        }

        boolean visivelParaJogador = Boolean.TRUE.equals(request.visivelParaJogador());

        FichaAnotacao anotacao = FichaAnotacao.builder()
                .ficha(ficha)
                .autor(autor)
                .titulo(request.titulo())
                .conteudo(request.conteudo())
                .tipoAnotacao(request.tipoAnotacao())
                .visivelParaJogador(visivelParaJogador)
                .build();

        anotacao = fichaAnotacaoRepository.save(anotacao);
        log.info("Anotação '{}' criada na ficha {} por autor {}", anotacao.getTitulo(), fichaId, autorId);
        return anotacao;
    }

    /**
     * Deleta uma anotação (soft delete).
     *
     * <p>MESTRE pode deletar qualquer anotação da ficha. JOGADOR só pode deletar as próprias.</p>
     *
     * @param fichaId      ID da ficha (para validação de pertencimento)
     * @param anotacaoId   ID da anotação a deletar
     */
    @Transactional
    public void deletar(Long fichaId, Long anotacaoId) {
        FichaAnotacao anotacao = fichaAnotacaoRepository.findById(anotacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação não encontrada: " + anotacaoId));

        if (!anotacao.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Esta anotação não pertence à ficha informada.");
        }

        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = anotacao.getFicha().getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            // Jogador não pode deletar anotações em fichas de NPC
            if (anotacao.getFicha().isNpc()) {
                throw new ForbiddenException("Acesso negado: Jogadores não podem deletar anotações em fichas de NPC.");
            }
            // Jogador só pode deletar suas próprias anotações
            if (anotacao.getAutor() == null || !usuarioAtual.getId().equals(anotacao.getAutor().getId())) {
                throw new ForbiddenException("Acesso negado: você só pode deletar suas próprias anotações.");
            }
        }

        anotacao.delete();
        fichaAnotacaoRepository.save(anotacao);
        log.info("Anotação {} deletada da ficha {} por {}", anotacaoId, fichaId, usuarioAtual.getEmail());
    }

    // ==================== PRIVADOS ====================

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }

    /**
     * Combina e ordena duas listas de anotações por data de criação (mais recente primeiro).
     */
    private List<FichaAnotacao> mergeOrdenado(List<FichaAnotacao> lista1, List<FichaAnotacao> lista2) {
        return java.util.stream.Stream.concat(lista1.stream(), lista2.stream())
                .distinct()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
    }
}
