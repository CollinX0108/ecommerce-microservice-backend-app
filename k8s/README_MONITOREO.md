# 🚀 **DESPLIEGUE RÁPIDO - STACK DE MONITOREO**

## 📋 **RESUMEN**
Stack completo de observabilidad y monitoreo para microservicios de e-commerce:
- ✅ **Prometheus + Grafana** (métricas y dashboards)
- ✅ **ELK Stack** (logs centralizados)
- ✅ **AlertManager** (alertas inteligentes)
- ✅ **Zipkin** (tracing distribuido - ya configurado)

## 🚀 **DESPLIEGUE EN 1 COMANDO**

### **Windows (PowerShell)**
```powershell
cd k8s
.\deploy-monitoring.sh
```

### **Linux/Mac**
```bash
cd k8s
chmod +x deploy-monitoring.sh
./deploy-monitoring.sh
```

## 📊 **URLS DE ACCESO**

Una vez desplegado, accede a:

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Prometheus** | http://localhost:30090 | - |
| **Grafana** | http://localhost:30030 | admin/admin123 |
| **Kibana** | http://localhost:30056 | - |

## ⏱️ **TIEMPO DE DESPLIEGUE**
- **Despliegue inicial**: ~5-8 minutos
- **Disponibilidad completa**: ~10 minutos

## 🔍 **VERIFICACIÓN RÁPIDA**

```bash
# Ver estado de pods
kubectl get pods -n monitoring

# Debe mostrar todos como "Running":
# - prometheus-xxx
# - grafana-xxx  
# - elasticsearch-xxx
# - logstash-xxx
# - kibana-xxx
```

## 📈 **DASHBOARDS INCLUIDOS**

### **Grafana - Dashboards Automáticos**
1. **E-commerce Overview** - Estado general del sistema
2. **Business Metrics** - KPIs de negocio (órdenes, ingresos, usuarios)

### **Métricas Monitoreadas**
- ✅ Disponibilidad de servicios
- ✅ Tiempos de respuesta
- ✅ Tasa de errores
- ✅ Uso de recursos (CPU/RAM)
- ✅ Métricas de negocio

## ⚠️ **ALERTAS CONFIGURADAS**

### **Críticas** 🔴
- Servicios caídos (>1min)
- Gateway de pagos no disponible (>30s)
- Alta tasa de errores (>5%)
- Fallos masivos en órdenes (>10 en 5min)

### **Advertencias** 🟡
- Alto uso CPU/memoria (>80%/85%)
- Tiempos de respuesta elevados (>1s)
- Inventario bajo (<10 unidades)

## 🔧 **TROUBLESHOOTING**

### **Si algo no funciona:**
```bash
# Reiniciar componente específico
kubectl rollout restart deployment/prometheus -n monitoring
kubectl rollout restart deployment/grafana -n monitoring

# Ver logs de errores
kubectl logs -n monitoring deployment/prometheus
kubectl logs -n monitoring deployment/grafana
```

### **Port-forwarding manual:**
```bash
kubectl port-forward -n monitoring svc/prometheus 9090:9090
kubectl port-forward -n monitoring svc/grafana 3000:3000
kubectl port-forward -n monitoring svc/kibana 5601:5601
```

## 📚 **DOCUMENTACIÓN COMPLETA**
Ver: `docs/OBSERVABILIDAD_MONITOREO.md`

---
**🎯 Stack de monitoreo listo en menos de 10 minutos!** 