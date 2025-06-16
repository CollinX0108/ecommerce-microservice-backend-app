from locust import HttpUser, task, between
import json
import random
import time
from datetime import datetime

class EcommerceUser(HttpUser):
    """Usuario principal para pruebas de e-commerce completas"""
    wait_time = between(2, 5)
    
    def on_start(self):
        """Llamado cuando un usuario inicia sesión"""
        self.setup_test_data()
    
    def setup_test_data(self):
        """Configurar datos de prueba"""
        self.user_id = random.randint(1, 5)  # Usuarios existentes en la BD
        self.product_ids = [1, 2, 3, 4]  # Productos existentes
        self.category_ids = [1, 2, 3]    # Categorías existentes
        self.order_ids = [1, 2, 3, 4]    # Órdenes existentes
        self.cart_ids = [1, 2, 3, 4]     # Carritos existentes
        self.payment_ids = [1, 2, 3, 4]  # Pagos existentes
    
    @task(4)
    def browse_products(self):
        """Simular navegación por productos"""
        # Ver lista de productos
        self.client.get("/product-service/api/products", name="products_list")
        
        # Ver categorías
        self.client.get("/product-service/api/categories", name="categories_list")
        
        # Ver producto específico
        product_id = random.choice(self.product_ids)
        self.client.get(f"/product-service/api/products/{product_id}", name="product_detail")
    
    @task(3)
    def manage_favourites(self):
        """Simular gestión de favoritos"""
        # Ver todos los favoritos
        self.client.get("/favourite-service/api/favourites", name="favourites_list")
        
        # Crear favorito
        current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
        favourite_data = {
            "userId": random.choice([1, 2, 3, 4, 5]),
            "productId": random.choice(self.product_ids),
            "likeDate": current_time
        }
        
        self.client.post("/favourite-service/api/favourites", 
                        json=favourite_data, 
                        name="create_favourite")
    
    @task(2)
    def manage_orders(self):
        """Simular gestión de órdenes"""
        # Ver todas las órdenes
        self.client.get("/order-service/api/orders", name="orders_list")
        
        # Ver orden específica
        order_id = random.choice(self.order_ids)
        self.client.get(f"/order-service/api/orders/{order_id}", name="order_detail")
        
        # Crear carrito
        cart_data = {"userId": random.choice([1, 2, 3, 4, 5])}
        self.client.post("/order-service/api/carts", json=cart_data, name="create_cart")
    
    @task(2)
    def manage_payments(self):
        """Simular gestión de pagos"""
        # Ver todos los pagos
        self.client.get("/payment-service/api/payments", name="payments_list")
        
        # Ver pago específico
        payment_id = random.choice(self.payment_ids)
        self.client.get(f"/payment-service/api/payments/{payment_id}", name="payment_detail")
        
        # Crear pago
        payment_data = {
            "isPayed": True,
            "paymentStatus": "COMPLETED",
            "order": {
                "orderId": random.choice(self.order_ids)
            }
        }
        
        self.client.post("/payment-service/api/payments", 
                        json=payment_data, 
                        name="create_payment")
    
    @task(1)
    def manage_users(self):
        """Simular gestión de usuarios"""
        # Ver todos los usuarios
        self.client.get("/user-service/api/users", name="users_list")
        
        # Ver usuario específico
        user_id = random.choice([1, 2, 3, 4, 5])
        self.client.get(f"/user-service/api/users/{user_id}", name="user_detail")

class ProductServiceUser(HttpUser):
    """Usuario especializado para pruebas intensivas del servicio de productos"""
    wait_time = between(1, 3)
    
    def on_start(self):
        self.product_ids = [1, 2, 3, 4]
        self.category_ids = [1, 2, 3]
    
    @task(6)
    def intensive_product_browsing(self):
        """Navegación intensiva de productos"""
        # Lista de productos
        self.client.get("/product-service/api/products", name="products_intensive")
        
        # Múltiples productos específicos
        for _ in range(random.randint(2, 5)):
            product_id = random.choice(self.product_ids)
            self.client.get(f"/product-service/api/products/{product_id}", name="product_detail_intensive")
    
    @task(4)
    def category_operations(self):
        """Operaciones intensivas de categorías"""
        # Lista de categorías
        self.client.get("/product-service/api/categories", name="categories_intensive")
        
        # Categorías específicas
        for category_id in self.category_ids:
            self.client.get(f"/product-service/api/categories/{category_id}", name="category_detail_intensive")

class OrderServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de órdenes"""
    wait_time = between(1, 4)
    
    def on_start(self):
        self.order_ids = [1, 2, 3, 4]
        self.cart_ids = [1, 2, 3, 4]
        self.user_ids = [1, 2, 3, 4, 5]
    
    @task(4)
    def order_operations(self):
        """Operaciones intensivas de órdenes"""
        # Ver todas las órdenes
        self.client.get("/order-service/api/orders", name="orders_intensive")
        
        # Múltiples órdenes específicas
        for _ in range(random.randint(2, 4)):
            order_id = random.choice(self.order_ids)
            self.client.get(f"/order-service/api/orders/{order_id}", name="order_detail_intensive")
    
    @task(3)
    def cart_operations(self):
        """Operaciones intensivas de carritos"""
        # Ver todos los carritos
        self.client.get("/order-service/api/carts", name="carts_intensive")
        
        # Crear múltiples carritos
        for _ in range(random.randint(1, 2)):
            cart_data = {"userId": random.choice(self.user_ids)}
            self.client.post("/order-service/api/carts", json=cart_data, name="create_cart_intensive")

class PaymentServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de pagos"""
    wait_time = between(2, 5)
    
    def on_start(self):
        self.payment_ids = [1, 2, 3, 4]
        self.order_ids = [1, 2, 3, 4]
    
    @task(5)
    def payment_processing(self):
        """Procesamiento intensivo de pagos"""
        # Ver todos los pagos
        self.client.get("/payment-service/api/payments", name="payments_intensive")
        
        # Pagos específicos
        for _ in range(random.randint(2, 4)):
            payment_id = random.choice(self.payment_ids)
            self.client.get(f"/payment-service/api/payments/{payment_id}", name="payment_detail_intensive")
    
    @task(3)
    def create_payments(self):
        """Crear múltiples pagos"""
        for _ in range(random.randint(1, 3)):
            payment_data = {
                "isPayed": random.choice([True, False]),
                "paymentStatus": random.choice(["PENDING", "COMPLETED", "FAILED"]),
                "order": {
                    "orderId": random.choice(self.order_ids)
                }
            }
            
            self.client.post("/payment-service/api/payments", 
                           json=payment_data, 
                           name="create_payment_intensive")

class FavouriteServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de favoritos"""
    wait_time = between(1, 3)
    
    def on_start(self):
        self.user_ids = [1, 2, 3, 4, 5]
        self.product_ids = [1, 2, 3, 4]
    
    @task(5)
    def favourite_operations(self):
        """Operaciones intensivas de favoritos"""
        # Ver todos los favoritos
        self.client.get("/favourite-service/api/favourites", name="favourites_intensive")
        
        # Favoritos específicos
        for _ in range(random.randint(2, 5)):
            favourite_id = random.randint(1, 20)
            self.client.get(f"/favourite-service/api/favourites/{favourite_id}", name="favourite_detail_intensive")
    
    @task(3)
    def create_favourites(self):
        """Crear múltiples favoritos"""
        for _ in range(random.randint(1, 3)):
            current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
            favourite_data = {
                "userId": random.choice(self.user_ids),
                "productId": random.choice(self.product_ids),
                "likeDate": current_time
            }
            
            self.client.post("/favourite-service/api/favourites", 
                           json=favourite_data, 
                           name="create_favourite_intensive")

class UserServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de usuarios"""
    wait_time = between(2, 4)
    
    def on_start(self):
        self.user_ids = [1, 2, 3, 4, 5]
    
    @task(6)
    def user_operations(self):
        """Operaciones intensivas de usuarios"""
        # Ver todos los usuarios
        self.client.get("/user-service/api/users", name="users_intensive")
        
        # Usuarios específicos
        for _ in range(random.randint(2, 4)):
            user_id = random.choice(self.user_ids)
            self.client.get(f"/user-service/api/users/{user_id}", name="user_detail_intensive")

class HealthCheckUser(HttpUser):
    """Usuario para monitorear health checks de todos los servicios"""
    wait_time = between(5, 10)
    
    @task(1)
    def check_all_services(self):
        """Verificar salud de todos los microservicios"""
        services = [
            "user-service",
            "product-service", 
            "order-service",
            "payment-service",
            "favourite-service"
        ]
        
        for service in services:
            # Health check endpoint
            self.client.get(f"/{service}/actuator/health", name=f"{service}_health")

# Configuración para diferentes tipos de carga
class LightLoadUser(EcommerceUser):
    """Usuario para carga ligera"""
    wait_time = between(3, 8)
    weight = 1

class HeavyLoadUser(EcommerceUser):
    """Usuario para carga pesada"""
    wait_time = between(0.5, 2)
    weight = 3

class SpikeLoadUser(EcommerceUser):
    """Usuario para picos de carga"""
    wait_time = between(0.1, 1)
    weight = 5

class MobileAppUser(HttpUser):
    """Usuario simulando comportamiento de aplicación móvil"""
    wait_time = between(0.5, 2)
    
    def on_start(self):
        self.product_ids = [1, 2, 3, 4]
        self.user_ids = [1, 2, 3, 4, 5]
    
    @task(4)
    def mobile_product_browsing(self):
        """Navegación rápida típica de móvil"""
        headers = {"User-Agent": "MobileApp/1.0"}
        
        # Búsqueda rápida de productos
        self.client.get("/product-service/api/products", headers=headers, name="mobile_products")
        
        # Ver producto específico
        product_id = random.choice(self.product_ids)
        self.client.get(f"/product-service/api/products/{product_id}", 
                       headers=headers, name="mobile_product_detail")
    
    @task(2)
    def mobile_favourites(self):
        """Gestión rápida de favoritos en móvil"""
        headers = {"User-Agent": "MobileApp/1.0"}
        
        # Ver favoritos
        self.client.get("/favourite-service/api/favourites", headers=headers, name="mobile_favourites")
        
        # Crear favorito rápido
        current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
        favourite_data = {
            "userId": random.choice(self.user_ids),
            "productId": random.choice(self.product_ids),
            "likeDate": current_time
        }
        
        self.client.post("/favourite-service/api/favourites", 
                        json=favourite_data, 
                        headers=headers,
                        name="mobile_create_favourite")

if __name__ == "__main__":
    print("🚀 Ecommerce Load Test Suite")
    print("=" * 50)
    print("Available user classes:")
    print("- EcommerceUser: General e-commerce operations")
    print("- ProductServiceUser: Product browsing and search")
    print("- OrderServiceUser: Order management operations")
    print("- PaymentServiceUser: Payment processing")
    print("- FavouriteServiceUser: Favourite management")
    print("- UserServiceUser: User management")
    print("- HealthCheckUser: Service health monitoring")
    print("- MobileAppUser: Mobile app simulation")
    print()
    print("Example usage:")
    print("# Carga normal")
    print("locust -f ecommerce_load_test.py --headless -u 100 -r 10 --run-time 15m --host http://localhost:9081")
    print()
    print("# Prueba de estrés")
    print("locust -f ecommerce_load_test.py --headless -u 200 -r 20 --run-time 20m --host http://localhost:9081")
    print()
    print("# Prueba de picos")
    print("locust -f ecommerce_load_test.py --headless -u 500 -r 50 --run-time 10m --host http://localhost:9081") 