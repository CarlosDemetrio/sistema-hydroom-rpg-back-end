# Infraestrutura OCI — DESCONTINUADO

> ⚠️ **DESCONTINUADO** — O deploy na OCI foi abandonado por falta de maquinas
> disponiveis no free tier (ARM Ampere A1).
>
> O deploy atual usa **GCP Free Tier**:
> - Backend: Cloud Run (serverless) → `infra/gcp/`
> - Frontend: Firebase Hosting → Spec 019
> - Banco: VM e2-micro com PostgreSQL → `infra/gcp/`
>
> Estes scripts sao mantidos como referencia historica.

## Arquivos

| Arquivo | Descricao |
|---------|-----------|
| `setup-oci.sh` | Setup completo da VM OCI (14 etapas, hardened) |
| `rollback.sh` | Script de rollback de imagem Docker |
| `.env.example` | Template de variaveis de ambiente OCI |
