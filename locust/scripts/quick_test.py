#!/usr/bin/env python3
"""
Script para ejecutar una prueba rápida de demostración del sistema de rendimiento
"""

import subprocess
import sys
import os
from datetime import datetime

def run_quick_test():
    """Ejecutar una prueba rápida de demostración"""
    print("🚀 PRUEBA RÁPIDA DE RENDIMIENTO - DEMOSTRACIÓN")
    print("=" * 60)
    
    # Crear directorio de reportes si no existe
    reports_dir = "../reports"
    if not os.path.exists(reports_dir):
        os.makedirs(reports_dir)
        print(f"📁 Directorio de reportes creado: {reports_dir}")
    
    # Configuración de la prueba rápida
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    report_file = f"{reports_dir}/quick_test_{timestamp}"
    
    print(f"⚡ Configuración de prueba rápida:")
    print(f"   - Usuarios: 10")
    print(f"   - Spawn rate: 2 usuarios/segundo")
    print(f"   - Duración: 30 segundos")
    print(f"   - Host: http://localhost:9081 (API Gateway)")
    print(f"   - Reporte: {report_file}.html")
    print()
    
    # Comando de Locust
    cmd = [
        "locust",
        "-f", "../test/ecommerce-complete/ecommerce_load_test.py",
        "--headless",
        "-u", "10",
        "-r", "2", 
        "--run-time", "30s",
        "--host", "http://localhost:9081",
        "--html", f"{report_file}.html",
        "--csv", report_file
    ]
    
    print("🔄 Iniciando prueba...")
    print(f"💻 Comando: {' '.join(cmd)}")
    print()
    
    try:
        # Ejecutar la prueba
        result = subprocess.run(cmd, check=True, capture_output=True, text=True)
        
        print("✅ ¡PRUEBA COMPLETADA EXITOSAMENTE!")
        print("=" * 60)
        print(f"📊 Reporte HTML generado: {report_file}.html")
        print(f"📈 Datos CSV generados: {report_file}_stats.csv")
        print()
        print("🎯 Para ver el reporte detallado, abre el archivo HTML en tu navegador")
        print()
        print("📋 Próximos pasos:")
        print("   1. Revisar el reporte HTML para métricas detalladas")
        print("   2. Ejecutar pruebas más largas con más usuarios")
        print("   3. Probar servicios específicos individualmente")
        print()
        print("🚀 Comandos de ejemplo para pruebas más avanzadas:")
        print("   # Prueba normal del sistema completo")
        print("   python run_load_tests.py --type normal")
        print()
        print("   # Prueba específica del servicio de productos")
        print("   python run_load_tests.py --service product-service")
        print()
        print("   # Prueba de estrés")
        print("   python run_load_tests.py --type stress")
        
        return True
        
    except subprocess.CalledProcessError as e:
        print("❌ ERROR EJECUTANDO LA PRUEBA")
        print("=" * 60)
        print(f"Código de salida: {e.returncode}")
        print(f"Output: {e.stdout}")
        print(f"Error: {e.stderr}")
        print()
        print("🔍 Posibles soluciones:")
        print("   1. Verificar que los microservicios estén ejecutándose")
        print("   2. Comprobar que el API Gateway esté en http://localhost:9081")
        print("   3. Instalar dependencias: pip install -r requirements.txt")
        print("   4. Verificar conectividad de red")
        
        return False
    
    except FileNotFoundError:
        print("❌ ERROR: Locust no está instalado")
        print("=" * 60)
        print("🔧 Para instalar Locust:")
        print("   pip install locust")
        print("   # o")
        print("   pip install -r requirements.txt")
        
        return False

def check_services():
    """Verificar que los servicios estén ejecutándose"""
    print("🔍 Verificando servicios...")
    
    services = [
        ("API Gateway", "http://localhost:9081"),
        ("Product Service", "http://localhost:8081"),
        ("User Service", "http://localhost:8080"),
        ("Order Service", "http://localhost:8082"),
        ("Payment Service", "http://localhost:8083"),
        ("Favourite Service", "http://localhost:8085")
    ]
    
    try:
        import requests
        
        for service_name, url in services:
            try:
                response = requests.get(f"{url}/actuator/health", timeout=5)
                if response.status_code == 200:
                    print(f"   ✅ {service_name}: OK")
                else:
                    print(f"   ⚠️ {service_name}: Respuesta {response.status_code}")
            except requests.exceptions.RequestException:
                print(f"   ❌ {service_name}: No disponible")
        
        print()
        
    except ImportError:
        print("   ⚠️ Módulo 'requests' no disponible para verificar servicios")
        print("   💡 Instalar con: pip install requests")
        print()

if __name__ == "__main__":
    print("🎯 SISTEMA DE PRUEBAS DE RENDIMIENTO")
    print("   Prueba rápida de demostración")
    print()
    
    # Verificar servicios (opcional)
    check_services()
    
    # Ejecutar prueba rápida
    success = run_quick_test()
    
    if success:
        print("\n🎉 ¡Prueba de demostración completada exitosamente!")
        sys.exit(0)
    else:
        print("\n💥 La prueba de demostración falló")
        sys.exit(1) 