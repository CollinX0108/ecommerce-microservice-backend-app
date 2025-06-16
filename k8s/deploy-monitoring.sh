#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 DESPLEGANDO STACK DE MONITOREO E-COMMERCE${NC}"
echo "==============================================="

# Función para mostrar el estado del despliegue
check_deployment() {
    local deployment=$1
    local namespace=$2
    
    echo -e "${YELLOW}⏳ Esperando que $deployment esté listo...${NC}"
    kubectl rollout status deployment/$deployment -n $namespace --timeout=300s
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $deployment desplegado exitosamente${NC}"
    else
        echo -e "${RED}❌ Error desplegando $deployment${NC}"
        exit 1
    fi
}

# 1. Crear namespace de monitoreo
echo -e "${BLUE}📁 Creando namespace de monitoreo...${NC}"
kubectl apply -f monitoring-namespace.yaml

# 2. Desplegar Prometheus
echo -e "${BLUE}📊 Desplegando Prometheus...${NC}"
kubectl apply -f prometheus-config.yaml
kubectl apply -f prometheus-rules.yaml
kubectl apply -f prometheus-deployment.yaml
check_deployment prometheus monitoring

# 3. Desplegar Grafana
echo -e "${BLUE}📈 Desplegando Grafana...${NC}"
kubectl apply -f grafana-config.yaml
kubectl apply -f grafana-dashboards.yaml
kubectl apply -f grafana-deployment.yaml
check_deployment grafana monitoring

# 4. Desplegar ELK Stack
echo -e "${BLUE}📋 Desplegando Elasticsearch...${NC}"
kubectl apply -f elasticsearch-deployment.yaml
check_deployment elasticsearch monitoring

echo -e "${BLUE}🔍 Desplegando Logstash...${NC}"
kubectl apply -f logstash-config.yaml
kubectl apply -f logstash-deployment.yaml
check_deployment logstash monitoring

echo -e "${BLUE}📊 Desplegando Kibana...${NC}"
kubectl apply -f kibana-deployment.yaml
check_deployment kibana monitoring

# 5. Desplegar AlertManager
echo -e "${BLUE}⚠️ Desplegando AlertManager...${NC}"
kubectl apply -f alertmanager-config.yaml

# 6. Mostrar información de acceso
echo ""
echo -e "${GREEN}🎉 DESPLIEGUE COMPLETADO EXITOSAMENTE${NC}"
echo "========================================"
echo ""
echo -e "${BLUE}📊 URLs de Acceso:${NC}"
echo -e "Prometheus:  ${YELLOW}http://localhost:30090${NC}"
echo -e "Grafana:     ${YELLOW}http://localhost:30030${NC} (admin/admin123)"
echo -e "Kibana:      ${YELLOW}http://localhost:30056${NC}"
echo ""

# 7. Mostrar estado de todos los pods
echo -e "${BLUE}📋 Estado de los Pods de Monitoreo:${NC}"
kubectl get pods -n monitoring -o wide

echo ""
echo -e "${BLUE}🔧 Servicios de Monitoreo:${NC}"
kubectl get services -n monitoring

echo ""
echo -e "${GREEN}✅ Stack de monitoreo desplegado y funcionando${NC}"
echo -e "${YELLOW}💡 Tip: Espera unos minutos para que todos los servicios estén completamente operativos${NC}"

# 8. Verificar conectividad
echo ""
echo -e "${BLUE}🔍 Verificando conectividad...${NC}"

# Port forward para acceso local
echo -e "${YELLOW}🔗 Iniciando port-forwards (en background)...${NC}"
kubectl port-forward -n monitoring svc/prometheus 9090:9090 > /dev/null 2>&1 &
kubectl port-forward -n monitoring svc/grafana 3000:3000 > /dev/null 2>&1 &
kubectl port-forward -n monitoring svc/kibana 5601:5601 > /dev/null 2>&1 &

echo -e "${GREEN}🚀 Stack de Monitoreo E-commerce listo para uso!${NC}"
echo ""
echo -e "${BLUE}📚 Próximos pasos:${NC}"
echo "1. Abrir Grafana en http://localhost:3000"
echo "2. Configurar dashboards personalizados"
echo "3. Revisar métricas en Prometheus"
echo "4. Configurar alertas según necesidades"
echo "5. Verificar logs en Kibana" 