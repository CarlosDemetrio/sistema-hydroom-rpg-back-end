# docs/ — Mapa de Navegacao

> Indice pratico da documentacao do ficha-controlador.
> Ultima atualizacao: 2026-04-07 (reorganizacao completa dos docs de tracking)

---

## Leia primeiro

Para retomar trabalho rapidamente:

1. **`HANDOFF-SESSAO.md`** — estado atual da sessao (sempre o mais recente, reescrito a cada sessao)
2. **`MASTER.md`** — indice mestre: tabela de specs + status geral + sequenciamento
3. **`SPRINT-ATUAL.md`** — tracking detalhado da sprint corrente
4. **`PM.md`** — status detalhado por area (backend/frontend/testes) + historico de sprints

---

## Estrutura

```
docs/
├── README.md                  <- este arquivo
├── HANDOFF-SESSAO.md          <- estado da ultima sessao (ativo)
├── MASTER.md                  <- indice mestre de specs + status geral (ativo)
├── SPRINT-ATUAL.md            <- sprint corrente em andamento (ativo)
├── PM.md                      <- board detalhado do PM (ativo)
├── GLOSSARIO.md               <- resumo do glossario Klayrah (ler antes de tocar em dominio)
├── API-CONTRACT.md            <- contrato REST publicado
├── AI_GUIDELINES_BACKEND.md   <- guidelines para agentes backend
│
├── specs/                     <- FONTE DA VERDADE para implementacao
│   ├── 004-configuracoes-siglas-formulas/
│   ├── 005-participantes/
│   ├── 006-ficha-wizard/
│   ├── 007-vantagem-efeito/
│   ├── 008-sub-recursos-classes-racas/
│   ├── 009-npc-visibility/
│   ├── 010-roles-refactor/    (stand-by pos-homologacao)
│   ├── 011-galeria-anotacoes/ (stand-by pos-homologacao)
│   ├── 012-niveis-progressao-frontend/
│   ├── 013-documentacao-tecnica/ (stand-by pos-homologacao)
│   ├── 014-cobertura-testes/     (stand-by pos-homologacao)
│   ├── 015-config-pontos-classe-raca/
│   ├── 016-sistema-itens/     (stand-by pos-homologacao)
│   ├── 017-correcoes-rc/      <- NOVA (auditoria 2026-04-07)
│   └── ROADMAP-MVP.md
│
├── tracking/                  <- historico vivo de execucao
│   ├── rodadas/
│   │   ├── RODADA-10.md
│   │   ├── RODADA-11.md
│   │   ├── RODADA-12.md
│   │   ├── RODADA-13.md
│   │   └── RODADA-14.md       <- em andamento (sessao auditoria + Spec 017)
│   └── sprints/               <- vazio por enquanto (sprints encerradas ainda estao em PM.md)
│
├── auditoria/                 <- auditorias tecnicas pontuais
│   ├── AUDITORIA-ROTAS-ERROS-2026-04-07.md
│   └── AUDITORIA-UX-UI-2026-04-07.md
│
├── analises/                  <- analises BA por tema
├── backend/                   <- padroes e guidelines de backend
│   ├── 01-architecture.md
│   ├── 02-entities-dtos.md
│   ├── ...
│   └── 11-owasp-security.md
├── design/                    <- design system, tokens, mockups
├── gaps/                      <- dossies de gaps identificados pelo BA
├── glossario/                 <- glossario modular do dominio Klayrah
│   ├── 01-contexto-geral.md
│   ├── 02-configuracoes-jogo.md
│   ├── 03-termos-dominio.md
│   ├── 04-siglas-formulas.md
│   └── 05-termos-tecnicos-fluxo.md
├── testes/                    <- documentacao de estrategia de testes
│
├── deploy/                    <- docs de deployment
│   ├── DEPLOY-BACKEND.md
│   └── DEPLOY-OCI.md
│
└── historico/                 <- arquivos historicos (nao mexer)
    ├── CRONOLOGIA.md          <- cronologia reversa do projeto
    ├── arquivado/             <- docs absorvidos/desatualizados preservados
    │   ├── INDEX.md           (absorvido pelo MASTER.md)
    │   ├── TEAM-PLAN.md       (absorvido pelo MASTER.md)
    │   ├── PROXIMA-SESSAO.md  (desatualizado desde 2026-04-05, substituido pelo HANDOFF)
    │   └── RODADA-14-TRACKING-abandonada-2026-04-06.md
    └── backlogs-iniciais/     <- backlogs da fase de descoberta (Specs tomaram o lugar)
        ├── EPICS-BACKLOG.md
        ├── PRODUCT-BACKLOG.md
        └── UX-BACKLOG.md
```

---

## Papel de cada arquivo ativo

| Arquivo | Papel | Quem atualiza | Frequencia |
|---------|-------|---------------|-----------|
| `HANDOFF-SESSAO.md` | Estado da ultima sessao encerrada + plano da proxima | PM | Toda sessao |
| `MASTER.md` | Indice mestre: tabela de specs, status geral, sequenciamento, decisoes PO | PM | Toda rodada |
| `SPRINT-ATUAL.md` | Tracking detalhado da sprint corrente | PM | Toda rodada |
| `PM.md` | Status detalhado por area + historico de sprints encerradas | PM | Toda rodada |
| `tracking/rodadas/RODADA-N.md` | Execucao de uma rodada (tasks, commits, testes) | Agentes + PM | Durante a rodada |

**Regra de ouro:** `HANDOFF-SESSAO.md` e sempre o ponto de partida. Os outros
complementam com detalhe. Se houver conflito entre arquivos, `HANDOFF-SESSAO.md`
e a fonte mais recente — os outros devem ser atualizados para refleti-lo.

---

## Papel dos diretorios imutaveis

- **`specs/`** — FONTE DA VERDADE para implementacao. Cada spec tem `spec.md`,
  `plan.md`, `tasks/INDEX.md` e tasks individuais `Px-Ty-nome.md`. Agentes
  leem daqui quando vao implementar.
- **`backend/`** — padroes de arquitetura e convencoes do backend. Ler antes
  de abrir qualquer PR backend.
- **`glossario/`** — dominio Klayrah. Ler antes de tocar em qualquer regra
  de negocio.
- **`gaps/`** — dossies de gaps identificados pelo BA (decisoes do PO ja
  tomadas — ver `BA-GAPS-2026-04-02.md`).

---

## O que NAO tocar

- `specs/` — so o PM altera o estado das tasks via tracking files
- `backend/`, `glossario/` — documentacao de referencia estavel
- `historico/` — preservado para auditoria
- `gaps/`, `analises/`, `auditoria/` — entregaveis de BA/tech lead, imutaveis
  apos entrega
