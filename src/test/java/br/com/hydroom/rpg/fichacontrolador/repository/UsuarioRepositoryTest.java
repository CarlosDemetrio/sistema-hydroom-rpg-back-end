package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integração para UsuarioRepository.
 * Usa H2 em memória (application-test.properties).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UsuarioRepository - Testes de Integração")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        usuarioBase = Usuario.builder()
                .email("teste@example.com")
                .nome("Usuário Teste")
                .imagemUrl("https://example.com/foto.jpg")
                .provider("GOOGLE")
                .providerId("google-123456")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void testSalvarUsuario() {
        // Arrange
        // usuarioBase já configurado no setUp()

        // Act
        Usuario usuarioSalvo = usuarioRepository.save(usuarioBase);

        // Assert
        assertThat(usuarioSalvo).isNotNull();
        assertThat(usuarioSalvo.getId()).isNotNull();
        assertThat(usuarioSalvo.getEmail()).isEqualTo("teste@example.com");
        assertThat(usuarioSalvo.getNome()).isEqualTo("Usuário Teste");
        assertThat(usuarioSalvo.getProvider()).isEqualTo("GOOGLE");
        assertThat(usuarioSalvo.getProviderId()).isEqualTo("google-123456");
        assertThat(usuarioSalvo.getAtivo()).isTrue();
        assertThat(usuarioSalvo.getCriadoEm()).isNotNull();
        assertThat(usuarioSalvo.getAtualizadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void testBuscarPorEmail() {
        // Arrange
        usuarioRepository.save(usuarioBase);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("teste@example.com");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("teste@example.com");
        assertThat(resultado.get().getNome()).isEqualTo("Usuário Teste");
    }

    @Test
    @DisplayName("Deve retornar vazio quando email não existe")
    void testBuscarPorEmailNaoExistente() {
        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("naoexiste@example.com");

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar usuário por providerId")
    void testBuscarPorProviderId() {
        // Arrange
        usuarioRepository.save(usuarioBase);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByProviderId("google-123456");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProviderId()).isEqualTo("google-123456");
        assertThat(resultado.get().getEmail()).isEqualTo("teste@example.com");
    }

    @Test
    @DisplayName("Deve retornar vazio quando providerId não existe")
    void testBuscarPorProviderIdNaoExistente() {
        // Act
        Optional<Usuario> resultado = usuarioRepository.findByProviderId("nao-existe");

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void testExistsByEmail() {
        // Arrange
        usuarioRepository.save(usuarioBase);

        // Act
        boolean existe = usuarioRepository.existsByEmail("teste@example.com");
        boolean naoExiste = usuarioRepository.existsByEmail("outro@example.com");

        // Assert
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar email duplicado")
    void testEmailUnicoViolacao() {
        // Arrange
        usuarioRepository.save(usuarioBase);

        Usuario usuarioDuplicado = Usuario.builder()
                .email("teste@example.com") // Email duplicado
                .nome("Outro Usuário")
                .provider("GOOGLE")
                .providerId("google-789012") // ProviderId diferente
                .ativo(true)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioDuplicado);
            usuarioRepository.flush(); // Força execução imediata
        })
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar providerId duplicado")
    void testProviderIdUnicoViolacao() {
        // Arrange
        usuarioRepository.save(usuarioBase);

        Usuario usuarioDuplicado = Usuario.builder()
                .email("outro@example.com") // Email diferente
                .nome("Outro Usuário")
                .provider("GOOGLE")
                .providerId("google-123456") // ProviderId duplicado
                .ativo(true)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioDuplicado);
            usuarioRepository.flush(); // Força execução imediata
        })
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void testAtualizarUsuario() {
        // Arrange
        Usuario usuarioSalvo = usuarioRepository.save(usuarioBase);
        Long id = usuarioSalvo.getId();

        // Act
        usuarioSalvo.setNome("Nome Atualizado");
        usuarioSalvo.setImagemUrl("https://example.com/nova-foto.jpg");
        Usuario usuarioAtualizado = usuarioRepository.save(usuarioSalvo);

        // Assert
        assertThat(usuarioAtualizado.getId()).isEqualTo(id);
        assertThat(usuarioAtualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(usuarioAtualizado.getImagemUrl()).isEqualTo("https://example.com/nova-foto.jpg");
        assertThat(usuarioAtualizado.getEmail()).isEqualTo("teste@example.com"); // Não mudou
        assertThat(usuarioAtualizado.getAtualizadoEm()).isAfter(usuarioAtualizado.getCriadoEm());
    }

    @Test
    @DisplayName("Deve salvar múltiplos usuários")
    void testSalvarMultiplosUsuarios() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
                .email("usuario1@example.com")
                .nome("Usuário 1")
                .provider("GOOGLE")
                .providerId("google-111")
                .ativo(true)
                .build();

        Usuario usuario2 = Usuario.builder()
                .email("usuario2@example.com")
                .nome("Usuário 2")
                .provider("GOOGLE")
                .providerId("google-222")
                .ativo(true)
                .build();

        // Act
        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
        long count = usuarioRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void testDeletarUsuario() {
        // Arrange
        Usuario usuarioSalvo = usuarioRepository.save(usuarioBase);
        Long id = usuarioSalvo.getId();

        // Act
        usuarioRepository.delete(usuarioSalvo);
        Optional<Usuario> resultado = usuarioRepository.findById(id);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve desativar usuário ao invés de deletar")
    void testDesativarUsuario() {
        // Arrange
        Usuario usuarioSalvo = usuarioRepository.save(usuarioBase);

        // Act
        usuarioSalvo.setAtivo(false);
        Usuario usuarioDesativado = usuarioRepository.save(usuarioSalvo);

        // Assert
        assertThat(usuarioDesativado.getAtivo()).isFalse();
        assertThat(usuarioRepository.findById(usuarioDesativado.getId())).isPresent();
    }
}
