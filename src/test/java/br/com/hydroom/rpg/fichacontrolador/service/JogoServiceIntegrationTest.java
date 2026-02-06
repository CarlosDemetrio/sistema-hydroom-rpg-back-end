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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("JogoService - Testes de Integracao")
class JogoServiceIntegrationTest {

    @Autowired
    private JogoService jogoService;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        mestre = usuarioRepository.save(Usuario.builder()
            .nome("Mestre Teste")
            .email("mestre@test.com")
            .provider("google")
            .providerId("google-mestre")
            .build());

        jogador = usuarioRepository.save(Usuario.builder()
            .nome("Jogador Teste")
            .email("jogador@test.com")
            .provider("google")
            .providerId("google-jogador")
            .build());

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Integracao")
            .descricao("Descricao teste")
            .dataInicio(LocalDate.now())
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .build());

        setAuth(mestre);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar apenas jogos ativos")
    void listarJogosDoUsuario() {
        // Arrange
        Jogo jogoInativo = Jogo.builder().nome("Jogo Inativo").build();
        jogoInativo = jogoRepository.save(jogoInativo);
        jogoInativo.delete();
        jogoRepository.save(jogoInativo);

        // Act
        List<Jogo> result = jogoService.listarJogosDoUsuario();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Deve buscar jogo quando participa")
    void buscarJogoComParticipacao() {
        // Arrange
        setAuth(jogador);

        // Act
        Jogo result = jogoService.buscarJogo(jogo.getId());

        // Assert
        assertThat(result.getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Deve negar acesso quando nao participa")
    void buscarJogoSemParticipacao() {
        // Arrange
        Usuario outro = usuarioRepository.save(Usuario.builder()
            .nome("Outro")
            .email("outro@test.com")
            .provider("google")
            .providerId("google-outro")
            .build());
        setAuth(outro);

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.buscarJogo(jogo.getId()));
    }

    @Test
    @DisplayName("Deve criar jogo e registrar mestre")
    void criarJogo() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Nova Campanha")
            .descricao("Nova descricao")
            .dataInicio(LocalDate.now())
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNome()).isEqualTo("Nova Campanha");
        assertThat(result.isActive()).isTrue();
        assertThat(jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(
            mestre.getId(), result.getId(), RoleJogo.MESTRE)).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar jogo quando mestre")
    void atualizarJogo() {
        // Arrange
        var request = EditarJogoRequest.builder()
            .nome("Campanha Atualizada")
            .descricao("Descricao Atualizada")
            .imagemUrl("http://img")
            .dataInicio(LocalDate.now())
            .dataFim(LocalDate.now().plusDays(1))
            .build();

        // Act
        Jogo result = jogoService.atualizarJogo(jogo.getId(), request);

        // Assert
        assertThat(result.getNome()).isEqualTo("Campanha Atualizada");
        assertThat(result.getDescricao()).isEqualTo("Descricao Atualizada");
    }

    @Test
    @DisplayName("Deve negar atualizacao quando nao e mestre")
    void atualizarJogoSemPermissao() {
        // Arrange
        setAuth(jogador);
        var request = EditarJogoRequest.builder()
            .nome("Tentativa")
            .build();

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.atualizarJogo(jogo.getId(), request));
    }

    @Test
    @DisplayName("Deve marcar jogo como inativo quando mestre")
    void deletarJogo() {
        // Act
        jogoService.deletarJogo(jogo.getId());

        // Assert
        Jogo atualizado = jogoRepository.findById(jogo.getId()).orElseThrow();
        assertThat(atualizado.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Deve reativar jogo quando mestre")
    void ativarJogo() {
        // Arrange
        jogo.delete();
        jogoRepository.save(jogo);

        // Act
        Jogo result = jogoService.ativarJogo(jogo.getId());

        // Assert
        assertThat(result.isActive()).isTrue();
    }

    private void setAuth(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
