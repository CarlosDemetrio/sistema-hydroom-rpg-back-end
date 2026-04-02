package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para FichaVantagemService.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Compra de vantagem sem pré-requisitos</li>
 *   <li>Compra com pré-requisitos atendidos</li>
 *   <li>Rejeição por pré-requisitos não atendidos</li>
 *   <li>Rejeição por vantagem já comprada (conflito)</li>
 *   <li>Aumento de nível</li>
 *   <li>Rejeição por exceder nível máximo</li>
 *   <li>Verificação de inexistência de endpoint de remoção</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaVantagemService - Testes de Integração")
class FichaVantagemServiceIntegrationTest {

    @Autowired
    private FichaVantagemService fichaVantagemService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaVantagemRepository fichaVantagemRepository;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private VantagemPreRequisitoRepository vantagemPreRequisitoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private CategoriaVantagemRepository categoriaVantagemRepository;

    @Autowired
    private FichaVantagemMapper fichaVantagemMapper;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;
    private Ficha ficha;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
        vantagemPreRequisitoRepository.deleteAll();
        fichaVantagemRepository.deleteAll();
        fichaDescricaoFisicaRepository.deleteAll();
        fichaProspeccaoRepository.deleteAll();
        fichaAmeacaRepository.deleteAll();
        fichaEssenciaRepository.deleteAll();
        fichaVidaMembroRepository.deleteAll();
        fichaVidaRepository.deleteAll();
        fichaBonusRepository.deleteAll();
        fichaAptidaoRepository.deleteAll();
        fichaAtributoRepository.deleteAll();
        fichaRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
        categoriaVantagemRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Teste")
                .email("mestre.vant" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-vant-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Teste")
                .email("jogador.vant" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-vant-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Vantagem Teste " + n)
                .build());

        // Adicionar jogador como participante aprovado
        JogoParticipante participante = JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build();
        jogoParticipanteRepository.save(participante);

        // Criar ficha para o jogador
        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Teste", jogador.getId(),
                null, null, null, null, null, false));
    }

    // =========================================================
    // TESTES DE COMPRA
    // =========================================================

    @Test
    @DisplayName("Deve comprar vantagem sem pré-requisitos")
    void deveComprarVantagemSemPreRequisitos() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Vantagem Simples", "1", 5);

        // Act
        autenticarComo(mestre);
        FichaVantagem resultado = fichaVantagemService.comprar(ficha.getId(), vantagem.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNivelAtual()).isEqualTo(1);
        assertThat(resultado.getCustoPago()).isEqualTo(1);
        assertThat(resultado.getVantagemConfig().getId()).isEqualTo(vantagem.getId());
        assertThat(resultado.getFicha().getId()).isEqualTo(ficha.getId());
    }

    @Test
    @DisplayName("Deve comprar vantagem com pré-requisitos atendidos")
    void deveComprarVantagemComPreRequisitosAtendidos() {
        // Arrange - criar vantagem pré-requisito e a vantagem principal
        VantagemConfig prereqVantagem = criarVantagem("Pré-Requisito", "2", 5);
        VantagemConfig vantagemPrincipal = criarVantagem("Vantagem Principal", "3", 5);

        // Criar a relação de pré-requisito
        VantagemPreRequisito preRequisito = VantagemPreRequisito.builder()
                .vantagem(vantagemPrincipal)
                .requisito(prereqVantagem)
                .nivelMinimo(1)
                .build();
        vantagemPreRequisitoRepository.save(preRequisito);

        // Comprar o pré-requisito primeiro
        autenticarComo(mestre);
        fichaVantagemService.comprar(ficha.getId(), prereqVantagem.getId());

        // Act - comprar a vantagem principal
        FichaVantagem resultado = fichaVantagemService.comprar(ficha.getId(), vantagemPrincipal.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNivelAtual()).isEqualTo(1);
        assertThat(resultado.getVantagemConfig().getId()).isEqualTo(vantagemPrincipal.getId());

        // Verificar que ambas as vantagens foram compradas
        List<FichaVantagem> vantagens = fichaVantagemService.listar(ficha.getId());
        assertThat(vantagens).hasSize(2);
    }

    @Test
    @DisplayName("Deve rejeitar compra com pré-requisitos não atendidos")
    void deveRejeitarCompraComPreRequisitosNaoAtendidos() {
        // Arrange
        VantagemConfig prereqVantagem = criarVantagem("Req Não Atendido", "2", 5);
        VantagemConfig vantagemPrincipal = criarVantagem("Vantagem Com Req", "3", 5);

        VantagemPreRequisito preRequisito = VantagemPreRequisito.builder()
                .vantagem(vantagemPrincipal)
                .requisito(prereqVantagem)
                .nivelMinimo(1)
                .build();
        vantagemPreRequisitoRepository.save(preRequisito);

        // Act & Assert - tentar comprar sem ter o pré-requisito
        autenticarComo(mestre);
        assertThrows(ValidationException.class,
                () -> fichaVantagemService.comprar(ficha.getId(), vantagemPrincipal.getId()));
    }

    @Test
    @DisplayName("Deve rejeitar compra de vantagem já comprada (ConflictException)")
    void deveRejeitarCompraDeVantagemJaComprada() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Vantagem Única", "1", 5);

        // Comprar a vantagem uma vez
        autenticarComo(mestre);
        fichaVantagemService.comprar(ficha.getId(), vantagem.getId());

        // Act & Assert - tentar comprar novamente
        assertThrows(ConflictException.class,
                () -> fichaVantagemService.comprar(ficha.getId(), vantagem.getId()));
    }

    // =========================================================
    // TESTES DE AUMENTO DE NÍVEL
    // =========================================================

    @Test
    @DisplayName("Deve aumentar nível de vantagem")
    void deveAumentarNivelDeVantagem() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Vantagem Nivelável", "nivel_vantagem", 5);

        autenticarComo(mestre);
        FichaVantagem comprada = fichaVantagemService.comprar(ficha.getId(), vantagem.getId());
        assertThat(comprada.getNivelAtual()).isEqualTo(1);

        // Act
        FichaVantagem atualizada = fichaVantagemService.aumentarNivel(ficha.getId(), comprada.getId());

        // Assert
        assertThat(atualizada.getNivelAtual()).isEqualTo(2);
        assertThat(atualizada.getCustoPago()).isEqualTo(2); // nivel_vantagem = 2
    }

    @Test
    @DisplayName("Deve rejeitar aumento de nível além do nível máximo")
    void deveRejeitarAumentoAlemDoNivelMaximo() {
        // Arrange - vantagem com nivelMaximo = 1
        VantagemConfig vantagem = criarVantagem("Vantagem Limite", "1", 1);

        autenticarComo(mestre);
        FichaVantagem comprada = fichaVantagemService.comprar(ficha.getId(), vantagem.getId());
        assertThat(comprada.getNivelAtual()).isEqualTo(1);
        assertThat(comprada.getVantagemConfig().getNivelMaximo()).isEqualTo(1);

        // Act & Assert - tentar subir nível além do máximo
        assertThrows(ValidationException.class,
                () -> fichaVantagemService.aumentarNivel(ficha.getId(), comprada.getId()));
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Deve listar vantagens da ficha")
    void deveListarVantagensDaFicha() {
        // Arrange
        VantagemConfig v1 = criarVantagem("Vantagem A", "1", 5);
        VantagemConfig v2 = criarVantagem("Vantagem B", "2", 5);

        autenticarComo(mestre);
        fichaVantagemService.comprar(ficha.getId(), v1.getId());
        fichaVantagemService.comprar(ficha.getId(), v2.getId());

        // Act
        List<FichaVantagem> vantagens = fichaVantagemService.listar(ficha.getId());

        // Assert
        assertThat(vantagens).hasSize(2);
    }

    // =========================================================
    // TESTES DE REGRA DE NEGÓCIO: SEM REMOÇÃO
    // =========================================================

    @Test
    @DisplayName("Não deve existir método de remoção de vantagem na ficha")
    void naoDevePermitirRemocaoDeVantagem() {
        // Verificar via repositório - nenhum delete deve ser exposto pelo service
        // O FichaVantagemService não deve ter método deletar/remover
        // Esta é uma verificação de design: o service não expõe deleção
        boolean temMetodoDeletar = java.util.Arrays.stream(FichaVantagemService.class.getMethods())
                .anyMatch(m -> m.getName().toLowerCase().contains("deletar")
                        || m.getName().toLowerCase().contains("remover")
                        || m.getName().toLowerCase().contains("excluir")
                        || m.getName().toLowerCase().contains("delete")
                        || m.getName().toLowerCase().contains("remove"));

        assertThat(temMetodoDeletar).isFalse();
    }

    // =========================================================
    // TESTES DE categoriaNome (Tarefa 3)
    // =========================================================

    @Test
    @DisplayName("Listar vantagens retorna categoriaNome quando vantagem tem categoria")
    void listarVantagnensRetornaCategoriaNomeQuandoTemCategoria() {
        // Arrange
        CategoriaVantagem categoria = categoriaVantagemRepository.save(
                CategoriaVantagem.builder()
                        .jogo(jogo)
                        .nome("Combate")
                        .ordemExibicao(1)
                        .build());

        VantagemConfig vantagem = VantagemConfig.builder()
                .jogo(jogo)
                .nome("Golpe Preciso")
                .formulaCusto("1")
                .nivelMaximo(3)
                .categoriaVantagem(categoria)
                .ordemExibicao(0)
                .build();
        vantagemConfigRepository.save(vantagem);

        autenticarComo(mestre);
        fichaVantagemService.comprar(ficha.getId(), vantagem.getId());

        // Act
        List<FichaVantagem> vantagens = fichaVantagemService.listar(ficha.getId());
        FichaVantagemResponse response = fichaVantagemMapper.toResponse(vantagens.get(0));

        // Assert
        assertThat(response.categoriaNome()).isEqualTo("Combate");
    }

    @Test
    @DisplayName("Listar vantagens retorna categoriaNome null quando vantagem não tem categoria")
    void listarVantagnensRetornaCategoriaNomeNullQuandoSemCategoria() {
        // Arrange — vantagem sem categoriaVantagem
        VantagemConfig vantagem = criarVantagem("Vantagem Sem Categoria", "1", 5);

        autenticarComo(mestre);
        fichaVantagemService.comprar(ficha.getId(), vantagem.getId());

        // Act
        List<FichaVantagem> vantagens = fichaVantagemService.listar(ficha.getId());
        FichaVantagemResponse response = fichaVantagemMapper.toResponse(vantagens.get(0));

        // Assert
        assertThat(response.categoriaNome()).isNull();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private VantagemConfig criarVantagem(String nome, String formulaCusto, int nivelMaximo) {
        VantagemConfig vantagem = VantagemConfig.builder()
                .jogo(jogo)
                .nome(nome)
                .formulaCusto(formulaCusto)
                .nivelMaximo(nivelMaximo)
                .ordemExibicao(0)
                .build();
        return vantagemConfigRepository.save(vantagem);
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
