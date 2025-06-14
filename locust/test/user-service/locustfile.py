from locust import HttpUser, task, between
import random

class UserServiceUser(HttpUser):
    """Usuario especializado para pruebas del servicio de usuarios"""
    wait_time = between(1, 3)
    
    def on_start(self):
        """Configurar datos de prueba"""
        self.user_ids = [1, 2, 3, 4, 5]  # IDs de usuarios existentes
    
    @task(6)
    def get_all_users(self):
        """Obtener lista de todos los usuarios"""
        with self.client.get("/user-service/api/users", 
                           catch_response=True, 
                           name="GET /api/users") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(4)
    def get_user_by_id(self):
        """Obtener usuario específico por ID"""
        user_id = random.choice(self.user_ids)
        with self.client.get(f"/user-service/api/users/{user_id}", 
                           catch_response=True, 
                           name="GET /api/users/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(2)
    def create_user(self):
        """Crear nuevo usuario (simulación)"""
        user_data = {
            "firstName": f"TestUser{random.randint(1000, 9999)}",
            "lastName": "LoadTest",
            "imageUrl": "https://example.com/avatar.jpg",
            "email": f"test{random.randint(1000, 9999)}@loadtest.com",
            "phone": f"+1-555-{random.randint(1000, 9999)}"
        }
        
        with self.client.post("/user-service/api/users", 
                            json=user_data,
                            catch_response=True, 
                            name="POST /api/users") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
    
    @task(1)
    def update_user(self):
        """Actualizar usuario existente (simulación)"""
        user_id = random.choice(self.user_ids)
        user_data = {
            "firstName": f"UpdatedUser{random.randint(1000, 9999)}",
            "lastName": "LoadTestUpdated",
            "imageUrl": "https://example.com/updated-avatar.jpg",
            "email": f"updated{random.randint(1000, 9999)}@loadtest.com",
            "phone": f"+1-555-{random.randint(1000, 9999)}"
        }
        
        with self.client.put(f"/user-service/api/users/{user_id}", 
                           json=user_data,
                           catch_response=True, 
                           name="PUT /api/users/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")

class UserServiceStressUser(HttpUser):
    """Usuario para pruebas de estrés del servicio de usuarios"""
    wait_time = between(0.5, 1.5)
    
    def on_start(self):
        self.user_ids = [1, 2, 3, 4, 5]
    
    @task(8)
    def rapid_user_queries(self):
        """Consultas rápidas e intensivas de usuarios"""
        # Múltiples consultas rápidas
        for _ in range(random.randint(3, 6)):
            user_id = random.choice(self.user_ids)
            self.client.get(f"/user-service/api/users/{user_id}", 
                          name="STRESS_user_detail")
    
    @task(5)
    def concurrent_user_list(self):
        """Consultas concurrentes de lista de usuarios"""
        self.client.get("/user-service/api/users", name="STRESS_users_list")
    
    @task(2)
    def bulk_user_creation(self):
        """Creación masiva de usuarios para pruebas de estrés"""
        for _ in range(random.randint(2, 4)):
            user_data = {
                "firstName": f"StressUser{random.randint(10000, 99999)}",
                "lastName": "StressTest",
                "imageUrl": "https://example.com/stress-avatar.jpg",
                "email": f"stress{random.randint(10000, 99999)}@loadtest.com",
                "phone": f"+1-555-{random.randint(1000, 9999)}"
            }
            
            self.client.post("/user-service/api/users", 
                           json=user_data,
                           name="STRESS_create_user")

if __name__ == "__main__":
    print("👥 User Service Load Test")
    print("Available user classes:")
    print("- UserServiceUser: Normal user service operations")
    print("- UserServiceStressUser: Stress testing for user service")
    print()
    print("Example usage:")
    print("locust -f locustfile.py --headless -u 30 -r 3 --run-time 10m --host http://localhost:8080") 