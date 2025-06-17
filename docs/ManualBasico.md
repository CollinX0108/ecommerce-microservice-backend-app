# Manual de Operaciones - E-commerce Microservices

## 1. Requisitos Previos

### 1.1 Herramientas Necesarias
- Jenkins
- Azure CLI
- kubectl
- Git

### 1.2 Configuración de Azure
```bash
# Login en Azure
az login

# Configurar suscripción
az account set --subscription "nombre-suscripcion"

# Configurar AKS
az aks get-credentials --resource-group nombre-grupo --name aks-develop-ecommerce
az aks get-credentials --resource-group nombre-grupo --name aks-master-ecommerce
```

### 1.3 Configuración de Jenkins
- Instalar plugins necesarios:
  - Pipeline
  - Git
  - Docker
  - Kubernetes
  - Maven
- Configurar credenciales:
  - Docker Hub (docker_hub_pwd)
  - Azure
  - GitHub

## 2. Estructura del Pipeline

### 2.1 Jenkinsfile
El pipeline está configurado para manejar tres ambientes:
- develop → dev
- stage → stage
- master → prod

### 2.2 Etapas del Pipeline
1. **Init**
   - Configuración de variables por ambiente
   - Definición de tags y sufijos

2. **Build**
   - Compilación de servicios
   - Generación de JARs

3. **Tests**
   - Tests unitarios
   - Tests de integración
   - Tests E2E

4. **Deploy**
   - Despliegue a AKS
   - Configuración de servicios

## 3. Proceso de Desarrollo

### 3.1 Flujo de Trabajo
1. Crear rama feature
2. Desarrollar cambios
3. Push a GitHub
4. Jenkins ejecuta pipeline
5. Revisar resultados

### 3.2 Validación Local
```bash
# Port Forwarding para pruebas
kubectl port-forward svc/api-gateway 8080:8080 -n default
kubectl port-forward svc/service-discovery 8761:8761 -n default
kubectl port-forward svc/cloud-config 9296:9296 -n default
kubectl port-forward svc/proxy-client 8900:8900 -n default
```

## 4. Monitoreo

### 4.1 Acceso a Herramientas
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- Zipkin: http://localhost:9411
- Kibana: http://localhost:5601

### 4.2 Port Forwarding para Monitoreo
```bash
# Grafana
kubectl port-forward svc/grafana 3000:3000 -n monitoring

# Prometheus
kubectl port-forward svc/prometheus 9090:9090 -n monitoring

# Zipkin
kubectl port-forward svc/zipkin 9411:9411 -n monitoring
```

## 5. Troubleshooting Básico

### 5.1 Verificar Estado
```bash
# Ver pods
kubectl get pods -n default

# Ver servicios
kubectl get svc -n default

# Ver logs
kubectl logs -f deployment/nombre-servicio -n default
```

### 5.2 Reiniciar Servicios
```bash
# Reiniciar deployment
kubectl rollout restart deployment/nombre-servicio -n default

# Eliminar pod para reinicio
kubectl delete pod -l app=nombre-servicio -n default
```

## 6. Contactos

### 6.1 Equipo
- DevOps: devops@example.com
- Desarrollo: dev@example.com
- Operaciones: ops@example.com

### 6.2 URLs Importantes
- Jenkins: http://jenkins-url
- GitHub: https://github.com/collinx0108/ecommerce-microservice-backend-app
- Docker Hub: https://hub.docker.com/u/collinx0108