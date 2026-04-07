# Infraestrutura — Ficha Controlador

Scripts e configuracoes de deploy organizados por provedor de cloud.

## Estrutura

```
infra/
  gcp/                    ← ATIVO — Deploy no GCP Free Tier
    README.md             — Documentacao
    setup-db-vm.sh        — Setup da VM e2-micro (PostgreSQL)
    docker-compose-db.yml — Compose apenas PostgreSQL
    .env.example          — Template de variaveis
  oci/                    ← DESCONTINUADO — Mantido como referencia
    README.md             — Documentacao (descontinuado)
    setup-oci.sh          — Setup da VM OCI (14 etapas)
    rollback.sh           — Script de rollback Docker
    .env.example          — Template de variaveis OCI
```

## Arquitetura Atual (GCP)

```
Cloud Run (serverless)     → Backend Spring Boot (Native Image)
VM e2-micro (1 vCPU, 1GB) → PostgreSQL 16 (apenas banco)
Firebase Hosting (CDN)     → Frontend Angular (arquivos estaticos)
```

## Specs Relacionadas

- **Spec 018** — Deploy Backend: `docs/specs/018-deploy-backend-gcp/`
- **Spec 019** — Deploy Frontend: `docs/specs/019-deploy-frontend-firebase/`
