# Índice de Tasks — Spec 010 (Galeria de Imagens e Anotações)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-image-storage-service.md) | Storage | ImageStorageService interface + Local + S3 stub | 🟡 |
| [P2-T1](./P2-T1-ficha-imagem.md) | Galeria | FichaImagem entity + service + controller + upload | 🟡 |
| [P3-T1](./P3-T1-ficha-anotacao.md) | Anotações | FichaAnotacao entity + enum Visibilidade + CRUD + regras | 🟡 |
| [P4-T1](./P4-T1-anotacao-mestre.md) | Anotações Mestre | AnotacaoMestre entity + CRUD restrito a Mestre | 🟢 |
| [P5-T1](./P5-T1-testes-galeria.md) | Testes | Testes galeria de imagens (mock storage) | 🟡 |
| [P5-T2](./P5-T2-testes-anotacoes.md) | Testes | Testes anotações (visibilidade PUBLICA vs PRIVADA) | 🟡 |

**Total**: 6 tasks, ~4-5 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
