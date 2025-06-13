# 🏗️ Terraform Infrastructure as Code - Ecommerce Microservices

Esta implementación de **Infraestructura como Código (IaC)** con Terraform gestiona completamente la infraestructura de la aplicación de microservicios de ecommerce.

## 📋 Índice

- [Arquitectura](#-arquitectura)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Prerrequisitos](#-prerrequisitos)
- [Configuración](#-configuración)
- [Despliegue](#-despliegue)
- [Ambientes](#-ambientes)
- [Módulos](#-módulos)
- [Backend Remoto](#-backend-remoto)
- [Operaciones](#-operaciones)

## 🏗 Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    KUBERNETES CLUSTER                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │   NAMESPACE     │  │   MONITORING    │                  │
│  │ ecommerce-{env} │  │     STACK       │                  │
│  └─────────────────┘  └─────────────────┘                  │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │  MICROSERVICES  │  │    NETWORKING   │                  │
│  │   (10 SERVICES) │  │   & SECURITY    │                  │
│  └─────────────────┘  └─────────────────┘                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Estructura del Proyecto

```
terraform/
├── 📁 environments/          # Configuraciones por ambiente
│   ├── 📁 dev/              # Desarrollo
│   ├── 📁 stage/            # Staging
│   └── 📁 prod/             # Producción
├── 📁 modules/              # Módulos reutilizables
│   ├── 📁 kubernetes/       # Namespace, RBAC, ConfigMaps
│   ├── 📁 microservices/    # Deployments y Services
│   ├── 📁 monitoring/       # Stack de observabilidad
│   └── 📁 networking/       # Ingress y networking
├── 📁 backend/              # Configuración backend remoto
├── 📄 main.tf              # Configuración principal
├── 📄 variables.tf         # Variables globales
├── 📄 outputs.tf           # Outputs principales
├── 📄 deploy.sh            # Script de despliegue
└── 📄 README.md            # Esta documentación
```

## 🔧 Prerrequisitos

### Software Requerido
- **Terraform** >= 1.0
- **kubectl** 
- **Docker** (para validación de imágenes)
- **Kubernetes Cluster** (minikube, EKS, AKS, GKE)

### Verificación de Prerrequisitos
```bash
# Verificar versiones
terraform version
kubectl version --client
docker --version

# Verificar conectividad a Kubernetes
kubectl cluster-info
kubectl get nodes
```

## ⚙️ Configuración

### 1. Configurar Backend Remoto (Opcional pero Recomendado)

```bash
cd terraform/backend
terraform init
terraform plan
terraform apply
```

### 2. Configurar Credenciales Docker (Si es necesario)

```bash
export TF_VAR_docker_username="tu-usuario"
export TF_VAR_docker_password="tu-password"
```

### 3. Configurar Variables por Ambiente

Edita los archivos `terraform/environments/{env}/terraform.tfvars` según tus necesidades:

```hcl
# Ejemplo para desarrollo
environment     = "dev"
docker_registry = "collinx0108"
image_tag      = "dev"
replica_count = {
  "api-gateway" = 1
  # ... otros servicios
}
```

## 🚀 Despliegue

### Uso del Script Automatizado

```bash
# Hacer ejecutable el script
chmod +x terraform/deploy.sh

# Ver plan para desarrollo
./terraform/deploy.sh dev plan

# Aplicar cambios a desarrollo
./terraform/deploy.sh dev apply

# Ver outputs
./terraform/deploy.sh dev output

# Destruir infraestructura (¡CUIDADO!)
./terraform/deploy.sh dev destroy
```

### Despliegue Manual

```bash
cd terraform/environments/dev

# Inicializar
terraform init

# Planificar
terraform plan -var-file="terraform.tfvars"

# Aplicar
terraform apply -var-file="terraform.tfvars"

# Ver outputs
terraform output
```

## 🌍 Ambientes

### Development (dev)
- **Propósito**: Desarrollo y pruebas iniciales
- **Recursos**: Mínimos (1 replica por servicio)
- **Características**:
  - Logging deshabilitado (ahorro de recursos)
  - Monitoring básico habilitado
  - H2 in-memory database

### Staging (stage)
- **Propósito**: Pruebas de integración y E2E
- **Recursos**: Medios (2 replicas servicios críticos)
- **Características**:
  - Todas las características habilitadas
  - Replica del entorno productivo
  - Pruebas automatizadas

### Production (prod)
- **Propósito**: Entorno productivo
- **Recursos**: Completos (3+ replicas, HA)
- **Características**:
  - Alta disponibilidad
  - Monitoreo completo
  - Seguridad reforzada
  - Backup automático

## 🧩 Módulos

### 1. Kubernetes Base (`modules/kubernetes/`)
- **Responsabilidad**: Namespace, RBAC, ConfigMaps, Secrets
- **Recursos**:
  - `kubernetes_namespace`
  - `kubernetes_config_map`
  - `kubernetes_secret`
  - `kubernetes_service_account`
  - `kubernetes_role` & `kubernetes_role_binding`

### 2. Microservices (`modules/microservices/`)
- **Responsabilidad**: Deployments y Services de aplicación
- **Recursos**:
  - `kubernetes_deployment` (x10 servicios)
  - `kubernetes_service` (x10 servicios)
  - `kubernetes_horizontal_pod_autoscaler`

### 3. Monitoring (`modules/monitoring/`)
- **Responsabilidad**: Observabilidad y monitoreo
- **Recursos**:
  - Prometheus
  - Grafana
  - Zipkin
  - ELK Stack (opcional)

### 4. Networking (`modules/networking/`)
- **Responsabilidad**: Ingress y configuración de red
- **Recursos**:
  - `kubernetes_ingress_v1`
  - Network Policies
  - Service Mesh config

## 🗄️ Backend Remoto

### Configuración S3 (AWS)
```hcl
terraform {
  backend "s3" {
    bucket         = "ecommerce-terraform-state-dev-abc123"
    key            = "ecommerce/dev/terraform.tfstate"
    region         = "us-west-2"
    dynamodb_table = "terraform-locks-dev"
    encrypt        = true
  }
}
```

### Beneficios del Backend Remoto
- **🔒 Seguridad**: Estado encriptado
- **🤝 Colaboración**: Estado compartido
- **🔐 Bloqueo**: Previene modificaciones concurrentes
- **📈 Versionado**: Historial de cambios
- **💾 Backup**: Recuperación automática

## 🛠 Operaciones

### Comandos Útiles

```bash
# Verificar estado actual
terraform show

# Importar recurso existente
terraform import kubernetes_namespace.example default

# Actualizar estado
terraform refresh

# Ver plan de destrucción
terraform plan -destroy

# Forzar recreación de recurso
terraform taint kubernetes_deployment.api_gateway

# Validar sintaxis
terraform validate

# Formatear código
terraform fmt -recursive
```

### Troubleshooting

#### Error: Cluster no accesible
```bash
# Verificar contexto de kubectl
kubectl config current-context
kubectl config get-contexts

# Cambiar contexto si es necesario
kubectl config use-context minikube
```

#### Error: Imágenes no encontradas
```bash
# Verificar que las imágenes existen
docker pull collinx0108/api-gateway:dev

# Construir imágenes localmente si es necesario
cd ../
docker build -t collinx0108/api-gateway:dev ./api-gateway/
```

#### Error: Recursos insuficientes
```bash
# Verificar recursos disponibles
kubectl top nodes
kubectl describe nodes

# Ajustar resource limits en terraform.tfvars
```

### Monitoreo Post-Despliegue

```bash
# Verificar pods
kubectl get pods -n ecommerce-dev

# Ver logs
kubectl logs -f deployment/api-gateway -n ecommerce-dev

# Verificar servicios
kubectl get services -n ecommerce-dev

# Ver endpoints
kubectl get endpoints -n ecommerce-dev

# Port-forward para testing
kubectl port-forward service/api-gateway 8080:8080 -n ecommerce-dev
```

## 📊 Outputs Importantes

Después del despliegue, Terraform mostrará:

```yaml
api_gateway_url: "http://api-gateway.ecommerce-dev.svc.cluster.local:8080"
namespace_name: "ecommerce-dev"
service_discovery_url: "http://service-discovery.ecommerce-dev.svc.cluster.local:8761"
health_check_urls: {
  "api-gateway": "http://api-gateway.ecommerce-dev.svc.cluster.local:8080/actuator/health"
  # ... otros servicios
}
```

## 🎯 Próximos Pasos

Una vez desplegada la infraestructura base:

1. **🔍 Configurar Monitoring**: Prometheus + Grafana
2. **📋 Implementar Logging**: ELK Stack
3. **🛡️ Reforzar Seguridad**: Network Policies, TLS
4. **📈 Configurar HPA**: Autoscaling automático
5. **🌐 Setup Ingress**: Exposición externa segura

---

**¡Infraestructura lista!** 🎉 Tu arquitectura de microservicios ahora es completamente gestionada como código con Terraform. 