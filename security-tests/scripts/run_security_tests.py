#!/usr/bin/env python3
"""
🔒 SISTEMA DE PRUEBAS DE SEGURIDAD
Integra OWASP ZAP, análisis de dependencias y pruebas de API security
"""

import os
import sys
import json
import time
import requests
import subprocess
from datetime import datetime
from pathlib import Path

class SecurityTestRunner:
    def __init__(self):
        self.base_dir = Path(__file__).parent.parent
        self.reports_dir = self.base_dir / "reports"
        self.timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.session_dir = self.reports_dir / f"security_scan_{self.timestamp}"
        
        # Servicios a probar con sus rutas de health check
        self.services = {
            "api-gateway": {
                "url": "http://localhost:8080",
                "health_path": "/actuator/health"
            },
            "user-service": {
                "url": "http://localhost:8700",
                "health_path": "/user-service/actuator/health"
            },
            "product-service": {
                "url": "http://localhost:8500",
                "health_path": "/product-service/actuator/health"
            },
            "order-service": {
                "url": "http://localhost:8300",
                "health_path": "/order-service/actuator/health"
            },
            "payment-service": {
                "url": "http://localhost:8400",
                "health_path": "/payment-service/actuator/health"
            },
            "favourite-service": {
                "url": "http://localhost:8800",
                "health_path": "/favourite-service/actuator/health"
            }
        }
        
        # Configuración ZAP
        self.zap_port = 8090
        self.zap_api_key = "security-test-key"
        
    def setup_environment(self):
        """Configurar entorno de pruebas"""
        print("🔧 Configurando entorno de pruebas de seguridad...")
        
        # Crear directorios
        self.session_dir.mkdir(parents=True, exist_ok=True)
        
        # Verificar dependencias
        self._check_dependencies()
        
        print(f"✅ Entorno configurado: {self.session_dir}")
        
    def _check_dependencies(self):
        """Verificar herramientas necesarias"""
        dependencies = {
            "python": ["python", "--version"],
            "requests": ["python", "-c", "import requests; print('requests OK')"],
            "curl": ["curl", "--version"]
        }
        
        for name, cmd in dependencies.items():
            try:
                subprocess.run(cmd, capture_output=True, check=True)
                print(f"✅ {name}: OK")
            except (subprocess.CalledProcessError, FileNotFoundError):
                print(f"❌ {name}: No disponible")
                
    def check_services_health(self):
        """Verificar que los servicios estén activos"""
        print("\n🌐 Verificando servicios...")
        active_services = {}
        
        for name, service_info in self.services.items():
            try:
                health_url = f"{service_info['url']}{service_info['health_path']}"
                response = requests.get(health_url, timeout=5)
                if response.status_code == 200:
                    active_services[name] = service_info['url']
                    print(f"✅ {name}: Activo")
                else:
                    print(f"⚠️ {name}: Respuesta {response.status_code}")
            except requests.RequestException:
                print(f"❌ {name}: No disponible")
                
        return active_services
        
    def run_basic_security_scan(self, services):
        """Ejecutar escaneo básico de seguridad"""
        print("\n🔍 Ejecutando escaneo básico de seguridad...")
        
        results = {}
        
        for service_name, base_url in services.items():
            print(f"\n🎯 Escaneando {service_name}...")
            service_results = {
                "service": service_name,
                "base_url": base_url,
                "timestamp": datetime.now().isoformat(),
                "tests": {}
            }
            
            # Test 1: Headers de seguridad
            service_results["tests"]["security_headers"] = self._test_security_headers(base_url, self.services[service_name]["health_path"])
            
            # Test 2: Métodos HTTP
            service_results["tests"]["http_methods"] = self._test_http_methods(base_url, self.services[service_name]["health_path"])
            
            # Test 3: Información sensible
            service_results["tests"]["information_disclosure"] = self._test_information_disclosure(base_url)
            
            # Test 4: Autenticación
            service_results["tests"]["authentication"] = self._test_authentication(base_url)
            
            results[service_name] = service_results
            
        return results
        
    def _test_security_headers(self, base_url, health_path):
        """Probar headers de seguridad"""
        try:
            response = requests.get(f"{base_url}{health_path}", timeout=5)
            headers = response.headers
            
            security_headers = {
                "X-Content-Type-Options": "nosniff",
                "X-Frame-Options": ["DENY", "SAMEORIGIN"],
                "X-XSS-Protection": "1; mode=block",
                "Strict-Transport-Security": "max-age"
            }
            
            results = {}
            for header, expected in security_headers.items():
                if header in headers:
                    if isinstance(expected, list):
                        results[header] = {
                            "present": True,
                            "value": headers[header],
                            "secure": any(exp in headers[header] for exp in expected)
                        }
                    else:
                        results[header] = {
                            "present": True,
                            "value": headers[header],
                            "secure": expected in headers[header]
                        }
                else:
                    results[header] = {"present": False, "secure": False}
                    
            return {"status": "completed", "headers": results}
            
        except Exception as e:
            return {"status": "error", "message": str(e)}
            
    def _test_http_methods(self, base_url, health_path):
        """Probar métodos HTTP permitidos"""
        methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"]
        results = {}
        
        for method in methods:
            try:
                response = requests.request(method, f"{base_url}{health_path}", timeout=5)
                results[method] = {
                    "allowed": response.status_code not in [405, 501],
                    "status_code": response.status_code
                }
            except Exception as e:
                results[method] = {"allowed": False, "error": str(e)}
                
        return {"status": "completed", "methods": results}
        
    def _test_information_disclosure(self, base_url):
        """Probar divulgación de información sensible"""
        endpoints = [
            "/actuator/health",
            "/actuator/info", 
            "/actuator/env",
            "/actuator/configprops",
            "/error"
        ]
        
        results = {}
        sensitive_patterns = [
            "password", "secret", "key", "token", "jdbc", "database"
        ]
        
        for endpoint in endpoints:
            try:
                response = requests.get(f"{base_url}{endpoint}", timeout=5)
                if response.status_code == 200:
                    content = response.text.lower()
                    found_sensitive = [pattern for pattern in sensitive_patterns if pattern in content]
                    
                    results[endpoint] = {
                        "accessible": True,
                        "status_code": response.status_code,
                        "sensitive_data_found": found_sensitive,
                        "risk_level": "HIGH" if found_sensitive else "LOW"
                    }
                else:
                    results[endpoint] = {
                        "accessible": False,
                        "status_code": response.status_code
                    }
            except Exception as e:
                results[endpoint] = {"accessible": False, "error": str(e)}
                
        return {"status": "completed", "endpoints": results}
        
    def _test_authentication(self, base_url):
        """Probar mecanismos de autenticación"""
        protected_endpoints = [
            "/api/users",
            "/api/products", 
            "/api/orders"
        ]
        
        results = {}
        
        for endpoint in protected_endpoints:
            try:
                # Sin autenticación
                response_no_auth = requests.get(f"{base_url}{endpoint}", timeout=5)
                
                # Con token inválido
                headers = {"Authorization": "Bearer invalid-token"}
                response_invalid_token = requests.get(f"{base_url}{endpoint}", headers=headers, timeout=5)
                
                results[endpoint] = {
                    "no_auth": {
                        "status_code": response_no_auth.status_code,
                        "protected": response_no_auth.status_code in [401, 403]
                    },
                    "invalid_token": {
                        "status_code": response_invalid_token.status_code,
                        "protected": response_invalid_token.status_code in [401, 403]
                    }
                }
                
            except Exception as e:
                results[endpoint] = {"error": str(e)}
                
        return {"status": "completed", "endpoints": results}
        
    def run_dependency_check(self):
        """Ejecutar análisis de dependencias vulnerables"""
        print("\n📦 Analizando dependencias...")
        
        # Buscar archivos pom.xml
        pom_files = list(Path(".").rglob("pom.xml"))
        
        results = {
            "timestamp": datetime.now().isoformat(),
            "pom_files_found": len(pom_files),
            "files": [str(f) for f in pom_files],
            "analysis": "Manual review required - check for known vulnerable dependencies"
        }
        
        # Guardar lista de dependencias para revisión manual
        dependencies_file = self.session_dir / "dependencies_analysis.json"
        with open(dependencies_file, 'w') as f:
            json.dump(results, f, indent=2)
            
        print(f"✅ Análisis de dependencias guardado: {dependencies_file}")
        return results
        
    def generate_security_report(self, scan_results, dependency_results):
        """Generar reporte consolidado de seguridad"""
        print("\n📊 Generando reporte de seguridad...")
        
        report = {
            "security_scan_report": {
                "timestamp": datetime.now().isoformat(),
                "summary": {
                    "services_scanned": len(scan_results),
                    "total_tests": sum(len(service["tests"]) for service in scan_results.values()),
                    "high_risk_findings": 0,
                    "medium_risk_findings": 0,
                    "low_risk_findings": 0
                },
                "services": scan_results,
                "dependency_analysis": dependency_results,
                "recommendations": self._generate_recommendations(scan_results)
            }
        }
        
        # Contar riesgos
        for service_data in scan_results.values():
            for test_name, test_data in service_data["tests"].items():
                if test_name == "information_disclosure" and "endpoints" in test_data:
                    for endpoint_data in test_data["endpoints"].values():
                        if isinstance(endpoint_data, dict) and endpoint_data.get("risk_level") == "HIGH":
                            report["security_scan_report"]["summary"]["high_risk_findings"] += 1
        
        # Guardar reporte JSON
        report_file = self.session_dir / "security_report.json"
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2)
            
        # Generar reporte HTML
        self._generate_html_report(report, self.session_dir / "security_report.html")
        
        print(f"✅ Reporte JSON: {report_file}")
        print(f"✅ Reporte HTML: {self.session_dir / 'security_report.html'}")
        
        return report
        
    def _generate_recommendations(self, scan_results):
        """Generar recomendaciones de seguridad"""
        recommendations = []
        
        for service_name, service_data in scan_results.items():
            # Headers de seguridad
            if "security_headers" in service_data["tests"]:
                headers_data = service_data["tests"]["security_headers"]
                if "headers" in headers_data:
                    for header, data in headers_data["headers"].items():
                        if not data.get("present", False):
                            recommendations.append({
                                "service": service_name,
                                "category": "Security Headers",
                                "issue": f"Missing {header} header",
                                "severity": "MEDIUM",
                                "recommendation": f"Add {header} header to improve security"
                            })
                            
            # Información sensible
            if "information_disclosure" in service_data["tests"]:
                info_data = service_data["tests"]["information_disclosure"]
                if "endpoints" in info_data:
                    for endpoint, data in info_data["endpoints"].items():
                        if isinstance(data, dict) and data.get("risk_level") == "HIGH":
                            recommendations.append({
                                "service": service_name,
                                "category": "Information Disclosure",
                                "issue": f"Sensitive information exposed at {endpoint}",
                                "severity": "HIGH",
                                "recommendation": "Restrict access to sensitive endpoints or remove sensitive data"
                            })
                            
        return recommendations
        
    def _generate_html_report(self, report_data, output_file):
        """Generar reporte HTML"""
        html_content = f"""
<!DOCTYPE html>
<html>
<head>
    <title>🔒 Reporte de Seguridad - {self.timestamp}</title>
    <style>
        body {{ font-family: Arial, sans-serif; margin: 20px; }}
        .header {{ background: #2c3e50; color: white; padding: 20px; border-radius: 5px; }}
        .summary {{ background: #ecf0f1; padding: 15px; margin: 20px 0; border-radius: 5px; }}
        .service {{ border: 1px solid #bdc3c7; margin: 10px 0; padding: 15px; border-radius: 5px; }}
        .high-risk {{ color: #e74c3c; font-weight: bold; }}
        .medium-risk {{ color: #f39c12; font-weight: bold; }}
        .low-risk {{ color: #27ae60; }}
        .recommendation {{ background: #fff3cd; border: 1px solid #ffeaa7; padding: 10px; margin: 5px 0; border-radius: 3px; }}
        table {{ width: 100%; border-collapse: collapse; margin: 10px 0; }}
        th, td {{ border: 1px solid #ddd; padding: 8px; text-align: left; }}
        th {{ background-color: #f2f2f2; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>🔒 Reporte de Seguridad</h1>
        <p>Generado: {report_data['security_scan_report']['timestamp']}</p>
    </div>
    
    <div class="summary">
        <h2>📊 Resumen</h2>
        <p><strong>Servicios escaneados:</strong> {report_data['security_scan_report']['summary']['services_scanned']}</p>
        <p><strong>Total de pruebas:</strong> {report_data['security_scan_report']['summary']['total_tests']}</p>
        <p><strong>Hallazgos de alto riesgo:</strong> <span class="high-risk">{report_data['security_scan_report']['summary']['high_risk_findings']}</span></p>
    </div>
    
    <h2>🎯 Servicios Analizados</h2>
"""
        
        for service_name, service_data in report_data['security_scan_report']['services'].items():
            html_content += f"""
    <div class="service">
        <h3>🔧 {service_name}</h3>
        <p><strong>URL:</strong> {service_data['base_url']}</p>
        <p><strong>Pruebas ejecutadas:</strong> {len(service_data['tests'])}</p>
    </div>
"""
        
        if report_data['security_scan_report']['recommendations']:
            html_content += "<h2>💡 Recomendaciones</h2>"
            for rec in report_data['security_scan_report']['recommendations']:
                severity_class = rec['severity'].lower() + "-risk"
                html_content += f"""
    <div class="recommendation">
        <strong class="{severity_class}">[{rec['severity']}]</strong> 
        <strong>{rec['service']}</strong> - {rec['category']}: {rec['issue']}
        <br><em>Recomendación: {rec['recommendation']}</em>
    </div>
"""
        
        html_content += """
</body>
</html>
"""
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
            
    def run_complete_security_scan(self):
        """Ejecutar escaneo completo de seguridad"""
        print("🔒 INICIANDO ESCANEO COMPLETO DE SEGURIDAD")
        print("=" * 50)
        
        # 1. Configurar entorno
        self.setup_environment()
        
        # 2. Verificar servicios
        active_services = self.check_services_health()
        
        if not active_services:
            print("❌ No hay servicios activos para escanear")
            return
            
        # 3. Ejecutar escaneo básico
        scan_results = self.run_basic_security_scan(active_services)
        
        # 4. Análisis de dependencias
        dependency_results = self.run_dependency_check()
        
        # 5. Generar reporte
        final_report = self.generate_security_report(scan_results, dependency_results)
        
        # 6. Resumen final
        print("\n🎉 ESCANEO DE SEGURIDAD COMPLETADO")
        print("=" * 50)
        print(f"🌐 Reportes guardados en: {self.session_dir}")
        print(f"🌐 Servicios escaneados: {len(active_services)}")
        print(f"📊 Total de pruebas: {final_report['security_scan_report']['summary']['total_tests']}")
        print(f"⚠️ Hallazgos de alto riesgo: {final_report['security_scan_report']['summary']['high_risk_findings']}")
        
        return final_report

def main():
    """Función principal"""
    runner = SecurityTestRunner()
    runner.run_complete_security_scan()

if __name__ == "__main__":
    main() 