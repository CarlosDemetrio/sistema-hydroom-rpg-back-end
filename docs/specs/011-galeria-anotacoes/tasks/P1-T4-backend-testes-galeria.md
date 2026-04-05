# T4 — Backend: Testes de Integracao de Galeria + AnotacaoPasta

> Fase: P1 (Backend)
> Estimativa: 1 dia
> Depende de: T3 (Service + Controller de Galeria), T0 (Entity AnotacaoPasta)
> Bloqueia: nenhuma

---

## Objetivo

Criar `FichaImagemServiceIntegrationTest.java` com cobertura completa dos cenarios do `FichaImagemService`. Complementar `AnotacaoPastaServiceIntegrationTest.java` (ja iniciado em T0) com cenarios de integracao com anotacoes. O `CloudinaryUploadService` deve ser mockado em todos os testes para evitar chamadas reais ao Cloudinary.

---

## Estrategia de Mock do Cloudinary

**Regra critica:** Os testes de integracao NUNCA chamam o Cloudinary real. O bean `CloudinaryUploadService` e substituido por um mock via `@MockBean`.

```java
@MockBean
private CloudinaryUploadService cloudinaryUploadService;

@BeforeEach
void configurarMockCloudinary() {
    // Upload sempre retorna URL e publicId ficticios
    when(cloudinaryUploadService.upload(any(), anyString()))
        .thenReturn(Map.of(
            "url",       "https://res.cloudinary.com/test/image/upload/ficticias/test.jpg",
            "public_id", "rpg-fichas/1/fichas/42/test-uuid"
        ));

    // destroy nao retorna nada (void) — apenas verifica chamada quando necessario
    doNothing().when(cloudinaryUploadService).destroy(anyString());
}
```

---

## Estrutura do Teste FichaImagemServiceIntegrationTest

Seguir o padrao de `FichaAnotacaoServiceIntegrationTest.java`:
- `@SpringBootTest(webEnvironment = NONE)`
- `@Transactional` + `@ActiveProfiles("test")`
- `@BeforeEach` com limpeza manual na ordem correta de dependencias
- Metodo auxiliar `autenticarComo(usuario)`
- `@MockBean CloudinaryUploadService cloudinaryUploadService`

### Setup (`@BeforeEach`)

```
mestre, jogador, outroJogador → usuarios
jogo (criado pelo Mestre, inicializa configs)
jogoParticipante (jogador e outroJogador aprovados)
fichaDoJogador (criada pelo Mestre, jogadorId = jogador)
fichaDoOutroJogador (criada pelo Mestre, jogadorId = outroJogador)
fichaDeNpc (criada pelo Mestre, isNpc = true)
```

Metodo auxiliar para criar imagem de teste:

```java
private FichaImagem criarImagemMock(Ficha ficha, TipoImagem tipo, String titulo) {
    // Chama fichaImagemService.adicionar() com MultipartFile mockado
    MultipartFile arquivoMock = mock(MultipartFile.class);
    when(arquivoMock.getContentType()).thenReturn("image/jpeg");
    when(arquivoMock.getSize()).thenReturn(1024L);
    when(arquivoMock.isEmpty()).thenReturn(false);
    when(arquivoMock.getBytes()).thenReturn(new byte[]{1, 2, 3});
    return fichaImagemService.adicionar(ficha.getId(), arquivoMock, tipo, titulo, getUsuarioAtualId());
}
```

---

## Cenarios — FichaImagemServiceIntegrationTest

### Listagem

| Teste | Descricao |
|-------|-----------|
| `deveListarImagensDaFichaComoMestre` | Mestre lista imagens da ficha do jogador; retorna todas |
| `deveListarImagensComoJogadorDono` | Jogador lista imagens da propria ficha |
| `deveImpedirJogadorDeListarImagensDeOutro` | Jogador lista ficha de outro: ForbiddenException |
| `deveImpedirJogadorDeListarImagensDeNpc` | Jogador lista imagens de NPC: ForbiddenException |
| `deveListarVazioQuandoFichaSemImagens` | Ficha sem imagens; retorna lista vazia |
| `deveListarComAvatarPrimeiro` | Ficha com GALERIA e AVATAR; AVATAR aparece primeiro na lista |

### Upload

| Teste | Descricao |
|-------|-----------|
| `deveAdicionarAvatarComoJogador` | Jogador faz upload AVATAR na propria ficha; urlCloudinary e publicId persistidos |
| `deveAdicionarGaleriaComoMestre` | Mestre faz upload GALERIA em ficha de NPC |
| `deveSubstituirAvatarPromovendoAnteriorParaGaleria` | Upload novo AVATAR: avatar anterior vira GALERIA |
| `deveVerificarChamadaAoCloudinaryNoUpload` | Verificar que `cloudinaryUploadService.upload()` foi chamado com folder correto |
| `deveImpedirUploadPorJogadorEmFichaAlheia` | Jogador faz upload em ficha de outro: ForbiddenException |
| `deveImpedirUploadPorJogadorEmNpc` | Jogador faz upload em ficha de NPC: ForbiddenException |
| `deveImpedirUploadApos20Imagens` | Ficha com 20 imagens; 21a: BusinessException |

### Edicao

| Teste | Descricao |
|-------|-----------|
| `deveAtualizarTituloComoMestre` | Mestre atualiza titulo; campo atualizado |
| `deveAtualizarOrdemExibicao` | Ordem alterada de 0 para 3 |
| `deveMaterCamposNaoEnviadosNoUpdate` | Titulo enviado como null: titulo original preservado |
| `deveImpedirEdicaoPorJogadorEmFichaAlheia` | Jogador edita imagem de outro: ForbiddenException |

### Remocao

| Teste | Descricao |
|-------|-----------|
| `deveDeletarImagemComoMestre` | Mestre deleta; soft delete confirmado + Cloudinary destroy chamado |
| `deveDeletarImagemComoJogadorDono` | Jogador deleta imagem da propria ficha |
| `deveImpedirDeletarImagemEmFichaAlheia` | Jogador deleta imagem de outro: ForbiddenException |
| `deveLograrFalhaCloudinaryMasCompletarSoftDelete` | Cloudinary.destroy lanca excecao; soft delete local ainda ocorre; publicId permanece no banco |

---

## Cenarios — AnotacaoPastaServiceIntegrationTest (complemento)

Adicionar ao teste ja criado em T0:

| Teste | Descricao |
|-------|-----------|
| `deveCriarAnotacaoNaPasta` | Criar anotacao com pastaPaiId valido: anotacao vinculada a pasta |
| `deveMoverAnotacaoEntrePastas` | PUT anotacao com novo pastaPaiId: anotacao muda de pasta |
| `deveListarAnotacoesFiltrandoPorPasta` | GET /anotacoes?pastaPaiId=X: apenas anotacoes da pasta X retornadas |
| `deveListarAnotacoesRaiz` | GET /anotacoes?pastaPaiId=null (sem filtro): anotacoes sem pasta retornadas |

---

## Verificacoes de Soft Delete

Apos deletar imagem, verificar:

```java
// Verificar soft delete local
FichaImagem deletada = fichaImagemRepository.findById(imagemId).orElse(null);
if (deletada != null) {
    assertThat(deletada.getDeletedAt()).isNotNull();
}
// Se findById retorna empty, o @SQLRestriction filtrou — comportamento correto

// Verificar chamada ao Cloudinary
verify(cloudinaryUploadService).destroy("rpg-fichas/1/fichas/42/test-uuid");
```

---

## Cenario: Cloudinary falha ao deletar

```java
@Test
@DisplayName("Deve completar soft delete mesmo quando Cloudinary.destroy falha")
void deveCompletarSoftDeleteMesmoQuandoCloudinaryFalha() {
    // Arrange
    autenticarComo(mestre);
    var imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Teste");
    doThrow(new RuntimeException("Cloudinary indisponivel"))
        .when(cloudinaryUploadService).destroy(anyString());

    // Act — nao deve lancar excecao
    assertThatCode(() ->
        fichaImagemService.deletar(fichaDoJogador.getId(), imagem.getId(), getUsuarioAtualId())
    ).doesNotThrowAnyException();

    // Assert — soft delete ocorreu localmente
    // (verificar via EntityManager ou query nativa ignorando @SQLRestriction)
}
```

---

## Criterios de Aceite

- [ ] `@MockBean CloudinaryUploadService` substituindo o bean real em todos os testes
- [ ] Todos os cenarios de upload verificam que `urlCloudinary` e `publicId` foram persistidos (usando valores do mock)
- [ ] Cenario de substituicao de avatar confirma que avatar anterior mudou para `tipoImagem=GALERIA`
- [ ] Cenario de limite confirma que a 21a imagem lanca a excecao correta
- [ ] Cenario de falha do Cloudinary no delete confirma que o soft delete local ocorreu
- [ ] Todos os cenarios listados implementados e passando
- [ ] Nenhum teste depende de estado de outro (setup independente)
- [ ] `./mvnw test` continua passando integralmente
