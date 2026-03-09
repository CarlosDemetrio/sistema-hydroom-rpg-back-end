package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService.TipoSigla;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("SiglaValidationService - Testes de Integração")
class SiglaValidationServiceIntegrationTest {

    @Autowired
    private SiglaValidationService siglaValidationService;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private BonusConfigRepository bonusRepository;

    @Autowired
    private VantagemConfigRepository vantagemRepository;

    private Jogo jogo;
    private Jogo outroJogo;

    @BeforeEach
    void setUp() {
        vantagemRepository.deleteAll();
        bonusRepository.deleteAll();
        atributoRepository.deleteAll();
        jogoRepository.deleteAll();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Teste")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outra Campanha")
            .dataInicio(LocalDate.now())
            .jogoAtivo(false)
            .build());
    }

    @AfterEach
    void tearDown() {
        vantagemRepository.deleteAll();
        bonusRepository.deleteAll();
        atributoRepository.deleteAll();
        jogoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve aceitar sigla nova sem conflito")
    void deveAceitarSiglaNova() {
        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel("FOR", jogo.getId(), null, TipoSigla.ATRIBUTO)
        );
    }

    @Test
    @DisplayName("Deve rejeitar sigla de bônus já usada como abreviação de atributo")
    void deveRejeitarSiglaDeBonus_QuandoAtributoJaUsa() {
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Força").abreviacao("FOR").ordemExibicao(0).build());

        assertThrows(ConflictException.class, () ->
            siglaValidationService.validarSiglaDisponivel("FOR", jogo.getId(), null, TipoSigla.BONUS)
        );
    }

    @Test
    @DisplayName("Deve rejeitar sigla de atributo já usada em bônus")
    void deveRejeitarSiglaDeAtributo_QuandoBonusJaUsa() {
        bonusRepository.save(BonusConfig.builder()
            .jogo(jogo).nome("Bônus Base Ataque").sigla("BBA").ordemExibicao(0).formulaBase("NIVEL").build());

        assertThrows(ConflictException.class, () ->
            siglaValidationService.validarSiglaDisponivel("BBA", jogo.getId(), null, TipoSigla.ATRIBUTO)
        );
    }

    @Test
    @DisplayName("Deve rejeitar sigla de vantagem já usada como abreviação de atributo")
    void deveRejeitarSiglaDeVantagem_QuandoAtributoJaUsa() {
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Agilidade").abreviacao("AGI").ordemExibicao(0).build());

        assertThrows(ConflictException.class, () ->
            siglaValidationService.validarSiglaDisponivel("AGI", jogo.getId(), null, TipoSigla.VANTAGEM)
        );
    }

    @Test
    @DisplayName("Deve aceitar mesma sigla ao editar o próprio atributo (excludeId)")
    void deveAceitarMesmaSigla_AoEditarProprioAtributo() {
        AtributoConfig atributo = atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Força").abreviacao("FOR").ordemExibicao(0).build());

        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel("FOR", jogo.getId(), atributo.getId(), TipoSigla.ATRIBUTO)
        );
    }

    @Test
    @DisplayName("Deve aceitar mesma sigla ao editar o próprio bônus (excludeId)")
    void deveAceitarMesmaSigla_AoEditarProprioBonus() {
        BonusConfig bonus = bonusRepository.save(BonusConfig.builder()
            .jogo(jogo).nome("Bônus Ataque").sigla("BBA").ordemExibicao(0).formulaBase("NIVEL").build());

        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel("BBA", jogo.getId(), bonus.getId(), TipoSigla.BONUS)
        );
    }

    @Test
    @DisplayName("Deve aceitar sigla nula sem disparar validação")
    void deveAceitarSiglaNula() {
        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel(null, jogo.getId(), null, TipoSigla.VANTAGEM)
        );
    }

    @Test
    @DisplayName("Deve aceitar sigla em branco sem disparar validação")
    void deveAceitarSiglaEmBranco() {
        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel("", jogo.getId(), null, TipoSigla.VANTAGEM)
        );
    }

    @Test
    @DisplayName("Deve ignorar sigla de outro jogo (sem conflito cross-jogo)")
    void deveIgnorarSiglaDeOutroJogo() {
        atributoRepository.save(AtributoConfig.builder()
            .jogo(outroJogo).nome("Força").abreviacao("FOR").ordemExibicao(0).build());

        assertDoesNotThrow(() ->
            siglaValidationService.validarSiglaDisponivel("FOR", jogo.getId(), null, TipoSigla.ATRIBUTO)
        );
    }

    @Test
    @DisplayName("listarSiglasDoJogo retorna todas as siglas do jogo ordenadas")
    void deveListarSiglasDoJogo() {
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Força").abreviacao("FOR").ordemExibicao(0).build());
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Agilidade").abreviacao("AGI").ordemExibicao(1).build());
        bonusRepository.save(BonusConfig.builder()
            .jogo(jogo).nome("Bônus Ataque").sigla("BBA").ordemExibicao(0).formulaBase("NIVEL").build());
        vantagemRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Ambidestria").sigla("AMB").nivelMaximo(5).formulaCusto("NIVEL * 2").ordemExibicao(0).build());

        // Sigla de outro jogo — não deve aparecer
        atributoRepository.save(AtributoConfig.builder()
            .jogo(outroJogo).nome("Vigor").abreviacao("VIG").ordemExibicao(0).build());

        List<SiglaEmUsoResponse> siglas = siglaValidationService.listarSiglasDoJogo(jogo.getId());

        assertThat(siglas).hasSize(4);
        assertThat(siglas).extracting(SiglaEmUsoResponse::sigla)
            .containsExactly("AGI", "AMB", "BBA", "FOR");
    }

    @Test
    @DisplayName("listarSiglasDoJogo exclui vantagens sem sigla")
    void deveExcluirVantagemSemSigla() {
        vantagemRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Vantagem Sem Sigla").nivelMaximo(3).formulaCusto("NIVEL").ordemExibicao(0).build());

        List<SiglaEmUsoResponse> siglas = siglaValidationService.listarSiglasDoJogo(jogo.getId());

        assertThat(siglas).isEmpty();
    }
}
