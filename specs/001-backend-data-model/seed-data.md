# Seed Data: Template Klayrah Padrão

**Feature Branch**: `001-backend-data-model`  
**Date**: 2026-02-01  
**Origem**: Valores extraídos do sistema legado (React)

Este documento contém os dados padrão para o Template "Klayrah Padrão" que será aplicado quando um Mestre criar um novo jogo.

---

## 1. Atributos (ATRIBUTO_CONFIG)

| ordem | nome | descricao | formula_impeto | descricao_impeto |
|-------|------|-----------|----------------|------------------|
| 1 | Força | Potência física e capacidade de carga | TOTAL * 3 | Capacidade de carga em kg |
| 2 | Agilidade | Velocidade e coordenação motora | FLOOR(TOTAL / 3) | Deslocamento em metros |
| 3 | Vigor | Resistência física e vitalidade | FLOOR(TOTAL / 10) | Redução de Dano (RD) |
| 4 | Sabedoria | Conhecimento arcano e resistência mental | FLOOR(TOTAL / 10) | Redução de Dano Mágico (RDM) |
| 5 | Intuição | Percepção e sexto sentido | MIN(FLOOR(TOTAL / 20), 3) | Pontos de Sorte |
| 6 | Inteligência | Raciocínio lógico e capacidade de comando | FLOOR(TOTAL / 20) | Comando |
| 7 | Astúcia | Esperteza e pensamento estratégico | FLOOR(TOTAL / 10) | Estratégia |

---

## 2. Níveis (NIVEL_CONFIG)

Tabela completa de XP por nível com limitadores e pontos de atributo.

| numero_nivel | experiencia_necessaria | limitador_atributo | pontos_atributo | descricao | permite_renascimento |
|--------------|------------------------|-------------------|-----------------|-----------|---------------------|
| 0 | 0 | 10 | 0 | Iniciante | false |
| 1 | 1000 | 10 | 3 | Novato | false |
| 2 | 3000 | 50 | 3 | Aprendiz | false |
| 3 | 6000 | 50 | 3 | Aprendiz | false |
| 4 | 10000 | 50 | 3 | Aprendiz | false |
| 5 | 15000 | 50 | 3 | Experiente | false |
| 6 | 21000 | 50 | 3 | Experiente | false |
| 7 | 28000 | 50 | 3 | Experiente | false |
| 8 | 36000 | 50 | 3 | Experiente | false |
| 9 | 45000 | 50 | 3 | Experiente | false |
| 10 | 55000 | 50 | 3 | Veterano | false |
| 11 | 66000 | 50 | 3 | Veterano | false |
| 12 | 78000 | 50 | 3 | Veterano | false |
| 13 | 91000 | 50 | 3 | Veterano | false |
| 14 | 105000 | 50 | 3 | Veterano | false |
| 15 | 120000 | 50 | 3 | Mestre | false |
| 16 | 136000 | 50 | 3 | Mestre | false |
| 17 | 153000 | 50 | 3 | Mestre | false |
| 18 | 171000 | 50 | 3 | Mestre | false |
| 19 | 190000 | 50 | 3 | Mestre | false |
| 20 | 210000 | 50 | 3 | Grão-Mestre | false |
| 21 | 231000 | 75 | 3 | Campeão | false |
| 22 | 253000 | 75 | 3 | Campeão | false |
| 23 | 276000 | 75 | 3 | Campeão | false |
| 24 | 300000 | 75 | 3 | Campeão | false |
| 25 | 325000 | 75 | 3 | Lenda | false |
| 26 | 351000 | 100 | 3 | Herói | false |
| 27 | 378000 | 100 | 3 | Herói | false |
| 28 | 406000 | 100 | 3 | Herói | false |
| 29 | 435000 | 100 | 3 | Herói | false |
| 30 | 465000 | 100 | 3 | Semideus | false |
| 31 | 496000 | 120 | 3 | Ascendente | true |
| 32 | 528000 | 120 | 3 | Ascendente | true |
| 33 | 561000 | 120 | 3 | Ascendente | true |
| 34 | 595000 | 120 | 3 | Ascendente | true |
| 35 | 630000 | 120 | 3 | Transcendente | true |

---

## 3. Tipos de Aptidão (TIPO_APTIDAO)

| ordem | nome | descricao |
|-------|------|-----------|
| 1 | Física | Aptidões relacionadas ao corpo e movimento |
| 2 | Mental | Aptidões relacionadas à mente e percepção |

---

## 4. Aptidões (APTIDAO_CONFIG)

### 4.1 Aptidões Físicas

| ordem | nome | descricao | tipo |
|-------|------|-----------|------|
| 1 | Acrobacia | Movimentos acrobáticos, saltos e piruetas | Física |
| 2 | Guarda | Capacidade de manter posição defensiva | Física |
| 3 | Aparar | Habilidade de bloquear ataques com arma | Física |
| 4 | Atletismo | Força física, corrida, escalada | Física |
| 5 | Resvalar | Esquiva e evasão de ataques | Física |
| 6 | Resistência | Suportar condições adversas e danos | Física |
| 7 | Perseguição | Perseguir ou fugir de alvos | Física |
| 8 | Natação | Nadar e se mover na água | Física |
| 9 | Furtividade | Mover-se silenciosamente e sem ser visto | Física |
| 10 | Prestidigitação | Mãos ágeis, truques e furtos | Física |
| 11 | Conduzir | Pilotar veículos e montarias | Física |
| 12 | Arte da Fuga | Escapar de amarras e prisões | Física |

### 4.2 Aptidões Mentais

| ordem | nome | descricao | tipo |
|-------|------|-----------|------|
| 1 | Idiomas | Conhecimento de línguas e dialetos | Mental |
| 2 | Observação | Perceber detalhes e mudanças no ambiente | Mental |
| 3 | Falsificar | Criar documentos e objetos falsos | Mental |
| 4 | Prontidão | Reação rápida a situações inesperadas | Mental |
| 5 | Auto Controle | Controlar emoções e manter a calma | Mental |
| 6 | Sentir Motivação | Ler intenções e motivações de outros | Mental |
| 7 | Sobrevivência | Sobreviver na natureza selvagem | Mental |
| 8 | Investigar | Descobrir informações e pistas | Mental |
| 9 | Blefar | Enganar e mentir convincentemente | Mental |
| 10 | Atuação | Interpretar papéis e disfarces | Mental |
| 11 | Diplomacia | Negociar e persuadir | Mental |
| 12 | Operação de Mecanismos | Operar dispositivos mecânicos | Mental |

---

## 5. Bônus Calculados (BONUS_CONFIG)

| ordem | nome | descricao | formula_base |
|-------|------|-----------|--------------|
| 1 | B.B.A | Bônus Base de Ataque | FLOOR((FORCA + AGILIDADE) / 3) |
| 2 | Bloqueio | Capacidade de bloquear ataques | FLOOR((FORCA + VIGOR) / 3) |
| 3 | Reflexo | Velocidade de reação | FLOOR((AGILIDADE + ASTUCIA) / 3) |
| 4 | B.B.M | Bônus Base Mágico | FLOOR((SABEDORIA + INTELIGENCIA) / 3) |
| 5 | Percepção | Perceber detalhes e ameaças | FLOOR((INTELIGENCIA + INTUICAO) / 3) |
| 6 | Raciocínio | Capacidade de raciocínio lógico | FLOOR((INTELIGENCIA + ASTUCIA) / 3) |

---

## 6. Membros do Corpo (MEMBRO_CORPO_CONFIG)

| ordem | nome | porcentagem_vida |
|-------|------|------------------|
| 1 | Cabeça | 0.75 |
| 2 | Tronco | 1.00 |
| 3 | Braço Direito | 0.25 |
| 4 | Braço Esquerdo | 0.25 |
| 5 | Perna Direita | 0.25 |
| 6 | Perna Esquerda | 0.25 |
| 7 | Sangue | 1.00 |

---

## 7. Classes de Personagem (CLASSE_PERSONAGEM)

| ordem | nome | descricao | nivel_minimo |
|-------|------|-----------|--------------|
| 1 | Guerreiro | Especialista em combate corpo a corpo | 1 |
| 2 | Arqueiro | Mestre em armas de longo alcance | 1 |
| 3 | Monge | Lutador marcial com disciplina mental | 1 |
| 4 | Berserker | Guerreiro selvagem movido pela fúria | 1 |
| 5 | Assassino | Especialista em ataques furtivos | 1 |
| 6 | Fauno (Herdeiro) | Criatura mística ligada à natureza | 1 |
| 7 | Mago | Conjurador de magias arcanas | 1 |
| 8 | Feiticeiro | Usuário de magia inata | 1 |
| 9 | Necromante | Mestre das artes da morte | 1 |
| 10 | Sacerdote | Canalizador de poder divino | 1 |
| 11 | Ladrão | Especialista em furtos e trapaças | 1 |
| 12 | Negociante | Mestre em comércio e persuasão | 1 |

---

## 8. Dados de Prospecção (DADO_PROSPECCAO_CONFIG)

| ordem | nome | valor_maximo |
|-------|------|--------------|
| 1 | d3 | 3 |
| 2 | d4 | 4 |
| 3 | d6 | 6 |
| 4 | d8 | 8 |
| 5 | d10 | 10 |
| 6 | d12 | 12 |

---

## 9. Configuração de Vida (VIDA_CONFIG)

```
formula: VIGOR + NIVEL + VANTAGENS + RENASCIMENTOS + OUTROS
descricao: Vida total do personagem. Vida por membro = Vida Total × Porcentagem do membro.
```

---

## 10. Configuração de Essência (ESSENCIA_CONFIG)

```
formula: FLOOR((VIGOR + SABEDORIA) / 2) + NIVEL + RENASCIMENTOS + VANTAGENS + OUTROS
descricao: Recurso mágico/espiritual do personagem. Essência Restante = Total - Gastos.
```

---

## 11. Configuração de Ameaça (AMEACA_CONFIG)

```
formula: NIVEL + ITENS + TITULOS + RENASCIMENTOS + OUTROS
descricao: Indica o nível de perigo/poder que o personagem representa.
```

---

## 12. Configuração de Pontos de Vantagem (PONTOS_VANTAGEM_CONFIG)

```
pontos_por_nivel: 3
pontos_iniciais: 0
```

---

## 13. Categorias de Vantagens (CATEGORIA_VANTAGEM)

| ordem | nome | descricao |
|-------|------|-----------|
| 1 | Treinamento Físico | Foco no aprimoramento do combate corporal e defesa física |
| 2 | Treinamento Mental | Foco em habilidades mágicas, percepção sobrenatural e raciocínio |
| 3 | Ação | Habilidades ativas para controle de turno |
| 4 | Reação | Habilidades reativas para controle de turno |
| 5 | Vantagem de Atributo | Poderes derivados diretamente dos atributos básicos |
| 6 | Vantagem Geral | Habilidades utilitárias e de sobrevivência |
| 7 | Vantagem Histórica | Background, recursos e conexões sociais |
| 8 | Vantagem de Renascimento | Habilidades exclusivas para quem renasceu |

---

## 14. Vantagens - Treinamentos Físicos (VANTAGEM_CONFIG)

| nome | sigla | custo_base | niveis_max | bonus_por_nivel | pre_requisito | notas |
|------|-------|------------|------------|-----------------|---------------|-------|
| Treinamento em Combate Ofensivo | TCO | 4 | 10 | +1 B.B.A | B.B.A 5+ | Dados: 1D3 + D.UP/nível (até D10, reinicia) |
| Treinamento em Combate Defensivo | TCD | 4 | 10 | +1 Bloqueio | Bloqueio 5+ | Gera RD natural (contusão) |
| Treinamento em Combate Evasivo | TCE | 2 | 10 | +2 Reflexo | Reflexo 5+ | Bônus dobrado (+2 por nível) |

---

## 15. Vantagens - Treinamentos Mentais (VANTAGEM_CONFIG)

| nome | sigla | custo_base | niveis_max | bonus_por_nivel | pre_requisito | notas |
|------|-------|------------|------------|-----------------|---------------|-------|
| Treinamento Mágico | TM | 4 | 10 | +1 B.B.M | B.B.M 5+ | Contempla todas habilidades mágicas |
| Treinamento em Percepção Mágica | TPM | 2 | 10 | +2 Percepção | Percepção 5+ | Bônus dobrado (+2 por nível) |
| Treinamento Lógico | TL | 4 | 10 | +1 Raciocínio | Raciocínio 5+ | Base mental e lógica |
| Treinamento em Manipulação | T.M | 4 | 5 | +2 Aptidões Mentais | B.Mental 8+ | Afeta todas aptidões mentais |

---

## 16. Vantagens - Ações (VANTAGEM_CONFIG)

| nome | custo_base | niveis_max | pre_requisito | efeito |
|------|------------|------------|---------------|--------|
| Ataque Adicional | 10 | 1 | Bônus Ofensivo 15+ | Realiza um ataque extra após ação ofensiva |
| Ataque Sentai | 10 | 1 | Raciocínio 5+ | Força percepção em ataque conjunto |

---

## 17. Vantagens - Reações (VANTAGEM_CONFIG)

| nome | custo_base | niveis_max | pre_requisito | efeito |
|------|------------|------------|---------------|--------|
| Contra-Ataque | 5 | 3 | Bônus Ofensivo 18+ | Reagir atacando de volta (Dificuldade +5) |
| Intercepção | 10 | 1 | Reflexo ou Percepção 30+ | Interromper ação adversária |
| Reflexos Especiais | 5 | 1 | Bônus Base 10+ | Reações com habilidades |
| Instinto Heroico | 5 | 1 | Bônus Base 10+ | Ação padrão para salvar alguém |
| Deflexão Heroica | 5 | 3 | Bônus Base 10+ | Salvar a si e outro (Dificuldade +5) |
| Reflexos Aprimorados | 3 | 3 | Reflexo Base 7+ | Reduz dano pela metade se superar por -1×nível |
| Instinto Sobrevivência | 3 | 3 | Reflexo Base 7+ | Desvia de ataque múltiplo se superar por -1×nível |

---

## 18. Vantagens - Atributos (VANTAGEM_CONFIG)

| nome | sigla | custo_base | niveis_max | pre_requisito | efeito |
|------|-------|------------|------------|---------------|--------|
| Capacidade de Força Máxima | CFM | 6 | 1 | A cada 10 de Força | 1D3 em danos por contusão |
| Domínio de Força | DM | 2 | 2 | Ter CFM | Eleva o dado acima 1x/nível |
| Destreza Felina | DF | 5 | 3 | A cada 10 de Agilidade | -1 em penalidades de terreno |
| Tenacidade | - | 6 | 1 | A cada 10 de Vigor | 1D3 em RD por contusão |
| Domínio de Vigor | DV | 2 | 2 | Ter Tenacidade | Eleva o dado acima 1x/nível |
| Sabedoria de Gamaiel | SG | 3 | 3 | A cada 10 de Sabedoria | +1 NV em Aspecto Mágico |
| Inteligência de Nyck | IN | 2 | 3 | Raciocínio Base 7+ | +0.5x no Multiplicador |

---

## 19. Vantagens - Gerais (VANTAGEM_CONFIG)

| nome | custo_base | niveis_max | pre_requisito | efeito |
|------|------------|------------|---------------|--------|
| Saúde de Ferro | 3 | 4 | Vigor 3+ | +5 de Vida por nível |
| Concentração | 3 | 4 | Sabedoria 3+ | +5 de Animus por nível |
| Sentidos Aguçados | 3 | 5 | Nível 1+ | +2 de Percepção em 1 sentido |
| Saque Rápido | 3 | 2 | Agilidade 10+ | Sacar sem gastar P.A |
| Ambidestria | 5 | 1 | Agilidade 10+ | Uso pleno de ambas as mãos |
| Memória Fotográfica | 10 | 1 | Raciocínio 10+ | Memória visual plena |

---

## 20. Vantagens - Históricas (VANTAGEM_CONFIG)

| nome | custo_base | niveis_max | efeito |
|------|------------|------------|--------|
| Riqueza | 3 | 7 | 1 Grade de Riqueza por nível |
| Herança | 3 | 1 | 1D3 de Riqueza aplicada |
| Ofícios | 2 | 2 | 1 Profissão por nível |
| Treino de Ofício | 4 | 10 | +1 para exercer ofícios por nível |
| Índole Aplicada | 2 | 5 | Mudar índole com 1 alvo por nível |
| Capangas | 5 | 5 | Um aliado por nível |
| Vínculo com Organização | 7 | 3 | Influência em organizações |

---

## 21. Vantagens - Renascimento (VANTAGEM_CONFIG)

| nome | custo_base | niveis_max | pre_requisito | efeito |
|------|------------|------------|---------------|--------|
| Último Sigilo | 5 | 1 | 1 Renascimento | Ocultar manifestação mágica |
| Escaramuça | 3 | 3 | 1 Renascimento | Ataques falsos e manobras |
| Dano Não Letal | 5 | 1 | 1 Renascimento | Converte danos em contusão |
| Controle de Dano | 5 | 1 | 1 Renascimento | Escolher quanto do dano usar |
| Armas Improvisadas | 4 | 10 | 1 Renascimento | 1D3+D.UP/nível e +1 B.M/nível com objetos |
| Memória Eidética | 10 | 1 | 1 Renascimento | Lembrar com todos os sentidos |
| Senso Numérico | 10 | 1 | 1 Renascimento | Precisão numérica absoluta |
| Pensamento Bifurcado | 10 | 1 | 1 Renascimento | Ações simultâneas |
| Atenção Difusa | 5 | 10 | 1 Renascimento | Atenção ao redor (1m por nível) |
| Ação em Cadeia | 10 | 1 | 1 Renasc. + Ataque Adicional | Agir dentro do Ataque Adicional |
| Previsão em Combate | 15 | 3 | 2 Renascimentos | Bônus em Defesa, Ofensiva e Reação |

---

## Fórmulas de Cálculo Completas

### Atributos
```javascript
// Total do atributo
TOTAL = BASE + NIVEL + OUTROS

// Validação de pontos
PONTOS_ESPERADOS = NIVEL_PERSONAGEM × 3
PONTOS_DISTRIBUIDOS = soma de todos os campos .nivel dos atributos
```

### Ímpetos
```javascript
FORCA_IMPETO = TOTAL * 3                      // kg de carga
AGILIDADE_IMPETO = FLOOR(TOTAL / 3)           // metros de deslocamento
VIGOR_IMPETO = FLOOR(TOTAL / 10)              // RD (Redução de Dano)
SABEDORIA_IMPETO = FLOOR(TOTAL / 10)          // RDM (Redução de Dano Mágico)
INTUICAO_IMPETO = MIN(FLOOR(TOTAL / 20), 3)   // Pontos de Sorte (máximo 3)
INTELIGENCIA_IMPETO = FLOOR(TOTAL / 20)       // Comando
ASTUCIA_IMPETO = FLOOR(TOTAL / 10)            // Estratégia
```

### Bônus
```javascript
// Base calculada
BBA_BASE = FLOOR((FORCA_TOTAL + AGILIDADE_TOTAL) / 3)
BLOQUEIO_BASE = FLOOR((FORCA_TOTAL + VIGOR_TOTAL) / 3)
REFLEXO_BASE = FLOOR((AGILIDADE_TOTAL + ASTUCIA_TOTAL) / 3)
BBM_BASE = FLOOR((SABEDORIA_TOTAL + INTELIGENCIA_TOTAL) / 3)
PERCEPCAO_BASE = FLOOR((INTELIGENCIA_TOTAL + INTUICAO_TOTAL) / 3)
RACIOCINIO_BASE = FLOOR((INTELIGENCIA_TOTAL + ASTUCIA_TOTAL) / 3)

// Total com modificadores
BONUS_TOTAL = BASE + VANTAGENS + CLASSE + ITENS + GLORIA + OUTROS
```

### Aptidões
```javascript
APTIDAO_TOTAL = BASE + SORTE + CLASSE
```

### Vida
```javascript
VIDA_TOTAL = VIGOR_TOTAL + NIVEL + VT + RENASCIMENTOS + OUTROS
VIDA_MEMBRO = FLOOR(VIDA_TOTAL × PORCENTAGEM_MEMBRO) - DANOS_RECEBIDOS
```

### Essência
```javascript
ESSENCIA_BASE = FLOOR((VIGOR_TOTAL + SABEDORIA_TOTAL) / 2)
ESSENCIA_TOTAL = ESSENCIA_BASE + NIVEL + RENASCIMENTOS + VANTAGENS + OUTROS
ESSENCIA_RESTANTE = ESSENCIA_TOTAL - GASTOS
```

### Ameaça
```javascript
AMEACA_TOTAL = NIVEL + ITENS + TITULOS + RENASCIMENTOS + OUTROS
```

---

## SQL de Exemplo (V5__seed_default_data.sql)

```sql
-- Inserir atributos padrão para um jogo
INSERT INTO atributo_config (jogo_id, nome, descricao, formula_impeto, descricao_impeto, ordem_exibicao, ativo)
VALUES 
    (:jogo_id, 'Força', 'Potência física e capacidade de carga', 'TOTAL * 3', 'Capacidade de carga em kg', 1, true),
    (:jogo_id, 'Agilidade', 'Velocidade e coordenação motora', 'FLOOR(TOTAL / 3)', 'Deslocamento em metros', 2, true),
    (:jogo_id, 'Vigor', 'Resistência física e vitalidade', 'FLOOR(TOTAL / 10)', 'Redução de Dano (RD)', 3, true),
    (:jogo_id, 'Sabedoria', 'Conhecimento arcano e resistência mental', 'FLOOR(TOTAL / 10)', 'Redução de Dano Mágico (RDM)', 4, true),
    (:jogo_id, 'Intuição', 'Percepção e sexto sentido', 'MIN(FLOOR(TOTAL / 20), 3)', 'Pontos de Sorte', 5, true),
    (:jogo_id, 'Inteligência', 'Raciocínio lógico e capacidade de comando', 'FLOOR(TOTAL / 20)', 'Comando', 6, true),
    (:jogo_id, 'Astúcia', 'Esperteza e pensamento estratégico', 'FLOOR(TOTAL / 10)', 'Estratégia', 7, true);

-- Inserir níveis padrão (exemplo dos primeiros 5)
INSERT INTO nivel_config (jogo_id, numero_nivel, experiencia_necessaria, limitador_atributo, pontos_atributo, descricao, permite_renascimento)
VALUES 
    (:jogo_id, 0, 0, 10, 0, 'Iniciante', false),
    (:jogo_id, 1, 1000, 10, 3, 'Novato', false),
    (:jogo_id, 2, 3000, 50, 3, 'Aprendiz', false),
    (:jogo_id, 3, 6000, 50, 3, 'Aprendiz', false),
    (:jogo_id, 4, 10000, 50, 3, 'Aprendiz', false);
    -- ... continuar para todos os 36 níveis

-- Inserir dados de prospecção
INSERT INTO dado_prospeccao_config (jogo_id, nome, valor_maximo, ordem_exibicao, ativo)
VALUES 
    (:jogo_id, 'd3', 3, 1, true),
    (:jogo_id, 'd4', 4, 2, true),
    (:jogo_id, 'd6', 6, 3, true),
    (:jogo_id, 'd8', 8, 4, true),
    (:jogo_id, 'd10', 10, 5, true),
    (:jogo_id, 'd12', 12, 6, true);
```

---

## Referências

- Arquivo legado: `docs/requisitos_legado/RESUMO_EXECUTIVO.md`
- Arquivo legado: `docs/requisitos_legado/DIAGRAMAS_CASOS_USO.md`
- Código legado: `docs/requisitos_legado/codigo legado/klayrah-rpg/components/CharacterSheet.tsx`
