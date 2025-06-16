# 🔒 Pruebas de Seguridad

## ✅ Sistema Completo de Seguridad

Sistema integrado de pruebas de seguridad para el proyecto de microservicios.

## 🚀 Uso Rápido

### Ejecutar todas las pruebas
```bash
cd security-tests
./run_security_tests.sh
```

**¡Eso es todo!** 🎉

## 🎯 Lo que incluye:

### 🔍 Pruebas Básicas
- ✅ **Headers de seguridad**: X-Content-Type-Options, X-Frame-Options, etc.
- ✅ **Métodos HTTP**: Verificación de métodos permitidos
- ✅ **Información sensible**: Detección de datos expuestos
- ✅ **Autenticación**: Validación de endpoints protegidos

### 🕷️ Escaneo OWASP ZAP
- ✅ **SSL/TLS**: Verificación de configuración
- ✅ **Endpoints sensibles**: Detección de exposición
- ✅ **Configuración básica**: Sin necesidad de instalar ZAP

## 📊 Reportes Generados

Después de ejecutar, tendrás:
```
reports/security_tests_20241201_143022/
├── security_report.html          # Reporte visual principal
├── security_report.json          # Datos estructurados
├── zap_security_report.html      # Reporte ZAP
├── zap_security_report.json      # Datos ZAP
└── SECURITY_SUMMARY.md           # Resumen consolidado
```

## 🛡️ Servicios Analizados

- **API Gateway**: http://localhost:9081
- **User Service**: http://localhost:8080
- **Product Service**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **Payment Service**: http://localhost:8083
- **Favourite Service**: http://localhost:8085

## ⏱️ Tiempo de Ejecución

- **Pruebas básicas**: ~2-3 minutos
- **Escaneo ZAP**: ~1-2 minutos
- **Total**: ~5 minutos

## 💡 Recomendaciones Principales

### 🔧 Headers de Seguridad
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
```

### 🔐 Configuración Segura
- Usar HTTPS en producción
- Restringir endpoints administrativos
- Implementar rate limiting
- Validar tokens JWT correctamente

## 🎯 Estructura del Proyecto

```
security-tests/
├── scripts/
│   └── run_security_tests.py     # Pruebas básicas
├── zap/
│   └── zap_scan.py               # Escaneo ZAP
├── reports/                      # Reportes generados
├── run_security_tests.sh         # Script principal
└── README.md                     # Esta documentación
```

## 🚨 Troubleshooting

### Servicios no disponibles
```bash
# Verificar servicios manualmente
curl http://localhost:9081/actuator/health
```

### Error de dependencias
```bash
pip install requests
```

### Permisos en Linux/Mac
```bash
chmod +x run_security_tests.sh
```

## 🎉 Características

- ✅ **Sin instalaciones complejas**: Funciona con Python básico
- ✅ **Reportes HTML**: Visualización clara de resultados
- ✅ **Análisis automático**: Sin configuración manual
- ✅ **Integración fácil**: Se ejecuta junto a otras pruebas
- ✅ **Recomendaciones**: Sugerencias específicas de mejora

---

**¡Sistema de seguridad listo para validar la protección de tus microservicios!** 🔒 