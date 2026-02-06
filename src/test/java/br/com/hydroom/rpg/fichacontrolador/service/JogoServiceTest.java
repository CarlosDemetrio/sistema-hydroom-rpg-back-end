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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JogoService - Testes Unitarios")
class JogoServiceTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private JogoService jogoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
            .id(1L)
            .nome("Usuario Teste")
            .email("user@test.com")
            .provider("google")
            .providerId("google123")
            .build();

        var authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void stubUsuarioAtual() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar jogos ativos")
    void listarJogosDoUsuario() {
        // Arrange
        stubUsuarioAtual();
        var jogos = List.of(Jogo.builder().id(1L).build(), Jogo.builder().id(2L).build());
        when(jogoRepository.findByParticipantesUsuarioId(usuario.getId())).thenReturn(jogos);

        // Act
        var result = jogoService.listarJogosDoUsuario();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(jogos);
        verify(jogoRepository).findByParticipantesUsuarioId(usuario.getId());
    }

    @Test
    @DisplayName("Deve negar acesso quando usuario nao participa do jogo")
    void buscarJogoSemParticipacao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).build();
        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByUsuarioIdAndJogoId(usuario.getId(), 10L))
            .thenReturn(false);

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.buscarJogo(10L));
    }

    @Test
    @DisplayName("Deve buscar jogo quando usuario participa")
    void buscarJogoComParticipacao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).build();
        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByUsuarioIdAndJogoId(usuario.getId(), 10L))
            .thenReturn(true);

        // Act
        var result = jogoService.buscarJogo(10L);

        // Assert
        assertThat(result).isEqualTo(jogo);
    }

    @Test
    @DisplayName("Deve criar jogo e registrar mestre")
    void criarJogo() {
        // Arrange
        stubUsuarioAtual();
        var request = CriarJogoRequest.builder()
            .nome("Campanha Teste")
            .descricao("Descricao")
            .dataInicio(LocalDate.now())
            .build();

        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> {
            Jogo saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        // Act
        var result = jogoService.criarJogo(request);

        // Assert
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getNome()).isEqualTo("Campanha Teste");
        assertThat(result.isActive()).isTrue();

        ArgumentCaptor<JogoParticipante> captor = ArgumentCaptor.forClass(JogoParticipante.class);
        verify(jogoParticipanteRepository).save(captor.capture());
        var participacao = captor.getValue();
        assertThat(participacao.getUsuario()).isEqualTo(usuario);
        assertThat(participacao.getRole()).isEqualTo(RoleJogo.MESTRE);
        assertThat(participacao.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve negar edicao quando usuario nao e mestre")
    void atualizarJogoSemPermissao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).build();
        var request = EditarJogoRequest.builder().nome("Novo Nome").build();

        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(10L, usuario.getId(), RoleJogo.MESTRE))
            .thenReturn(false);

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.atualizarJogo(10L, request));
    }

    @Test
    @DisplayName("Deve atualizar jogo quando usuario e mestre")
    void atualizarJogoComPermissao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).nome("Antigo").build();
        var request = EditarJogoRequest.builder()
            .nome("Novo Nome")
            .descricao("Nova descricao")
            .imagemUrl("http://img")
            .dataInicio(LocalDate.now())
            .dataFim(LocalDate.now().plusDays(1))
            .build();

        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(10L, usuario.getId(), RoleJogo.MESTRE))
            .thenReturn(true);
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = jogoService.atualizarJogo(10L, request);

        // Assert
        assertThat(result.getNome()).isEqualTo("Novo Nome");
        assertThat(result.getDescricao()).isEqualTo("Nova descricao");
        assertThat(result.getImagemUrl()).isEqualTo("http://img");
        assertThat(result.getDataFim()).isEqualTo(request.getDataFim());
    }

    @Test
    @DisplayName("Deve marcar jogo como inativo quando mestre")
    void deletarJogoComPermissao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).build();

        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(10L, usuario.getId(), RoleJogo.MESTRE))
            .thenReturn(true);

        // Act
        jogoService.deletarJogo(10L);

        // Assert
        ArgumentCaptor<Jogo> captor = ArgumentCaptor.forClass(Jogo.class);
        verify(jogoRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("Deve reativar jogo quando mestre")
    void ativarJogoComPermissao() {
        // Arrange
        stubUsuarioAtual();
        var jogo = Jogo.builder().id(10L).build();

        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(10L, usuario.getId(), RoleJogo.MESTRE))
            .thenReturn(true);
        when(jogoRepository.findByMestreId(usuario.getId())).thenReturn(List.of());
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = jogoService.ativarJogo(10L);

        // Assert
        assertThat(result.getJogoAtivo()).isTrue();
    }
}
