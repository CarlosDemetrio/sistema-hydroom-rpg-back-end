---
name: Padrões ProspeccaoService e testes de visibilidade NPC
description: Armadilhas e padrões na implementação de ProspeccaoService e testes de FichaVisibilidadeService
type: feedback
---

Ao testar `temAcessoUsuarioAtual()` do FichaVisibilidadeService em testes `@Transactional`, o método pode retornar false mesmo após salvar a FichaVisibilidade. Isso ocorre porque o método usa o SecurityContext para buscar o usuário via e-mail e depois checa existsByFichaIdAndJogadorId — o problema é timing/contexto do L1 cache combinado com troca de autenticação.

**Why:** O método `temAcessoUsuarioAtual` foi projetado para o contexto web (enriquecimento de FichaResponse na listagem), não para testes unitários. Em testes, o SecurityContext é manipulado manualmente.

**How to apply:** Em testes de integração que verificam visibilidade, usar `fichaVisibilidadeService.temAcesso(fichaId, jogadorId)` (recebe IDs explícitos) em vez de `temAcessoUsuarioAtual()`. Ou verificar diretamente no repositório via `fichaVisibilidadeRepository.existsByFichaIdAndJogadorId()`.

---

O `ProspeccaoService` não estende `AbstractConfiguracaoService` — é um service standalone que gerencia estado transacional (não é CRUD de configuração). Usa o padrão:
- `@Transactional(readOnly = true)` na classe
- `@Transactional` nos métodos de escrita (usar, reverter, confirmar, conceder)
- Verificação de permissão inline (isMestre vs jogadorId da ficha)

A entidade `ProspeccaoUso` estende `BaseEntity` mas o histórico nunca é soft-deletado — deleted_at permanece null. O desfecho é registrado via `status` (PENDENTE/CONFIRMADO/REVERTIDO).

O `FichaResumoResponse` foi estendido com `vidaAtual` e `essenciaAtual` na Spec-009 T5. Estes campos são a fonte autoritativa para o frontend (não usar GET /fichas/{id} básico para combate).
