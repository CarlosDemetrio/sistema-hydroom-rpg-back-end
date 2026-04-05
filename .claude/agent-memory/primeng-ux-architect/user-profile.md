---
name: Perfil do usuário e preferências de colaboração
description: Carlos — tech lead do projeto Klayrah RPG, Angular 21 + Spring Boot 4
type: user
---

## Perfil técnico

- Tech lead e desenvolvedor full-stack do projeto Klayrah RPG
- Forte conhecimento em Spring Boot (backend proprietário) e Angular (frontend)
- Projeto pessoal: sistema de fichas de RPG de mesa totalmente configurável
- Atua tanto como Mestre (admin do sistema) quanto como usuário final

## Stack do projeto

**Backend**: Java 25, Spring Boot 4.0.2, PostgreSQL, MapStruct, exp4j, OAuth2 Google
**Frontend**: Angular 21, PrimeNG 18+ (Aura Styled), PrimeFlex/Tailwind, NgRx Signals, Vitest

## Padrões de codificação Angular que o time segue

- Standalone components sempre
- `input()` / `output()` / `signal()` / `computed()` — nova sintaxe (nunca @Input/@Output)
- `inject()` para DI — nunca constructor injection
- Control Flow: `@if` / `@for` — nunca *ngIf/*ngFor
- PrimeNG modo Styled com tema Aura (nunca Lara, nunca Unstyled)
- PrimeFlex utility classes para layout — sem CSS customizado em componentes
- NgRx Signals para state management global (`FichasStore`, `JogosStore`, `ConfigStore`)

## Estrutura esperada dos design specs

Baseado no trabalho realizado em 2026-04-01, o usuário espera specs com:
1. Wireframe textual ASCII mostrando layout desktop e mobile
2. Listagem explícita de componentes PrimeNG com módulos de import
3. Props TypeScript na nova sintaxe (`input.required<T>()`, `output<T>()`)
4. Tratamento dos 3 estados: loading (skeleton), empty, erro
5. Checklists de implementação ao final
6. Identificação de limitações/dívidas técnicas de API
