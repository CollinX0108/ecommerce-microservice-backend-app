# 🚀 CÓMO USAR EL SCRIPT DE PRUEBAS DE RENDIMIENTO

## ✅ Script Único y Simple

Ahora tienes **UN SOLO SCRIPT** que hace todo lo que necesitas.

## 📋 Instrucciones

### 1. Ir al directorio correcto
```bash
cd locust/scripts
```

### 2. Ejecutar el script
```bash
./run_performance_tests.sh
```

**¡Eso es todo!** 🎉

## 🎯 Lo que hace el script:

1. ✅ **Verifica** que Locust esté instalado (lo instala si no está)
2. ✅ **Crea** un directorio único para los reportes
3. ✅ **Ejecuta** 6 tipos de pruebas automáticamente:
   - 🟢 Carga ligera: 100 usuarios, 5 min
   - 🟡 Carga normal: 200 usuarios, 10 min  
   - 🟠 Carga pesada: 400 usuarios, 15 min
   - 🔴 Prueba de picos: 1000 usuarios, 5 min
   - 🔥 Prueba de estrés: 600 usuarios, 8 min
   - ⏰ Prueba de resistencia: 250 usuarios, 20 min
4. ✅ **Genera** reportes HTML y CSV
5. ✅ **Crea** un resumen automático

## 📊 Resultados

Después de ejecutar, tendrás:
```
performance-reports_20241201_143022/
├── light_load_test.html      # Reporte visual
├── normal_load_test.html     # Reporte visual
├── heavy_load_test.html      # Reporte visual
├── spike_test.html           # Reporte visual
├── stress_test.html          # Reporte visual
├── endurance_test.html       # Reporte visual
├── *.csv                     # Datos raw
└── RESUMEN.txt               # Resumen completo
```

## 💡 Consejos

- **Abre los archivos .html** en tu navegador para ver reportes detallados
- **El script tarda ~1 hora** en completar todas las pruebas
- **Los reportes se guardan** con timestamp único (no se sobreescriben)
- **Si algo falla**, el script te lo dirá con colores

## 🎉 ¡Listo!

**Un solo comando, todas las pruebas, reportes completos.** 

Basado en el script de tus compañeros pero mejorado para ser más robusto y fácil de usar. 