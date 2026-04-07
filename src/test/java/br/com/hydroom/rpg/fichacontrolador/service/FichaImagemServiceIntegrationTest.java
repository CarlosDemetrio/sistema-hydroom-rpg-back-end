package br.com.hydroom.rpg.fichacontrolador.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO [Spec 011 T3 BLOCKED]: FichaImagemService não foi implementado neste lote.
 * A task T3 (FichaImagemService + CloudinaryUploadService) aguarda security review.
 *
 * Todos os cenários definidos em P1-T4-backend-testes-galeria.md estão pendentes:
 * - Listagem: 6 cenários (deveListarImagensDaFichaComoMestre, etc.)
 * - Upload: 7 cenários (deveAdicionarAvatarComoJogador, etc.)
 * - Edição: 4 cenários
 * - Remoção: 4 cenários
 *
 * Quando T3 for implementada, esta classe deve ser expandida com:
 * - @MockBean CloudinaryUploadService cloudinaryUploadService
 * - Setup: mestre, jogador, jogo, fichas
 * - Todos os cenários documentados em P1-T4-backend-testes-galeria.md
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaImagemService - Testes de Integração (TODO: aguardando T3)")
public class FichaImagemServiceIntegrationTest {

    @Test
    @DisplayName("TODO: Todos os cenários pendentes - aguardando Spec 011 T3")
    @Disabled("Spec 011 T3 bloqueada - FichaImagemService não implementado")
    void todoFichaImagemTestes() {
        // Placeholder para Spec 011 T3
        // 21 cenários definidos em P1-T4-backend-testes-galeria.md
    }
}
