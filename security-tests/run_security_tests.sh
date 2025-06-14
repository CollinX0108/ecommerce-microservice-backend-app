#!/bin/bash

# =============================================================================
# SCRIPT PRINCIPAL DE PRUEBAS DE SEGURIDAD
# Ejecuta análisis completo de seguridad del sistema de microservicios
# =============================================================================

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
PURPLE='\033[0;35m'
NC='\033[0m'

echo -e "${BLUE}🔒 INICIANDO PRUEBAS DE SEGURIDAD${NC}"
echo -e "${BLUE}=================================${NC}"

# Crear directorio de reportes con timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORTS_DIR="reports/security_tests_${TIMESTAMP}"
mkdir -p "$REPORTS_DIR"
echo -e "${GREEN}✅ Directorio creado: $REPORTS_DIR${NC}"

# Verificar Python y dependencias
echo -e "${YELLOW}📦 Verificando dependencias...${NC}"
if ! python3 -c "import requests" &> /dev/null; then
    echo -e "${YELLOW}⚠️ Instalando requests...${NC}"
    pip install requests
fi
echo -e "${GREEN}✅ Dependencias OK${NC}"

# Función para verificar servicios
check_services() {
    echo -e "${BLUE}🌐 Verificando servicios...${NC}"
    
    services=(
        "API Gateway:http://localhost:9081"
        "User Service:http://localhost:8080"
        "Product Service:http://localhost:8081"
        "Order Service:http://localhost:8082"
        "Payment Service:http://localhost:8083"
        "Favourite Service:http://localhost:8085"
    )
    
    active_count=0
    
    for service in "${services[@]}"; do
        name=$(echo $service | cut -d: -f1)
        url=$(echo $service | cut -d: -f2-3)
        
        if curl -s --max-time 3 "$url/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $name: Activo${NC}"
            ((active_count++))
        else
            echo -e "${RED}❌ $name: No disponible${NC}"
        fi
    done
    
    echo -e "${BLUE}📊 Servicios activos: $active_count/6${NC}"
    echo
}

# Ejecutar pruebas básicas de seguridad
run_basic_security_tests() {
    echo -e "${PURPLE}🔍 Ejecutando pruebas básicas de seguridad...${NC}"
    
    cd scripts/
    python3 run_security_tests.py
    cd ..
    
    echo -e "${GREEN}✅ Pruebas básicas completadas${NC}"
}

# Ejecutar escaneo ZAP
run_zap_scan() {
    echo -e "${PURPLE}🕷️ Ejecutando escaneo ZAP...${NC}"
    
    cd zap/
    python3 zap_scan.py
    cd ..
    
    echo -e "${GREEN}✅ Escaneo ZAP completado${NC}"
}

# Generar reporte consolidado
generate_consolidated_report() {
    echo -e "${BLUE}📊 Generando reporte consolidado...${NC}"
    
    SUMMARY_FILE="$REPORTS_DIR/SECURITY_SUMMARY.md"
    
    cat > "$SUMMARY_FILE" << EOF
# 🔒 REPORTE CONSOLIDADO DE SEGURIDAD

## 📋 Información General
- **Fecha**: $(date)
- **Timestamp**: $TIMESTAMP
- **Directorio**: $REPORTS_DIR

## 🎯 Pruebas Ejecutadas

### ✅ Pruebas Básicas de Seguridad
- Headers de seguridad
- Métodos HTTP permitidos
- Divulgación de información sensible
- Mecanismos de autenticación

### ✅ Escaneo OWASP ZAP
- Verificación de SSL/TLS
- Headers de seguridad
- Endpoints sensibles expuestos

## 🛡️ Recomendaciones Generales

### 🔧 Headers de Seguridad
- Implementar X-Content-Type-Options: nosniff
- Configurar X-Frame-Options: DENY o SAMEORIGIN
- Añadir X-XSS-Protection: 1; mode=block
- Configurar Strict-Transport-Security para HTTPS

### 🔐 Autenticación y Autorización
- Validar tokens JWT correctamente
- Implementar rate limiting
- Usar HTTPS en producción
- Restringir acceso a endpoints administrativos

## 🎯 Próximos Pasos

1. **Revisar reportes HTML** para detalles específicos
2. **Implementar recomendaciones** de seguridad
3. **Configurar monitoreo** continuo
4. **Establecer proceso** de revisión regular

---

**Generado por el Sistema de Pruebas de Seguridad** 🔒
EOF

    echo -e "${GREEN}✅ Reporte consolidado: $SUMMARY_FILE${NC}"
}

# Función principal
main() {
    # 1. Verificar servicios
    check_services
    
    # 2. Ejecutar pruebas básicas
    run_basic_security_tests
    
    # 3. Ejecutar escaneo ZAP
    run_zap_scan
    
    # 4. Generar reporte consolidado
    generate_consolidated_report
    
    # Mensaje final
    echo
    echo -e "${GREEN}🎉 PRUEBAS DE SEGURIDAD COMPLETADAS${NC}"
    echo -e "${GREEN}===================================${NC}"
    echo -e "${GREEN}📁 Reportes: $REPORTS_DIR${NC}"
    echo -e "${GREEN}📊 Resumen: $REPORTS_DIR/SECURITY_SUMMARY.md${NC}"
    echo
    echo -e "${YELLOW}💡 Abre los archivos HTML para ver reportes detallados${NC}"
    echo -e "${BLUE}🔒 Revisa las recomendaciones de seguridad${NC}"
    
    echo
    echo -e "${PURPLE}🚀 ¡Análisis de seguridad completado!${NC}"
}

# Ejecutar función principal
main 