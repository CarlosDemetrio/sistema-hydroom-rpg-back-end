---
name: PO Decisions 2026-04-03
description: All GAPs and pending decisions resolved by PO on 2026-04-03, including INCONS-02 (fichas never deleted) and admin scope
type: project
---

On 2026-04-03, the PO resolved ALL pending decisions. Key non-obvious decisions:

1. **INCONS-02**: Fichas are NEVER physically deleted. They can be marked "morta" or "abandonada" via a status field. Backend should return 405 on DELETE /fichas. This is a fundamental domain rule.

2. **P-03**: ADMIN role in MVP is ONLY for user management. No bypass of canAccessJogo(). This simplifies Spec 010 significantly.

3. **GAP-04**: REJEITADO participants can re-apply without cooldown. BANIDO is reversible (Mestre can unban). DELETE on participant = "remover provisorio" (1 endpoint). This defines the complete state machine for Spec 005.

4. **Renascimento**: Completely OUT of MVP. Spec 012 T12/T13 removed. Level 31+ mechanics are post-MVP.

5. **PA-001/PA-002**: Mestre can revoke ANY vantagem (including Insolitus). TipoVantagem is an enum (VANTAGEM | INSOLITUS), not a boolean.

**Why:** These decisions unblocked 33 Sprint 2 tasks and 42 backlog tasks that were waiting on PO clarification.

**How to apply:** When generating agent prompts or reviewing task specs, always reference these decisions. Especially INCONS-02 (no delete) and P-03 (admin scope) as they affect multiple specs.
