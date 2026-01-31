# 🔍 Checklist de Revisão de Segurança

Sempre que eu pedir para revisar um código, verifique:
1. **Input Validation:** Existe validação para tamanho, tipo e formato em todos os campos?
2. **Authentication:** O endpoint está protegido? Exige o escopo correto?
3. **Logging:** Dados sensíveis (senhas, cartões, tokens) estão sendo logados? (Não devem!).
4. **SQLi:** Existe algum uso de `entityManager.createNativeQuery` com strings dinâmicas?
5. **Rate Limiting:** O endpoint possui proteção contra força bruta ou spam?
6. **Dependency Check:** As bibliotecas sugeridas possuem vulnerabilidades conhecidas (CVEs)?
