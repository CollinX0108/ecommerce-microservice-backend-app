from locust import HttpUser, task, between
import random

class ProductServiceUser(HttpUser):
    """Usuario especializado para pruebas intensivas del servicio de productos"""
    wait_time = between(1, 3)
    
    def on_start(self):
        """Configurar datos de prueba"""
        self.product_ids = [1, 2, 3, 4]  # IDs de productos existentes
        self.category_ids = [1, 2, 3]    # IDs de categorías existentes
    
    @task(5)
    def get_all_products(self):
        """Obtener lista de todos los productos"""
        with self.client.get("/product-service/api/products", 
                           catch_response=True, 
                           name="GET /api/products") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(4)
    def get_product_by_id(self):
        """Obtener producto específico por ID"""
        product_id = random.choice(self.product_ids)
        with self.client.get(f"/product-service/api/products/{product_id}", 
                           catch_response=True, 
                           name="GET /api/products/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(3)
    def get_all_categories(self):
        """Obtener lista de todas las categorías"""
        with self.client.get("/product-service/api/categories", 
                           catch_response=True, 
                           name="GET /api/categories") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(3)
    def get_category_by_id(self):
        """Obtener categoría específica por ID"""
        category_id = random.choice(self.category_ids)
        with self.client.get(f"/product-service/api/categories/{category_id}", 
                           catch_response=True, 
                           name="GET /api/categories/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(2)
    def get_products_by_category(self):
        """Obtener productos por categoría"""
        category_id = random.choice(self.category_ids)
        with self.client.get(f"/product-service/api/categories/{category_id}/products", 
                           catch_response=True, 
                           name="GET /api/categories/{id}/products") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(1)
    def create_product(self):
        """Crear nuevo producto (simulación)"""
        product_data = {
            "productTitle": f"Test Product {random.randint(1000, 9999)}",
            "imageUrl": "https://example.com/image.jpg",
            "sku": f"SKU-{random.randint(10000, 99999)}",
            "priceUnit": round(random.uniform(10.0, 500.0), 2),
            "quantity": random.randint(1, 100),
            "categoryDto": {
                "categoryId": random.choice(self.category_ids)
            }
        }
        
        with self.client.post("/product-service/api/products", 
                            json=product_data,
                            catch_response=True, 
                            name="POST /api/products") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(1)
    def create_category(self):
        """Crear nueva categoría (simulación)"""
        category_data = {
            "categoryTitle": f"Test Category {random.randint(1000, 9999)}",
            "imageUrl": "https://example.com/category.jpg"
        }
        
        with self.client.post("/product-service/api/categories", 
                            json=category_data,
                            catch_response=True, 
                            name="POST /api/categories") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")

class ProductServiceStressUser(HttpUser):
    """Usuario para pruebas de estrés específicas del servicio de productos"""
    wait_time = between(0.5, 1.5)
    
    def on_start(self):
        self.product_ids = [1, 2, 3, 4]
        self.category_ids = [1, 2, 3]
    
    @task(8)
    def rapid_product_browsing(self):
        """Navegación rápida e intensiva de productos"""
        # Múltiples consultas rápidas
        for _ in range(random.randint(3, 6)):
            product_id = random.choice(self.product_ids)
            self.client.get(f"/product-service/api/products/{product_id}", 
                          name="STRESS_product_detail")
    
    @task(5)
    def rapid_category_browsing(self):
        """Navegación rápida e intensiva de categorías"""
        for _ in range(random.randint(2, 4)):
            category_id = random.choice(self.category_ids)
            self.client.get(f"/product-service/api/categories/{category_id}", 
                          name="STRESS_category_detail")
    
    @task(3)
    def concurrent_operations(self):
        """Operaciones concurrentes para probar límites"""
        # Simular múltiples operaciones simultáneas
        self.client.get("/product-service/api/products", name="STRESS_products_list")
        self.client.get("/product-service/api/categories", name="STRESS_categories_list")

if __name__ == "__main__":
    print("🔥 Product Service Load Test")
    print("Available user classes:")
    print("- ProductServiceUser: Normal product service operations")
    print("- ProductServiceStressUser: Stress testing for product service")
    print()
    print("Example usage:")
    print("locust -f locustfile.py --headless -u 50 -r 5 --run-time 10m --host http://localhost:8081") 