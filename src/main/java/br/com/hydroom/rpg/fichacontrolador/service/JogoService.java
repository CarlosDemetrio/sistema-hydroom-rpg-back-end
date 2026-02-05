package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.EditarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
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

    public List<Jogo> listarJogosDoUsuario() {
        return jogoRepository.findByAtivoTrue();
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

        Jogo novoJogo = new Jogo();
        novoJogo.setNome(request.getNome());
        novoJogo.setDescricao(request.getDescricao());
        novoJogo.setDataInicio(request.getDataInicio());
        novoJogo.setAtivo(true);

        Jogo jogoSalvo = jogoRepository.save(novoJogo);

        // Adicionar mestre como participante
        JogoParticipante participacao = JogoParticipante.builder()
                .jogo(jogoSalvo)
                .usuario(usuarioAtual)
                .role(RoleJogo.MESTRE)
                .ativo(true)
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

        jogo.setAtivo(false);
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

        // Desativar todos os outros jogos do mestre
        jogoRepository.desativarTodosDoMestre(usuarioAtual.getId());

        // Ativar o jogo selecionado
        jogo.setAtivo(true);
        return jogoRepository.save(jogo);
    }

    /**
     * Busca o jogo ativo do mestre logado.
     *
     * @return Jogo ativo
     * @throws IllegalStateException se não houver jogo ativo
     */
    public Jogo buscarJogoAtivo() {
        Usuario usuarioAtual = getUsuarioAtual();
        return jogoRepository.findByMestreIdAndAtivoTrue(usuarioAtual.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum jogo ativo encontrado"));
    }

    public boolean usuarioEhMestreDoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(
                usuarioId, jogoId, RoleJogo.MESTRE);
    }

    public boolean usuarioPertenceAoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndAtivoTrue(usuarioId, jogoId);
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
