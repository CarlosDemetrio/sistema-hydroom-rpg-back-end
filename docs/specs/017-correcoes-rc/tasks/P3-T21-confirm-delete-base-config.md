# T21 — Frontend: Auditar `confirmDelete` sem dialog em BaseConfigComponent

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

`base-config.component.ts:165-169` tem implementacao padrao de `confirmDelete()` que **deleta diretamente sem dialog**. Apenas subclasses que sobrescrevem (como `racas-config`, `atributos-config`) tem confirmacao. Configs simples (generos, indoles, presencas, membros-corpo, tipos-aptidao) podem estar deletando sem perguntar.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `features/mestre/pages/config/base-config.component.ts:165-169` | Adicionar confirmacao padrao via `ConfirmationService` |
| Subclasses que ja sobrescrevem | Nenhuma — `super.confirmDelete()` ja e suficiente |

---

## Passos

1. Listar todas as subclasses de `BaseConfigComponent`:
   ```
   grep -rn "extends BaseConfigComponent" src/app --include="*.ts"
   ```
2. Para cada uma, verificar se sobrescreve `confirmDelete`
3. Para as que NAO sobrescrevem, confirmar que estao deletando diretamente
4. Refatorar `BaseConfigComponent.confirmDelete()` para SEMPRE mostrar dialog:
   ```typescript
   confirmDelete(item: T): void {
     this.confirmationService.confirm({
       message: `Deseja excluir "${item.nome}"?`,
       header: 'Confirmar exclusao',
       icon: 'pi pi-exclamation-triangle',
       accept: () => this.delete(item.id)
     });
   }
   ```
5. Rodar testes — algumas subclasses podem ter mock de `confirmDelete` que precisa atualizar

---

## Criterios de Aceite

- [ ] `BaseConfigComponent.confirmDelete()` sempre mostra dialog
- [ ] Todas as configs simples (generos, indoles, presencas, membros-corpo, tipos-aptidao) pedem confirmacao ao deletar
- [ ] Testes passam
- [ ] Validacao manual em pelo menos 2 configs simples

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-06 (linha 257-262)
