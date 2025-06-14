from locust import HttpUser, task, between
import random
from datetime import datetime

class FavouriteServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de favoritos"""
    wait_time = between(1, 3)
    
    def on_start(self):
        """Configurar datos de prueba"""
        self.user_ids = [1, 2, 3, 4, 5]  # IDs de usuarios existentes
        self.product_ids = [1, 2, 3, 4]  # IDs de productos existentes
        self.favourite_ids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]  # IDs de favoritos existentes
    
    @task(5)
    def get_all_favourites(self):
        """Obtener lista de todos los favoritos"""
        with self.client.get("/favourite-service/api/favourites", 
                           catch_response=True, 
                           name="GET /api/favourites") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(4)
    def get_favourite_by_id(self):
        """Obtener favorito específico por ID"""
        favourite_id = random.choice(self.favourite_ids)
        with self.client.get(f"/favourite-service/api/favourites/{favourite_id}", 
                           catch_response=True, 
                           name="GET /api/favourites/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(3)
    def create_favourite(self):
        """Crear nuevo favorito"""
        current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
        favourite_data = {
            "userId": random.choice(self.user_ids),
            "productId": random.choice(self.product_ids),
            "likeDate": current_time
        }
        
        with self.client.post("/favourite-service/api/favourites", 
                            json=favourite_data,
                            catch_response=True, 
                            name="POST /api/favourites") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(2)
    def update_favourite(self):
        """Actualizar favorito existente"""
        favourite_id = random.choice(self.favourite_ids)
        current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
        favourite_data = {
            "userId": random.choice(self.user_ids),
            "productId": random.choice(self.product_ids),
            "likeDate": current_time
        }
        
        with self.client.put(f"/favourite-service/api/favourites/{favourite_id}", 
                           json=favourite_data,
                           catch_response=True, 
                           name="PUT /api/favourites/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(1)
    def delete_favourite(self):
        """Eliminar favorito (simulación)"""
        favourite_id = random.choice(self.favourite_ids)
        with self.client.delete(f"/favourite-service/api/favourites/{favourite_id}", 
                              catch_response=True, 
                              name="DELETE /api/favourites/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")

class FavouriteServiceStressUser(HttpUser):
    """Usuario para pruebas de estrés del servicio de favoritos"""
    wait_time = between(0.5, 1.5)
    
    def on_start(self):
        self.user_ids = [1, 2, 3, 4, 5]
        self.product_ids = [1, 2, 3, 4]
        self.favourite_ids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    
    @task(8)
    def rapid_favourite_queries(self):
        """Consultas rápidas e intensivas de favoritos"""
        # Múltiples consultas rápidas
        for _ in range(random.randint(3, 6)):
            favourite_id = random.choice(self.favourite_ids)
            self.client.get(f"/favourite-service/api/favourites/{favourite_id}", 
                          name="STRESS_favourite_detail")
    
    @task(5)
    def concurrent_favourite_list(self):
        """Consultas concurrentes de lista de favoritos"""
        self.client.get("/favourite-service/api/favourites", name="STRESS_favourites_list")
    
    @task(3)
    def bulk_favourite_creation(self):
        """Creación masiva de favoritos para pruebas de estrés"""
        for _ in range(random.randint(2, 5)):
            current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
            favourite_data = {
                "userId": random.choice(self.user_ids),
                "productId": random.choice(self.product_ids),
                "likeDate": current_time
            }
            
            self.client.post("/favourite-service/api/favourites", 
                           json=favourite_data,
                           name="STRESS_create_favourite")

class FavouriteServiceEnduranceUser(HttpUser):
    """Usuario para pruebas de resistencia del servicio de favoritos"""
    wait_time = between(2, 4)
    
    def on_start(self):
        self.user_ids = [1, 2, 3, 4, 5]
        self.product_ids = [1, 2, 3, 4]
        self.favourite_ids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    
    @task(6)
    def endurance_browsing(self):
        """Navegación prolongada de favoritos"""
        # Simular sesión larga de navegación
        self.client.get("/favourite-service/api/favourites", name="ENDURANCE_favourites_list")
        
        # Ver varios favoritos específicos
        for _ in range(random.randint(2, 4)):
            favourite_id = random.choice(self.favourite_ids)
            self.client.get(f"/favourite-service/api/favourites/{favourite_id}", 
                          name="ENDURANCE_favourite_detail")
    
    @task(2)
    def endurance_operations(self):
        """Operaciones prolongadas de favoritos"""
        # Crear favorito
        current_time = datetime.now().strftime("dd-MM-yyyy__HH:mm:ss:SSSSSS")
        favourite_data = {
            "userId": random.choice(self.user_ids),
            "productId": random.choice(self.product_ids),
            "likeDate": current_time
        }
        
        self.client.post("/favourite-service/api/favourites", 
                       json=favourite_data,
                       name="ENDURANCE_create_favourite")

if __name__ == "__main__":
    print("❤️ Favourite Service Load Test")
    print("Available user classes:")
    print("- FavouriteServiceUser: Normal favourite service operations")
    print("- FavouriteServiceStressUser: Stress testing for favourite service")
    print("- FavouriteServiceEnduranceUser: Endurance testing for favourite service")
    print()
    print("Example usage:")
    print("locust -f locustfile.py --headless -u 40 -r 4 --run-time 10m --host http://localhost:8085") 