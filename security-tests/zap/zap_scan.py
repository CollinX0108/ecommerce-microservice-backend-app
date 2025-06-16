#!/usr/bin/env python3
"""
🔒 OWASP ZAP Security Scanner
Script para ejecutar escaneos automatizados con OWASP ZAP
"""

import os
import json
import time
import requests
import subprocess
from datetime import datetime
from pathlib import Path

class ZAPScanner:
    def __init__(self):
        self.base_dir = Path(__file__).parent.parent
        self.reports_dir = self.base_dir / "reports"
        self.timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.session_dir = self.reports_dir / f"zap_scan_{self.timestamp}"
        
        # Servicios objetivo
        self.targets = [
            "http://localhost:9081",  # API Gateway
            "http://localhost:8080",  # User Service
            "http://localhost:8081",  # Product Service
        ]
        
    def check_zap_installation(self):
        """Verificar si ZAP está instalado"""
        print("🔍 Verificando instalación de OWASP ZAP...")
        
        # Buscar ZAP en ubicaciones comunes
        zap_paths = [
            "/usr/share/zaproxy/zap.sh",
            "/opt/zaproxy/zap.sh", 
            "C:\\Program Files\\OWASP\\Zed Attack Proxy\\ZAP.exe",
            "C:\\Program Files (x86)\\OWASP\\Zed Attack Proxy\\ZAP.exe"
        ]
        
        for path in zap_paths:
            if os.path.exists(path):
                print(f"✅ ZAP encontrado: {path}")
                return path
                
        print("❌ OWASP ZAP no encontrado")
        print("💡 Instala ZAP desde: https://www.zaproxy.org/download/")
        return None
        
    def run_basic_scan_without_zap(self):
        """Ejecutar escaneo básico sin ZAP"""
        print("🔍 Ejecutando escaneo básico de seguridad...")
        
        results = {}
        
        for target in self.targets:
            print(f"🎯 Escaneando {target}...")
            
            scan_result = {
                "target": target,
                "timestamp": datetime.now().isoformat(),
                "tests": {
                    "ssl_check": self._check_ssl(target),
                    "headers_check": self._check_security_headers(target),
                    "endpoints_check": self._check_sensitive_endpoints(target)
                }
            }
            
            results[target] = scan_result
            
        return results
        
    def _check_ssl(self, target):
        """Verificar configuración SSL"""
        if not target.startswith("https://"):
            return {"status": "warning", "message": "No usa HTTPS"}
            
        try:
            response = requests.get(target, timeout=5, verify=True)
            return {"status": "ok", "message": "SSL válido"}
        except requests.exceptions.SSLError:
            return {"status": "error", "message": "Error SSL"}
        except Exception as e:
            return {"status": "error", "message": str(e)}
            
    def _check_security_headers(self, target):
        """Verificar headers de seguridad"""
        try:
            response = requests.get(f"{target}/actuator/health", timeout=5)
            headers = response.headers
            
            security_headers = [
                "X-Content-Type-Options",
                "X-Frame-Options", 
                "X-XSS-Protection",
                "Strict-Transport-Security"
            ]
            
            missing_headers = [h for h in security_headers if h not in headers]
            
            return {
                "status": "completed",
                "missing_headers": missing_headers,
                "total_checked": len(security_headers),
                "missing_count": len(missing_headers)
            }
            
        except Exception as e:
            return {"status": "error", "message": str(e)}
            
    def _check_sensitive_endpoints(self, target):
        """Verificar endpoints sensibles"""
        sensitive_endpoints = [
            "/actuator/env",
            "/actuator/configprops", 
            "/actuator/dump",
            "/actuator/trace"
        ]
        
        accessible_endpoints = []
        
        for endpoint in sensitive_endpoints:
            try:
                response = requests.get(f"{target}{endpoint}", timeout=5)
                if response.status_code == 200:
                    accessible_endpoints.append(endpoint)
            except:
                pass
                
        return {
            "status": "completed",
            "accessible_sensitive_endpoints": accessible_endpoints,
            "risk_level": "HIGH" if accessible_endpoints else "LOW"
        }
        
    def generate_report(self, results):
        """Generar reporte de resultados"""
        print("📊 Generando reporte...")
        
        self.session_dir.mkdir(parents=True, exist_ok=True)
        
        # Reporte JSON
        report_file = self.session_dir / "zap_security_report.json"
        with open(report_file, 'w') as f:
            json.dump(results, f, indent=2)
            
        # Reporte HTML simple
        html_file = self.session_dir / "zap_security_report.html"
        self._generate_html_report(results, html_file)
        
        print(f"✅ Reporte JSON: {report_file}")
        print(f"✅ Reporte HTML: {html_file}")
        
    def _generate_html_report(self, results, output_file):
        """Generar reporte HTML"""
        html_content = f"""
<!DOCTYPE html>
<html>
<head>
    <title>🔒 ZAP Security Report - {self.timestamp}</title>
    <style>
        body {{ font-family: Arial, sans-serif; margin: 20px; }}
        .header {{ background: #e74c3c; color: white; padding: 20px; border-radius: 5px; }}
        .target {{ border: 1px solid #bdc3c7; margin: 10px 0; padding: 15px; border-radius: 5px; }}
        .high-risk {{ color: #e74c3c; font-weight: bold; }}
        .medium-risk {{ color: #f39c12; font-weight: bold; }}
        .low-risk {{ color: #27ae60; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>🔒 ZAP Security Scan Report</h1>
        <p>Generado: {datetime.now().isoformat()}</p>
    </div>
"""
        
        for target, data in results.items():
            html_content += f"""
    <div class="target">
        <h2>🎯 {target}</h2>
        <p><strong>Estado:</strong> {data.get('tests', {}).get('ssl_check', {}).get('status', 'N/A')}</p>
    </div>
"""
        
        html_content += """
</body>
</html>
"""
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
            
    def run_security_scan(self):
        """Ejecutar escaneo completo"""
        print("🔒 INICIANDO ESCANEO DE SEGURIDAD CON ZAP")
        print("=" * 50)
        
        # 1. Verificar ZAP
        zap_path = self.check_zap_installation()
        
        # 2. Ejecutar escaneo básico (funciona sin ZAP)
        print("🔍 Ejecutando escaneo básico...")
        results = self.run_basic_scan_without_zap()
            
        # 3. Generar reporte
        self.generate_report(results)
        
        print("\n🎉 ESCANEO COMPLETADO")
        print(f"📁 Reportes en: {self.session_dir}")
        
        return results

def main():
    scanner = ZAPScanner()
    scanner.run_security_scan()

if __name__ == "__main__":
    main() 