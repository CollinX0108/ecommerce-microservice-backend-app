#!/usr/bin/env python3
"""
Script para ejecutar diferentes tipos de pruebas de rendimiento con Locust
"""

import subprocess
import sys
import time
import argparse
from datetime import datetime
import os

class LoadTestRunner:
    def __init__(self):
        self.base_url = "http://localhost:9081"  # API Gateway
        self.reports_dir = "../reports"
        
        # Configuraciones de prueba
        self.test_configs = {
            "light": {
                "users": 50,
                "spawn_rate": 5,
                "run_time": "10m",
                "description": "Carga ligera - 50 usuarios"
            },
            "normal": {
                "users": 100,
                "spawn_rate": 10,
                "run_time": "15m",
                "description": "Carga normal - 100 usuarios"
            },
            "heavy": {
                "users": 200,
                "spawn_rate": 20,
                "run_time": "20m",
                "description": "Carga pesada - 200 usuarios"
            },
            "stress": {
                "users": 300,
                "spawn_rate": 30,
                "run_time": "15m",
                "description": "Prueba de estrés - 300 usuarios"
            },
            "spike": {
                "users": 500,
                "spawn_rate": 50,
                "run_time": "10m",
                "description": "Prueba de picos - 500 usuarios"
            },
            "endurance": {
                "users": 150,
                "spawn_rate": 15,
                "run_time": "60m",
                "description": "Prueba de resistencia - 150 usuarios por 1 hora"
            }
        }
        
        # Servicios específicos
        self.service_configs = {
            "product-service": {
                "port": 8081,
                "users": 100,
                "spawn_rate": 10,
                "run_time": "15m"
            },
            "user-service": {
                "port": 8080,
                "users": 50,
                "spawn_rate": 5,
                "run_time": "15m"
            },
            "order-service": {
                "port": 8082,
                "users": 80,
                "spawn_rate": 8,
                "run_time": "15m"
            },
            "payment-service": {
                "port": 8083,
                "users": 60,
                "spawn_rate": 6,
                "run_time": "15m"
            },
            "favourite-service": {
                "port": 8085,
                "users": 70,
                "spawn_rate": 7,
                "run_time": "15m"
            }
        }
    
    def create_reports_dir(self):
        """Crear directorio de reportes si no existe"""
        if not os.path.exists(self.reports_dir):
            os.makedirs(self.reports_dir)
    
    def run_complete_system_test(self, test_type="normal"):
        """Ejecutar prueba completa del sistema"""
        if test_type not in self.test_configs:
            print(f"❌ Tipo de prueba '{test_type}' no válido")
            return False
        
        config = self.test_configs[test_type]
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_file = f"{self.reports_dir}/complete_system_{test_type}_{timestamp}"
        
        print(f"🚀 Iniciando prueba completa del sistema: {config['description']}")
        print(f"📊 Configuración: {config['users']} usuarios, {config['spawn_rate']} spawn rate, {config['run_time']}")
        print(f"📁 Reporte: {report_file}")
        
        cmd = [
            "locust",
            "-f", "../test/ecommerce-complete/ecommerce_load_test.py",
            "--headless",
            "-u", str(config['users']),
            "-r", str(config['spawn_rate']),
            "--run-time", config['run_time'],
            "--host", self.base_url,
            "--html", f"{report_file}.html",
            "--csv", report_file
        ]
        
        try:
            result = subprocess.run(cmd, check=True, capture_output=True, text=True)
            print("✅ Prueba completada exitosamente")
            print(f"📊 Reporte HTML: {report_file}.html")
            return True
        except subprocess.CalledProcessError as e:
            print(f"❌ Error ejecutando prueba: {e}")
            print(f"Output: {e.stdout}")
            print(f"Error: {e.stderr}")
            return False
    
    def run_service_specific_test(self, service_name):
        """Ejecutar prueba específica de un servicio"""
        if service_name not in self.service_configs:
            print(f"❌ Servicio '{service_name}' no válido")
            return False
        
        config = self.service_configs[service_name]
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_file = f"{self.reports_dir}/{service_name}_{timestamp}"
        host_url = f"http://localhost:{config['port']}"
        
        print(f"🎯 Iniciando prueba específica: {service_name}")
        print(f"📊 Configuración: {config['users']} usuarios, {config['spawn_rate']} spawn rate, {config['run_time']}")
        print(f"🌐 Host: {host_url}")
        print(f"📁 Reporte: {report_file}")
        
        cmd = [
            "locust",
            "-f", f"../test/{service_name}/locustfile.py",
            "--headless",
            "-u", str(config['users']),
            "-r", str(config['spawn_rate']),
            "--run-time", config['run_time'],
            "--host", host_url,
            "--html", f"{report_file}.html",
            "--csv", report_file
        ]
        
        try:
            result = subprocess.run(cmd, check=True, capture_output=True, text=True)
            print("✅ Prueba completada exitosamente")
            print(f"📊 Reporte HTML: {report_file}.html")
            return True
        except subprocess.CalledProcessError as e:
            print(f"❌ Error ejecutando prueba: {e}")
            print(f"Output: {e.stdout}")
            print(f"Error: {e.stderr}")
            return False
    
    def run_all_services_test(self):
        """Ejecutar pruebas en todos los servicios secuencialmente"""
        print("🔄 Iniciando pruebas en todos los servicios...")
        results = {}
        
        for service_name in self.service_configs.keys():
            print(f"\n{'='*50}")
            print(f"🎯 Probando {service_name}")
            print(f"{'='*50}")
            
            success = self.run_service_specific_test(service_name)
            results[service_name] = success
            
            if success:
                print(f"✅ {service_name}: EXITOSO")
            else:
                print(f"❌ {service_name}: FALLÓ")
            
            # Pausa entre pruebas para evitar sobrecarga
            if service_name != list(self.service_configs.keys())[-1]:
                print("⏳ Esperando 30 segundos antes de la siguiente prueba...")
                time.sleep(30)
        
        # Resumen final
        print(f"\n{'='*50}")
        print("📊 RESUMEN DE PRUEBAS")
        print(f"{'='*50}")
        
        successful = sum(1 for success in results.values() if success)
        total = len(results)
        
        for service, success in results.items():
            status = "✅ EXITOSO" if success else "❌ FALLÓ"
            print(f"{service}: {status}")
        
        print(f"\n🎯 Resultado final: {successful}/{total} servicios probados exitosamente")
        return results
    
    def run_custom_test(self, users, spawn_rate, run_time, test_file=None, host=None):
        """Ejecutar prueba personalizada"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_file = f"{self.reports_dir}/custom_test_{timestamp}"
        
        if not test_file:
            test_file = "../test/ecommerce-complete/ecommerce_load_test.py"
        
        if not host:
            host = self.base_url
        
        print(f"⚙️ Iniciando prueba personalizada")
        print(f"📊 Configuración: {users} usuarios, {spawn_rate} spawn rate, {run_time}")
        print(f"📁 Archivo de prueba: {test_file}")
        print(f"🌐 Host: {host}")
        print(f"📁 Reporte: {report_file}")
        
        cmd = [
            "locust",
            "-f", test_file,
            "--headless",
            "-u", str(users),
            "-r", str(spawn_rate),
            "--run-time", run_time,
            "--host", host,
            "--html", f"{report_file}.html",
            "--csv", report_file
        ]
        
        try:
            result = subprocess.run(cmd, check=True, capture_output=True, text=True)
            print("✅ Prueba personalizada completada exitosamente")
            print(f"📊 Reporte HTML: {report_file}.html")
            return True
        except subprocess.CalledProcessError as e:
            print(f"❌ Error ejecutando prueba personalizada: {e}")
            return False

def main():
    parser = argparse.ArgumentParser(description="Ejecutor de pruebas de rendimiento con Locust")
    parser.add_argument("--type", choices=["light", "normal", "heavy", "stress", "spike", "endurance"], 
                       default="normal", help="Tipo de prueba del sistema completo")
    parser.add_argument("--service", choices=["product-service", "user-service", "order-service", 
                                            "payment-service", "favourite-service"], 
                       help="Ejecutar prueba específica de un servicio")
    parser.add_argument("--all-services", action="store_true", 
                       help="Ejecutar pruebas en todos los servicios")
    parser.add_argument("--custom", action="store_true", 
                       help="Ejecutar prueba personalizada")
    parser.add_argument("--users", type=int, default=100, 
                       help="Número de usuarios para prueba personalizada")
    parser.add_argument("--spawn-rate", type=int, default=10, 
                       help="Tasa de spawn para prueba personalizada")
    parser.add_argument("--run-time", default="15m", 
                       help="Duración de prueba personalizada")
    parser.add_argument("--host", help="Host para prueba personalizada")
    parser.add_argument("--test-file", help="Archivo de prueba para prueba personalizada")
    
    args = parser.parse_args()
    
    runner = LoadTestRunner()
    runner.create_reports_dir()
    
    print("🚀 EJECUTOR DE PRUEBAS DE RENDIMIENTO")
    print("=" * 50)
    
    if args.all_services:
        runner.run_all_services_test()
    elif args.service:
        runner.run_service_specific_test(args.service)
    elif args.custom:
        runner.run_custom_test(args.users, args.spawn_rate, args.run_time, 
                              args.test_file, args.host)
    else:
        runner.run_complete_system_test(args.type)

if __name__ == "__main__":
    main() 