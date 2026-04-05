# T8 — Testes de Integracao para Todos os Tipos de Efeito

> Fase: Backend | Dependencias: T2, T3, T4, T5, T6, T7 | Bloqueia: T9, T10, T11, T12
> Estimativa: 4–5 horas

---

## Objetivo

Criar testes de integracao que verificam o comportamento correto do `FichaCalculationService` para cada um dos 8 tipos de `VantagemEfeito`. Cada tipo deve ter pelo menos um cenario feliz e um cenario de borda.

---

## Arquivo de Teste

`test/java/.../service/FichaEfeitosCalculationIntegrationTest.java`

Usar `@ActiveProfiles("test")`, H2 in-memory, `@Transactional` para rollback automatico.

---

## Estrutura de Setup Compartilhado

```java
@BeforeEach
void setup() {
    jogo = criarJogo();
    
    // Configs de atributo
    atributoFOR = criarAtributo(jogo, "Forca", "FOR");
    atributoAGI = criarAtributo(jogo, "Agilidade", "AGI");
    atributoVIG = criarAtributo(jogo, "Vigor", "VIG");
    atributoSAB = criarAtributo(jogo, "Sabedoria", "SAB");
    
    // Config de bonus
    bonusBBA = criarBonus(jogo, "Bonus de Batalha de Ataque", "FOR + AGI");
    
    // Config de aptidao
    aptidaoFurtividade = criarAptidao(jogo, "Furtividade");
    
    // Config de membro
    membroCabeca = criarMembro(jogo, "Cabeca", BigDecimal.valueOf(0.10));
    
    // Config de dado
    dadoD6 = criarDado(jogo, "d6", 6, 2);  // ordemExibicao=2
    dadoD8 = criarDado(jogo, "d8", 8, 3);  // ordemExibicao=3
    
    // Ficha de teste
    ficha = criarFicha(jogo, nivel=5, renascimentos=0);
    fichaFOR = criarFichaAtributo(ficha, atributoFOR, base=15, nivel=3);
    fichaAGI = criarFichaAtributo(ficha, atributoAGI, base=12, nivel=2);
    fichaVIG = criarFichaAtributo(ficha, atributoVIG, base=20, nivel=4);
    fichaSAB = criarFichaAtributo(ficha, atributoSAB, base=10, nivel=1);
    fichaBBA = criarFichaBonus(ficha, bonusBBA);
    fichaAptidaoFurtividade = criarFichaAptidao(ficha, aptidaoFurtividade, base=5);
    fichaVida = criarFichaVida(ficha);
    fichaVidaCabeca = criarFichaVidaMembro(ficha, membroCabeca);
    fichaEssencia = criarFichaEssencia(ficha);
}
```

---

## Cenarios de Teste por Tipo de Efeito

### TC-1: BONUS_ATRIBUTO

```java
@Test
@DisplayName("BONUS_ATRIBUTO nivel 3 adiciona correto ao FichaAtributo.outros")
void deveBonusAtributoNivel3() {
    // Arrange
    VantagemConfig vantagem = criarVantagem(jogo, "Treinamento Forca");
    criarEfeito(vantagem, TipoEfeito.BONUS_ATRIBUTO,
        atributoFOR, null, null, null,
        null, BigDecimal.valueOf(2), null);  // +2 por nivel
    FichaVantagem fichaVantagem = criarFichaVantagem(ficha, vantagem, nivelAtual=3, custoPago=0);

    // Act
    List<FichaVantagem> vantagens = List.of(fichaVantagem);
    fichaCalculationService.recalcular(ficha, atributos, aptidoes, bonus, vida, membros, essencia, ameaca, vantagens);

    // Assert
    assertThat(fichaFOR.getOutros()).isEqualTo(6);       // 2 * 3
    assertThat(fichaFOR.getTotal()).isEqualTo(24);       // base=15 + nivel=3 + outros=6
}

@Test
@DisplayName("BONUS_ATRIBUTO ignora efeito com soft delete")
void deveIgnorarBonusAtributoSoftDeleted() {
    // ... efeito com deletedAt != null
    // Assert: fichaFOR.getOutros() == 0
}
```

### TC-2: BONUS_APTIDAO

```java
@Test
@DisplayName("BONUS_APTIDAO nivel 2 adiciona correto ao FichaAptidao.outros")
void deveBonusAptidaoNivel2() {
    // Arrange: vantagem com BONUS_APTIDAO em Furtividade, valorFixo=3
    // Act: recalcular
    // Assert: fichaAptidaoFurtividade.getOutros() == 3
    //         fichaAptidaoFurtividade.getTotal() == 8  (base=5 + outros=3)
}
```

### TC-3: BONUS_DERIVADO

```java
@Test
@DisplayName("BONUS_DERIVADO nivel 5 adiciona correto ao FichaBonus.vantagens")
void deveBonusDerivadoNivel5() {
    // Arrange: TCO com BONUS_DERIVADO em BBA, valorPorNivel=1
    // FichaVantagem nivelAtual=5
    // Act: recalcular
    // Assert: fichaBBA.getVantagens() == 5
    //         fichaBBA.getTotal() inclui os 5 pontos
}

@Test
@DisplayName("Multiplas vantagens BONUS_DERIVADO no mesmo alvo acumulam")
void deveAcumularBonusDerivaDoMesmoAlvo() {
    // Arrange: TCO (+1/nivel, nivel=3) + Outro (+2 fixo) ambos em BBA
    // Assert: fichaBBA.getVantagens() == 5  (3 + 2)
}
```

### TC-4: BONUS_VIDA

```java
@Test
@DisplayName("BONUS_VIDA nivel 4 aumenta FichaVida.vt e propaga para vidaTotal")
void deveBonusVidaNivel4() {
    // Arrange: "Saude de Ferro" com BONUS_VIDA, valorPorNivel=5
    // FichaVantagem nivelAtual=4
    // ficha.nivel=5, fichaVIG total esperado=24, renascimentos=0
    // Act: recalcular
    // Assert: fichaVida.getVt() == 20        (5 * 4)
    //         fichaVida.getVidaTotal() == 24 + 5 + 20 + 0 + 0 = 49
}

@Test
@DisplayName("BONUS_VIDA zerado corretamente ao recalcular sem vantagem")
void deveZerarVtAoRecalcularSemVantagem() {
    // fichaVida.setVt(50) manualmente
    // recalcular sem FichaVantagem com BONUS_VIDA
    // Assert: fichaVida.getVt() == 0
}
```

### TC-5: BONUS_VIDA_MEMBRO

```java
@Test
@DisplayName("BONUS_VIDA_MEMBRO adiciona bonus direto ao membro sem alterar pool global")
void deveBonusVidaMembroSemAlterarPool() {
    // Arrange: vantagem com BONUS_VIDA_MEMBRO em Cabeca, valorFixo=10
    // Act: recalcular (vidaTotal calculado sem VT de vantagem)
    // Assert: fichaVidaCabeca.getBonusVantagens() == 10
    //         fichaVidaCabeca.getVida() == floor(vidaTotal * 0.10) + 10
    //         fichaVida.getVt() == 0  (pool nao alterado)
}
```

### TC-6: BONUS_ESSENCIA

```java
@Test
@DisplayName("BONUS_ESSENCIA nivel 2 adiciona ao FichaEssencia.vantagens")
void deveBonusEssenciaNivel2() {
    // Arrange: vantagem com BONUS_ESSENCIA, valorFixo=5, valorPorNivel=2
    // FichaVantagem nivelAtual=2
    // Act: recalcular
    // Assert: fichaEssencia.getVantagens() == 9  (5 + 2*2)
}
```

### TC-7: DADO_UP

```java
@Test
@DisplayName("DADO_UP nivel 1 seleciona primeiro dado da sequencia")
void deveDadoUpNivel1PrimeiroD() {
    // dado sequencia: [d3(0), d4(1), d6(2), d8(3), d10(4), d12(5), d20(6)]
    // DADO_UP nivel 1 → posicao 0 → d3
    // Assert: fichaProspeccao.getDadoDisponivel().getNumeroFaces() == 3
}

@Test
@DisplayName("DADO_UP nivel 3 seleciona terceiro dado da sequencia")
void deveDadoUpNivel3TerceiroD() {
    // DADO_UP nivel 3 → posicao 2 → d6
    // Assert: fichaProspeccao.getDadoDisponivel().getNumeroFaces() == 6
}

@Test
@DisplayName("DADO_UP multiplas vantagens usa a maior posicao")
void deveDadoUpUsarMaiorPosicao() {
    // VantagemA DADO_UP nivel 2 → posicao 1 → d4
    // VantagemB DADO_UP nivel 5 → posicao 4 → d10
    // Assert: fichaProspeccao.getDadoDisponivel().getNumeroFaces() == 10 (d10 vence)
}

@Test
@DisplayName("DADO_UP nivel acima da sequencia usa o ultimo dado")
void deveDadoUpCapNaSequencia() {
    // Sequencia com 3 dados: [d4, d6, d8]
    // DADO_UP nivel 10 → posicao 9 → cap em posicao 2 → d8
    // Assert: fichaProspeccao.getDadoDisponivel().getNumeroFaces() == 8
}

@Test
@DisplayName("Sem DADO_UP ativo, dadoDisponivel fica null")
void deveSemDadoUpDadoDisponivel() {
    // Sem FichaVantagem com DADO_UP
    // Assert: fichaProspeccao.getDadoDisponivel() == null
}
```

### TC-8: FORMULA_CUSTOMIZADA

```java
@Test
@DisplayName("FORMULA_CUSTOMIZADA com atributo como variavel calcula corretamente")
void deveFormulasCustomizadaComAtributo() {
    // formula: "floor(FOR / 2) + nivel_vantagem"
    // atributoAlvo: null (atributoFOR em outros? confirmar semantica)
    // bonusAlvo: BBA
    // FichaVantagem nivelAtual=3, FOR total=24
    // Resultado: floor(24/2) + 3 = 12 + 3 = 15
    // Assert: fichaBBA.getVantagens() == 15
}

@Test
@DisplayName("FORMULA_CUSTOMIZADA com nivel_personagem como variavel")
void deveFormulasComNivelPersonagem() {
    // formula: "nivel_personagem * 2"
    // ficha.nivel=5
    // Resultado: 10
}

@Test
@DisplayName("FORMULA_CUSTOMIZADA com formula invalida nao quebra o calculo")
void deveFormulasInvalidaNaoQuebrarCalculo() {
    // Salvar efeito com formula invalida diretamente via repo (bypass da validacao de servico)
    // Act: recalcular
    // Assert: nenhuma excecao lancada; bonus permanece 0
}
```

---

## Cenarios de Borda (todos os tipos)

```java
@Test
@DisplayName("FichaVantagem nivel 0 nao aplica efeitos")
void deveIgnorarVantagemNivelZero() {
    // nivelAtual=0 — regra: minimo e 1, mas defensivo
}

@Test
@DisplayName("recalcular idempotente: chamado 3x produz mesmo resultado")
void deveRecalcularSerIdempotente() {
    // Chamar recalcular 3 vezes com mesmos dados
    // Assert: valores identicos nas 3 chamadas (sem dupla contagem)
}

@Test
@DisplayName("zerarContribuicoes limpa valores de calculo anterior")
void deveZerarContribuicoesAntesDeCalculo() {
    // Primeira passagem: vantagem nivel 3, FichaAtributo.outros = 6
    // Remove a vantagem, segunda passagem sem vantagens
    // Assert: FichaAtributo.outros == 0
}

@Test
@DisplayName("Sem vantagens, recalcular nao altera campos de contribuicao")
void deveSemVantagensManterCamposZerados() {
    // recalcular com lista de vantagens vazia
    // Assert: atributos.getOutros()==0, bonus.getVantagens()==0, vida.getVt()==0
}
```

---

## Criterios de Aceitacao Globais

- [ ] 8 testes principais (um por tipo de efeito) passando
- [ ] Cenarios de borda: idempotencia, soft delete, lista vazia
- [ ] Nenhum N+1: verificar que o log nao contem multiplas queries para efeitos
- [ ] `./mvnw test` passa com todos os 457 + novos testes
- [ ] Total de testes apos T8: >= 480 (estimativa conservadora)
