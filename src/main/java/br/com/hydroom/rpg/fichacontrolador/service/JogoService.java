package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.EditarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.MeuJogoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciar Jogos/Campanhas.
 * Apenas o Mestre do jogo pode criar/editar/deletar.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final GameConfigInitializerService configInitializerService;
    private final FichaRepository fichaRepository;

    public List<Jogo> listarJogosDoUsuario() {
        Usuario usuarioAtual = getUsuarioAtual();
        return jogoRepository.findByParticipantesUsuarioId(usuarioAtual.getId());
    }

    /**
     * Lista os jogos do usuário atual com informações de role e quantidade de personagens.
     */
    public List<MeuJogoResponse> listarMeus() {
        Usuario usuarioAtual = getUsuarioAtual();
        List<JogoParticipante> participacoes = jogoParticipanteRepository.findByUsuarioIdNotDeleted(usuarioAtual.getId());

        return participacoes.stream()
                .map(p -> {
                    Jogo jogo = p.getJogo();
                    boolean isMestre = RoleJogo.MESTRE.equals(p.getRole());
                    int meusPersonagens = (int) fichaRepository.countByJogoIdAndJogadorIdAndIsNpcFalse(
                            jogo.getId(), usuarioAtual.getId());
                    return new MeuJogoResponse(jogo.getId(), jogo.getNome(), isMestre, meusPersonagens);
                })
                .toList();
    }

    public Jogo buscarJogo(Long id) {
        return buscarJogoEntity(id);
    }

    private Jogo buscarJogoEntity(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioPertenceAoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Você não tem acesso a este jogo");
        }

        return jogo;
    }

    @Transactional
    public Jogo criarJogo(CriarJogoRequest request) {
        Usuario usuarioAtual = getUsuarioAtual();

        // Verificar se já existe algum jogo ativo para este mestre
        boolean temJogoAtivo = jogoRepository.findByMestreIdAndJogoAtivoTrue(usuarioAtual.getId()).isPresent();

        Jogo novoJogo = new Jogo();
        novoJogo.setNome(request.getNome());
        novoJogo.setDescricao(request.getDescricao());
        novoJogo.setDataInicio(request.getDataInicio());

        // Se não houver nenhum jogo ativo, este será o jogo ativo automaticamente
        if (!temJogoAtivo) {
            novoJogo.setJogoAtivo(true);
        }

        Jogo jogoSalvo = jogoRepository.save(novoJogo);

        // Adicionar mestre como participante
        JogoParticipante participacao = JogoParticipante.builder()
                .jogo(jogoSalvo)
                .usuario(usuarioAtual)
                .role(RoleJogo.MESTRE)
                .build();

        jogoParticipanteRepository.save(participacao);

        // Inicializar configurações padrão do jogo
        configInitializerService.initializeGameConfigs(jogoSalvo.getId());

        return jogoSalvo;
    }

    @Transactional
    public Jogo atualizarJogo(Long id, EditarJogoRequest request) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode editá-lo");
        }

        jogo.setNome(request.getNome());
        jogo.setDescricao(request.getDescricao());
        jogo.setImagemUrl(request.getImagemUrl());
        jogo.setDataInicio(request.getDataInicio());
        jogo.setDataFim(request.getDataFim());

        return jogoRepository.save(jogo);
    }

    @Transactional
    public void deletarJogo(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode deletá-lo");
        }

        // Soft delete (seta deleted_at)
        jogo.delete();
        jogoRepository.save(jogo);
    }

    @Transactional
    public Jogo ativarJogo(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode ativá-lo");
        }

        // REGRA: Apenas 1 jogo pode estar ativo por mestre
        // Desativa todos os outros jogos do mestre
        List<Jogo> jogosDoMestre = jogoRepository.findByMestreId(usuarioAtual.getId());
        for (Jogo j : jogosDoMestre) {
            if (j.getJogoAtivo() && !j.getId().equals(id)) {
                j.setJogoAtivo(false);
                jogoRepository.save(j);
            }
        }

        // Ativa o jogo selecionado
        jogo.restore(); // Remove soft delete se houver
        jogo.setJogoAtivo(true);
        return jogoRepository.save(jogo);
    }

    /**
     * Busca o jogo ativo do mestre logado.
     * Jogo ativo = jogo selecionado atualmente (jogoAtivo=true).
     *
     * @return Jogo ativo
     * @throws IllegalStateException se não houver jogo ativo
     */
    public Jogo buscarJogoAtivo() {
        Usuario usuarioAtual = getUsuarioAtual();
        return jogoRepository.findByMestreIdAndJogoAtivoTrue(usuarioAtual.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum jogo ativo encontrado"));
    }

    public boolean usuarioEhMestreDoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioId, RoleJogo.MESTRE);
    }

    public boolean usuarioPertenceAoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByUsuarioIdAndJogoId(usuarioId, jogoId);
    }

    public Long getUsuarioAtualId() {
        return getUsuarioAtual().getId();
    }

    public RoleJogo getMeuRole(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.findRoleByJogoIdAndUsuarioId(jogoId, usuarioId).orElse(null);
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
