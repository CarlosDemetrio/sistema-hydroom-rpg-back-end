# Infraestrutura GCP — Ficha Controlador

> Diretorio com scripts e configuracoes para deploy no GCP Free Tier.
> Para o plano OCI (descontinuado), veja `infra/oci/`.

## Arquitetura

```
Cloud Run (serverless)     → Backend Spring Boot (Native Image ou JVM)
VM e2-micro (1 vCPU, 1GB) → PostgreSQL 16 (apenas banco de dados)
Firebase Hosting (CDN)     → Frontend Angular (Spec 019)
```

## Arquivos

| Arquivo | Descricao |
|---------|-----------|
| `setup-db-vm.sh` | Setup completo da VM e2-micro (swap, Docker, PostgreSQL, backup, hardening) |
| `docker-compose-db.yml` | Docker Compose apenas com PostgreSQL |
| `.env.example` | Template de variaveis de ambiente para a VM |

## Uso

```bash
# 1. Criar VM e2-micro no GCP Console ou via gcloud CLI
# 2. Copiar scripts para a VM
scp setup-db-vm.sh .env.example docker-compose-db.yml usuario@<VM_IP>:~/

# 3. SSH na VM e executar setup
ssh usuario@<VM_IP>
chmod +x setup-db-vm.sh
sudo ./setup-db-vm.sh

# 4. Editar .env com valores reais
sudo -u deploy nano /opt/db/.env

# 5. Subir PostgreSQL
cd /opt/db && docker compose up -d
```

## Specs Relacionadas

- **Spec 018** — Deploy Backend GCP (Cloud Run + Native): `docs/specs/018-deploy-backend-gcp/`
- **Spec 019** — Deploy Frontend Firebase: `docs/specs/019-deploy-frontend-firebase/`
