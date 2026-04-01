package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
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
 * Testes de integração para o endpoint NPC (POST /api/v1/jogos/{jogoId}/npcs).
 *
 * <p>Valida o comportamento do FichaService ao criar NPCs via endpoint NPC do FichaController.
 * Cobre:</p>
 * <ul>
 *   <li>Criação de NPC com isNpc=true e jogadorId=null</li>
 *   <li>Rejeição de criação de NPC por JOGADOR</li>
 *   <li>Listagem de NPCs exclusivamente para MESTRE</li>
 *   <li>Separação correta entre fichas de jogador e NPCs</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("NPC - Testes de Integração (via FichaService)")
class NpcFichaMestreIntegrationTest {

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
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
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre NPC")
                .email("mestre.npc" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-npc-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador NPC Test")
                .email("jogador.npc" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-npc-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha NPC " + n)
                .build());

        // Adicionar jogador como participante aprovado
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());
    }

    // =========================================================
    // TESTES DE CRIAÇÃO DE NPC
    // =========================================================

    @Test
    @DisplayName("Mestre deve criar NPC com isNpc=true e jogadorId=null")
    void deveCriarNpcComSucesso() {
        // Arrange
        autenticarComo(mestre);
        // O endpoint NPC do controller converte NpcCreateRequest para CreateFichaRequest com isNpc=true
        var request = new CreateFichaRequest(jogo.getId(), "Goblin Chefe", null, null, null, null, null, null, true);

        // Act
        Ficha npc = fichaService.criar(request);

        // Assert
        assertThat(npc.getId()).isNotNull();
        assertThat(npc.getNome()).isEqualTo("Goblin Chefe");
        assertThat(npc.isNpc()).isTrue();
        assertThat(npc.getJogadorId()).isNull();
        assertThat(npc.getJogo().getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Mestre deve criar NPC com sub-registros de vida, essência e ameaça inicializados")
    void deveCriarNpcComSubRegistrosAutomaticamente() {
        // Arrange
        autenticarComo(mestre);

        // Criar uma ficha de referência para saber quantos atributos/aptidões o jogo tem configurado
        Ficha fichaRef = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha Referência",
                        jogador.getId(), null, null, null, null, null, false));
        int atributosEsperados = fichaAtributoRepository.findByFichaId(fichaRef.getId()).size();

        var request = new CreateFichaRequest(jogo.getId(), "Dragão Ancião", null, null, null, null, null, null, true);

        // Act
        Ficha npc = fichaService.criar(request);
        Long npcId = npc.getId();

        // Assert - sub-registros obrigatórios (1 por ficha) sempre criados
        assertThat(fichaVidaRepository.findByFichaId(npcId)).isPresent();
        assertThat(fichaEssenciaRepository.findByFichaId(npcId)).isPresent();
        assertThat(fichaAmeacaRepository.findByFichaId(npcId)).isPresent();

        // Assert - atributos do NPC iguais aos da ficha de referência (mesmo jogo = mesmas configs)
        int atributosNpc = fichaAtributoRepository.findByFichaId(npcId).size();
        assertThat(atributosNpc).isEqualTo(atributosEsperados);
    }

    @Test
    @DisplayName("Jogador não pode criar NPC — service lança ForbiddenException (SP1-T18)")
    void deveRejeitarCriacaoDeNpcPorJogador() {
        // Arrange
        autenticarComo(jogador);
        var request = new CreateFichaRequest(jogo.getId(), "NPC Tentativa", null, null, null, null, null, null, true);

        // Act & Assert — proteção no service garantida independentemente do @PreAuthorize do controller
        assertThrows(ForbiddenException.class, () -> fichaService.criar(request));
    }

    @Test
    @DisplayName("Jogador não pode listar NPCs (ForbiddenException esperada)")
    void deveRejeitarListagemDeNpcsPorJogador() {
        // Arrange
        autenticarComo(jogador);

        // Assert
        assertThrows(ForbiddenException.class,
                () -> fichaService.listarNpcs(jogo.getId()));
    }

    @Test
    @DisplayName("Mestre deve listar apenas NPCs via listarNpcs()")
    void deveMestreListarApenasNpcs() {
        // Arrange - criar ficha de jogador e NPC
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Jogador",
                jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC 1",
                null, null, null, null, null, null, true));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC 2",
                null, null, null, null, null, null, true));

        // Act
        List<Ficha> npcs = fichaService.listarNpcs(jogo.getId());

        // Assert - apenas NPCs retornados
        assertThat(npcs).hasSize(2);
        assertThat(npcs).allMatch(Ficha::isNpc);
        assertThat(npcs).allMatch(f -> f.getJogadorId() == null);
        assertThat(npcs).extracting(Ficha::getNome)
                .containsExactlyInAnyOrder("NPC 1", "NPC 2");
    }

    @Test
    @DisplayName("Fichas de jogador não aparecem na listagem de NPCs")
    void deveSepararFichasDeJogadorDosNpcs() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Aragorn",
                jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Orc Guerreiro",
                null, null, null, null, null, null, true));

        // Act
        List<Ficha> npcs = fichaService.listarNpcs(jogo.getId());
        List<Ficha> todasFichas = fichaService.listar(jogo.getId());

        // Assert - listarNpcs retorna apenas NPC
        assertThat(npcs).hasSize(1);
        assertThat(npcs.get(0).isNpc()).isTrue();
        assertThat(npcs.get(0).getNome()).isEqualTo("Orc Guerreiro");

        // Assert - listar retorna todos (Mestre vê tudo)
        assertThat(todasFichas).hasSize(2);
    }

    @Test
    @DisplayName("NPC não deve ter jogadorId mesmo que seja passado no request")
    void deveCriarNpcSemJogadorId() {
        // Arrange - mesmo que o Mestre passe um jogadorId, NPC não deve ter jogadorId
        autenticarComo(mestre);
        // O FichaService.criar define: jogadorId = isNpc ? null : jogadorId
        var request = new CreateFichaRequest(jogo.getId(), "Demônio Ancião",
                jogador.getId(), // passando jogadorId mas isNpc=true
                null, null, null, null, null, true);

        // Act
        Ficha npc = fichaService.criar(request);

        // Assert - NPC não tem jogadorId mesmo com jogadorId no request
        assertThat(npc.isNpc()).isTrue();
        assertThat(npc.getJogadorId()).isNull();
    }

    @Test
    @DisplayName("Deve atualizar descrição do NPC com sucesso")
    void deveAtualizarDescricaoDoNpc() {
        // Arrange
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Lich Mago", null, null, null, null, null, null, true));
        String descricao = "Um antigo mago que buscou a imortalidade através da necromancia.";

        // Act
        Ficha npcAtualizado = fichaService.atualizarDescricao(npc.getId(), descricao);

        // Assert
        assertThat(npcAtualizado.getDescricao()).isEqualTo(descricao);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
