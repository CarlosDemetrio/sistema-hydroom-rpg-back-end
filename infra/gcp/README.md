# Infraestrutura GCP — Ficha Controlador

> Diretorio com scripts e configuracoes para deploy no GCP Free Tier.
> Para o plano OCI (descontinuado), veja `infra/oci/`.

## Arquitetura

```
Internet
   │
   ▼
Cloud Run (serverless)         ← Backend Spring Boot (Native Image ou JVM)
   │  Direct VPC Egress
   │  10.128.0.0/20 (rede interna)
   ▼
VM e2-micro (1 vCPU, 1GB)     ← PostgreSQL 16 via Docker
   │  porta 5432 INVISIVEL para internet
   │  acesso APENAS via VPC interna

Firebase Hosting (CDN)         ← Frontend Angular (Spec 019)
```

> ⚠️ **PostgreSQL NUNCA exposto para a internet.** O acesso ao banco
> ocorre exclusivamente via VPC interna entre o Cloud Run e a VM.
> O firewall GCP bloqueia todo tráfego externo na porta 5432.

## Arquivos

| Arquivo | Descricao |
|---------|-----------|
| `setup-db-vm.sh` | Setup completo da VM e2-micro (swap, Docker, PostgreSQL, backup, hardening) |
| `docker-compose-db.yml` | Docker Compose apenas com PostgreSQL |
| `.env.example` | Template de variaveis de ambiente para a VM |
| `backup-postgres.sh` | Script de backup diario com retencao 7 dias |
| `cloud-run-deploy.sh` | Deploy automatizado do backend no Cloud Run |
| `setup-secrets.sh` | Criacao de segredos no GCP Secret Manager |
| `setup-artifact-registry.sh` | Criacao do repositorio Docker no Artifact Registry + permissoes |

---

## Provisionamento GCP (executar uma vez)

### 1. Criar IP estatico para a VM

```bash
gcloud compute addresses create postgres-vm-ip \
  --region=us-central1 \
  --description="IP estatico da VM PostgreSQL"

# Verificar o IP alocado
gcloud compute addresses describe postgres-vm-ip --region=us-central1
```

### 2. Criar a VM e2-micro

```bash
gcloud compute instances create postgres-db \
  --project=<PROJECT_ID> \
  --zone=us-central1-a \
  --machine-type=e2-micro \
  --image-family=ubuntu-2404-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=20GB \
  --boot-disk-type=pd-standard \
  --tags=postgres-server \
  --address=postgres-vm-ip \
  --no-address \
  --description="VM PostgreSQL dedicado — rpg-fichas"
```

> `--no-address` remove o IP publico efemero. O IP estatico acima
> deve ser promovido a IP interno ou use `--private-network-ip`.
> Ajuste conforme a topologia de rede do seu projeto.

### 3. Criar repositório no Artifact Registry

```bash
# Executar o script (substitua SA_EMAIL pelo email da Service Account do GitHub Actions)
cd infra/gcp
./setup-artifact-registry.sh SA_EMAIL@PROJECT_ID.iam.gserviceaccount.com

# Ou manualmente:
gcloud services enable artifactregistry.googleapis.com
gcloud artifacts repositories create ficha-controlador \
  --repository-format=docker \
  --location=us-east1 \
  --description="Docker images do backend ficha-controlador"

# Conceder permissão de push à Service Account
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member="serviceAccount:SA_EMAIL" \
  --role="roles/artifactregistry.writer"
```

### 4. Regras de Firewall

```bash
# PostgreSQL — APENAS rede interna VPC (Cloud Run via Direct VPC Egress)
gcloud compute firewall-rules create allow-postgres-vpc \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:5432 \
  --target-tags=postgres-server \
  --source-ranges=10.128.0.0/20 \
  --description="PostgreSQL - APENAS rede interna VPC (Cloud Run via Direct VPC Egress)"

# SSH — APENAS IP do desenvolvedor (substituir SEU_IP pelo IP real)
gcloud compute firewall-rules create allow-ssh-admin \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:22 \
  --target-tags=postgres-server \
  --source-ranges=SEU_IP/32 \
  --description="SSH - APENAS IP do desenvolvedor"

# Bloquear explicitamente qualquer acesso externo ao 5432
gcloud compute firewall-rules create deny-postgres-external \
  --direction=INGRESS \
  --action=DENY \
  --rules=tcp:5432 \
  --target-tags=postgres-server \
  --priority=900 \
  --description="Bloqueia PostgreSQL de qualquer origem externa"
```

---

## Configurando a VM

```bash
# 1. Copiar scripts para a VM
scp setup-db-vm.sh backup-postgres.sh .env.example docker-compose-db.yml \
    usuario@<VM_IP>:~/

# 2. SSH na VM e executar setup (como root)
ssh usuario@<VM_IP>
chmod +x setup-db-vm.sh
sudo ./setup-db-vm.sh

# 3. Editar .env com valores reais (OBRIGATORIO antes de subir o banco)
sudo -u deploy nano /opt/db/.env

# 4. Subir PostgreSQL
cd /opt/db && sudo -u deploy docker compose up -d

# 5. Verificar saude do banco
docker exec rpg-postgres pg_isready -U rpg_prod_user
```

---

## Seguranca — Notas Importantes

| Camada | Controle |
|--------|----------|
| Firewall GCP | Porta 5432 permitida APENAS de `10.128.0.0/20` (VPC interna) |
| UFW na VM | Segunda barreira: 5432 restrito ao mesmo range VPC |
| SSH | Root login desabilitado, autenticacao somente por chave |
| Fail2Ban | Bloqueia IPs apos 3 tentativas SSH falhas por 2 horas |
| Docker | `no-new-privileges`, memoria limitada a 384MB |
| Swap | 2GB (protecao contra OOM Killer na VM de 1GB RAM) |

> O PostgreSQL escuta em `0.0.0.0:5432` dentro da VM, mas isso e
> **seguro** porque o firewall GCP bloqueia a porta antes de chegar
> na interface de rede. O UFW adiciona uma segunda camada de defesa.

---

## Specs Relacionadas

- **Spec 018** — Deploy Backend GCP (Cloud Run + Native): `docs/specs/018-deploy-backend-gcp/`
- **Spec 019** — Deploy Frontend Firebase: `docs/specs/019-deploy-frontend-firebase/`
