# Infraestructura Ecommerce Microservices

Este directorio contiene la configuración de Terraform para desplegar la infraestructura necesaria para los microservicios de ecommerce en Azure.

## Estructura del Directorio

```
terraform/
├── modules/                    # Módulos reutilizables
│   ├── aks/                   # Módulo para el cluster AKS
│   ├── network/               # Módulo para la red
│   └── security/              # Módulo para la seguridad
├── environments/              # Configuraciones por ambiente
│   ├── dev/                  # Ambiente de desarrollo
│   ├── stage/                # Ambiente de staging
│   └── prod/                 # Ambiente de producción
└── backend.tf                # Configuración del backend remoto
```

## Requisitos Previos

- Azure CLI instalado
- Terraform v1.0.0 o superior
- Acceso a Azure DevOps (Organización: 1110282864)
- Permisos necesarios en Azure

## Configuración del Backend

El estado de Terraform se almacena en Azure Storage Account con la siguiente configuración:
- Resource Group: `rg-terraform-state`
- Storage Account: `stterraformstate1110282864`
- Container: `tfstate`
- Key: `ecommerce-microservice/terraform.tfstate`

## Despliegue

1. Inicializar Terraform:
```bash
terraform init
```

2. Planificar los cambios:
```bash
   terraform plan
   ```

3. Aplicar los cambios:
```bash
   terraform apply
```

## Ambientes

### Desarrollo
- 2 nodos
- VM Size: Standard_DS2_v2
- Región: East US

### Staging
- 3 nodos
- VM Size: Standard_DS3_v2
- Región: East US

### Producción
- 4 nodos
- VM Size: Standard_DS4_v2
- Región: East US

## Diagrama de Arquitectura

```
[Azure DevOps] --> [Terraform State]
    |
    v
[Resource Group] --> [AKS Cluster]
    |
    +--> [Node Pool]
    |
    +--> [Network Profile]
    |
    +--> [Identity]
```

## Tags

Todos los recursos están etiquetados con:
- Environment: dev/stage/prod
- Project: ecommerce-microservice
- Org: 1110282864 