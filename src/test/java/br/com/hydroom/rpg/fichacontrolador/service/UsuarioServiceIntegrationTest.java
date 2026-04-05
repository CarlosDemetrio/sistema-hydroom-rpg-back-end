package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para UsuarioService.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>GET /api/v1/usuarios/me — retorna dados do usuário autenticado</li>
 *   <li>PUT /api/v1/usuarios/me — atualiza apenas o nome</li>
 *   <li>Usuário não autenticado recebe ForbiddenException</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("UsuarioService - Testes de Integração")
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Usuarios " + n)
                .email("mestre-usuarios" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-usuarios-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Usuarios " + n)
                .email("jogador-usuarios" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-usuarios-" + n)
                .role("JOGADOR")
                .build());
    }

    // =========================================================
    // TESTES: BUSCAR USUÁRIO ATUAL
    // =========================================================

    @Test
    @DisplayName("Deve retornar dados do mestre autenticado")
    void deveRetornarDadosDoMestreAutenticado() {
        // Arrange
        autenticarComo(mestre);

        // Act
        Usuario resultado = usuarioService.buscarAtual();

        // Assert
        assertThat(resultado.getId()).isEqualTo(mestre.getId());
        assertThat(resultado.getEmail()).isEqualTo(mestre.getEmail());
        assertThat(resultado.getNome()).isEqualTo(mestre.getNome());
        assertThat(resultado.getRole()).isEqualTo("MESTRE");
    }

    @Test
    @DisplayName("Deve retornar dados do jogador autenticado")
    void deveRetornarDadosDoJogadorAutenticado() {
        // Arrange
        autenticarComo(jogador);

        // Act
        Usuario resultado = usuarioService.buscarAtual();

        // Assert
        assertThat(resultado.getId()).isEqualTo(jogador.getId());
        assertThat(resultado.getEmail()).isEqualTo(jogador.getEmail());
        assertThat(resultado.getRole()).isEqualTo("JOGADOR");
    }

    @Test
    @DisplayName("Usuário não autenticado deve receber ForbiddenException ao buscar perfil")
    void usuarioNaoAutenticadoDeveReceberForbidden() {
        // Arrange — sem autenticação
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> usuarioService.buscarAtual());
    }

    // =========================================================
    // TESTES: ATUALIZAR NOME
    // =========================================================

    @Test
    @DisplayName("Deve atualizar o nome do usuário autenticado com sucesso")
    void deveAtualizarNomeComSucesso() {
        // Arrange
        autenticarComo(jogador);
        String novoNome = "Novo Nome Do Jogador";

        // Act
        Usuario resultado = usuarioService.atualizarNome(novoNome);

        // Assert
        assertThat(resultado.getNome()).isEqualTo(novoNome);

        // Verificar que persistiu no banco
        Usuario doDb = usuarioRepository.findByEmail(jogador.getEmail()).orElseThrow();
        assertThat(doDb.getNome()).isEqualTo(novoNome);
    }

    @Test
    @DisplayName("Atualizar nome não deve alterar email nem provider")
    void atualizarNomeNaoDeveAlterarEmailOuProvider() {
        // Arrange
        autenticarComo(mestre);
        String emailOriginal = mestre.getEmail();
        String providerOriginal = mestre.getProvider();

        // Act
        usuarioService.atualizarNome("Novo Nome Mestre");

        // Assert — email e provider permanecem inalterados
        Usuario doDb = usuarioRepository.findByEmail(emailOriginal).orElseThrow();
        assertThat(doDb.getEmail()).isEqualTo(emailOriginal);
        assertThat(doDb.getProvider()).isEqualTo(providerOriginal);
        assertThat(doDb.getNome()).isEqualTo("Novo Nome Mestre");
    }

    @Test
    @DisplayName("Usuário não autenticado deve receber ForbiddenException ao atualizar nome")
    void usuarioNaoAutenticadoDeveReceberForbiddenAoAtualizar() {
        // Arrange — sem autenticação
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> usuarioService.atualizarNome("Qualquer Nome"));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
