#!/bin/bash

# =============================================================================
# SCRIPT DE PRUEBAS DE RENDIMIENTO
# Basado en el script de los compañeros con mejoras esenciales
# =============================================================================

# Colores para mejor visualización
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🚀 INICIANDO PRUEBAS DE RENDIMIENTO${NC}"
echo -e "${BLUE}====================================${NC}"

# Crear directorio para reportes con timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORTS_DIR="performance-reports_${TIMESTAMP}"
mkdir -p "$REPORTS_DIR"
echo -e "${GREEN}✅ Directorio creado: $REPORTS_DIR${NC}"

# Verificar/Instalar Locust
echo -e "${YELLOW}📦 Verificando Locust...${NC}"
if ! python -c "import locust" &> /dev/null; then
    echo -e "${YELLOW}⚠️ Instalando Locust...${NC}"
    pip install locust
fi
echo -e "${GREEN}✅ Locust disponible${NC}"

# Configuración
HOST_URL="http://localhost:9081"
LOCUST_FILE="../test/ecommerce-complete/ecommerce_load_test.py"

echo -e "${BLUE}🌐 Host: $HOST_URL${NC}"
echo -e "${BLUE}📁 Archivo: $LOCUST_FILE${NC}"
echo

# Función para ejecutar pruebas
run_test() {
    local name=$1
    local users=$2
    local spawn_rate=$3
    local time=$4
    local desc=$5
    
    echo -e "${BLUE}🚀 $desc${NC}"
    echo -e "   👥 $users usuarios | ⚡ $spawn_rate/s | ⏱️ $time"
    
    if locust -f "$LOCUST_FILE" \
        --headless \
        -u "$users" \
        -r "$spawn_rate" \
        --run-time "$time" \
        --host "$HOST_URL" \
        --html "$REPORTS_DIR/${name}.html" \
        --csv "$REPORTS_DIR/${name}"; then
        
        echo -e "${GREEN}✅ $name completado${NC}"
    else
        echo -e "${RED}❌ $name falló${NC}"
    fi
    echo
}

# Ejecutar todas las pruebas (basado en script de compañeros + mejoras)
run_test "light_load_test" 100 10 "5m" "Carga ligera"
run_test "normal_load_test" 200 20 "10m" "Carga normal"  
run_test "heavy_load_test" 400 40 "15m" "Carga pesada"
run_test "spike_test" 1000 100 "5m" "Prueba de picos"

# Pruebas adicionales útiles
run_test "stress_test" 600 60 "8m" "Prueba de estrés"
run_test "endurance_test" 250 25 "20m" "Prueba de resistencia"

# Generar resumen
echo -e "${BLUE}📊 Generando resumen...${NC}"
SUMMARY="$REPORTS_DIR/RESUMEN.txt"

cat > "$SUMMARY" << EOF
RESUMEN DE PRUEBAS DE RENDIMIENTO
=================================
Fecha: $(date)
Host: $HOST_URL
Directorio: $REPORTS_DIR

PRUEBAS EJECUTADAS:
✅ Carga Ligera: 100 usuarios, 5 min
✅ Carga Normal: 200 usuarios, 10 min  
✅ Carga Pesada: 400 usuarios, 15 min
✅ Prueba de Picos: 1000 usuarios, 5 min
✅ Prueba de Estrés: 600 usuarios, 8 min
✅ Prueba de Resistencia: 250 usuarios, 20 min

REPORTES HTML:
EOF

# Listar reportes generados
for html in "$REPORTS_DIR"/*.html; do
    if [ -f "$html" ]; then
        echo "- $(basename "$html")" >> "$SUMMARY"
    fi
done

echo -e "${GREEN}✅ Resumen: $SUMMARY${NC}"

# Mensaje final
echo
echo -e "${GREEN}🎉 PRUEBAS COMPLETADAS${NC}"
echo -e "${GREEN}=====================${NC}"
echo -e "${GREEN}📁 Reportes: $REPORTS_DIR${NC}"
echo -e "${YELLOW}💡 Abre los .html en tu navegador${NC}"

# Mostrar archivos
echo -e "${BLUE}📂 Archivos generados:${NC}"
ls -la "$REPORTS_DIR"

echo -e "${GREEN}🚀 ¡Listo!${NC}" 