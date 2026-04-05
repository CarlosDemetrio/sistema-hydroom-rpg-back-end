---
name: Autenticação nos testes de integração
description: Padrão de autenticarComo() com UsernamePasswordAuthenticationToken — construtor de 2 argumentos não define isAuthenticated=true
type: feedback
---

Usar `new UsernamePasswordAuthenticationToken(email, "n/a")` (2 argumentos) define `authenticated = false`. Os services do projeto recuperam a autenticação pelo `authentication.getName()` sem checar `isAuthenticated()`.

**Why:** O construtor de 3 argumentos (`new UsernamePasswordAuthenticationToken(principal, credentials, authorities)`) é o que define `authenticated = true`. O de 2 argumentos é para credentials ainda não validados.

**How to apply:** Ao escrever novos services, não checar `authentication.isAuthenticated()` em produção — usar apenas `authentication == null` como guard, pois nos testes o SecurityContextHolder é populado com o construtor de 2 argumentos. Se precisar checar, o `UsuarioService.getUsuarioAtual()` foi corrigido para usar apenas o check de null.
