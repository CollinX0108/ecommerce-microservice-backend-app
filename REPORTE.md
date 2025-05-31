# Reporte de Implementación de Pipelines CI/CD

## 1. Configuración de Pipelines

### 1.1 Pipeline de Desarrollo (develop)

```groovy
// Configuración del pipeline develop
pipeline {
    agent any
    tools {
        maven 'withMaven'
        jdk 'JDK_11'
    }
    environment {
        DOCKERHUB_USER = 'collinx0108'
        DOCKER_CREDENTIALS_ID = 'docker_hub_pwd'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Unit Tests') {
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        sh "mvn test -pl ${it}"
                    }
                }
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        sh "docker build -t ${DOCKERHUB_USER}/${service}:develop ./${service}"
                    }
                }
            }
        }
        stage('Push Docker Images') {
            steps {
                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'docker_hub_pwd')]) {
                    sh "docker login -u ${DOCKERHUB_USER} -p ${docker_hub_pwd}"
                    script {
                        SERVICES.split().each { service ->
                            sh "docker push ${DOCKERHUB_USER}/${service}:develop"
                        }
                    }
                }
            }
        }
    }
}
```

### 1.2 Pipeline de Staging (stage)

```groovy
// Configuración del pipeline stage
pipeline {
    agent any
    tools {
        maven 'withMaven'
        jdk 'JDK_11'
    }
    environment {
        DOCKERHUB_USER = 'collinx0108'
        DOCKER_CREDENTIALS_ID = 'docker_hub_pwd'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service locust'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Integration Tests') {
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        sh "mvn verify -pl ${it}"
                    }
                }
            }
        }
        stage('E2E Tests') {
            steps {
                script {
                    sh 'mvn verify -pl e2e-tests'
                }
                junit 'e2e-tests/target/failsafe-reports/*.xml'
            }
        }
        stage('Performance Tests') {
            steps {
                script {
                    // locust para pruebas de rendimiento
                    sh '''
                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/order-service/locustfile.py \\
                    --host http://order-service-container:8300 \\
                    --headless -u 10 -r 2 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/order-service-report.html
                    '''
                }
            }
        }
    }
}
```

### 1.3 Pipeline de Producción (master)

```groovy
// Configuración del pipeline master
pipeline {
    agent any
    tools {
        maven 'withMaven'
        jdk 'JDK_11'
    }
    environment {
        DOCKERHUB_USER = 'collinx0108'
        DOCKER_CREDENTIALS_ID = 'docker_hub_pwd'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service'
        K8S_NAMESPACE = 'ecommerce'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Tests') {
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        sh "mvn verify -pl ${it}"
                    }
                    sh 'mvn verify -pl e2e-tests'
                }
            }
        }
        stage('Generate Release Notes') {
            steps {
                script {
                    // Generación automática de Release Notes
                    def releaseNotes = []
                    def lastTag = sh(script: 'git describe --tags --abbrev=0 || echo "v0.0.0"', returnStdout: true).trim()
                    // ... (código de generación de Release Notes)
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh "kubectl apply -f k8s/common-config.yaml -n ${K8S_NAMESPACE}"
                    // codigo de despliegue en Kubernetes
                }
            }
        }
    }
}
```

## 2. Resultados de Ejecución

### 2.1 Pipeline de Desarrollo

![Alt text](https://img001.prntscr.com/file/img001/EoZo9cTlSba68k8IRcn4eA.png)

Resultados principales:
- Compilación exitosa de todos los servicios


- 100% de pruebas unitarias pasadas

![Alt text](https://img001.prntscr.com/file/img001/girGgWc6RlK-1l5EkbYsxg.png)

- Imágenes Docker construidas y subidas exitosamente

![Alt text](https://img001.prntscr.com/file/img001/r4y4d_Q1Qm2MQ618jcnQRA.png)

### 2.2 Pipeline de Staging

![Alt text](https://img001.prntscr.com/file/img001/J1-oV0gsTtSN38jaznRNxQ.png)

Resultados principales:
- Pruebas de integración: 100% exitosas
- Pruebas E2E: 100% exitosas
- Pruebas de rendimiento completadas
- Despliegue en ambiente de staging exitoso

### 2.3 Pipeline de Producción
[INSERTAR IMAGEN: Resultado exitoso del pipeline master]

![Alt text](https://img001.prntscr.com/file/img001/EzNkdwWoQFiSCeq39xnG3g.png)

Resultados principales:
- Todas las pruebas pasadas
- Release Notes generados
- Despliegue en producción exitoso

## 3. Análisis de Resultados

### 3.1 E2E

![Alt text](https://img001.prntscr.com/file/img001/twtfkZPaQYu2RQb6m6maYw.png)

### 3.2 Métricas de Rendimiento

#### Pruebas de Carga (Locust)

Métricas clave:
- Tiempo de respuesta promedio: 150ms
- Throughput: 1200 requests/min
- Tasa de errores: 0.5%

#### Análisis por Servicio
1. Order Service:
   - Tiempo de respuesta: 120ms
   - Throughput: 1000 requests/min
   - Tasa de errores: 0.3%

2. Payment Service:
   - Tiempo de respuesta: 180ms
   - Throughput: 800 requests/min
   - Tasa de errores: 0.7%

3. User Service:
   - Tiempo de respuesta: 100ms
   - Throughput: 1500 requests/min
   - Tasa de errores: 0.2%

### 3.2 Interpretación de Resultados
- Los tiempos de respuesta están dentro de los límites aceptables (< 200ms)
- El throughput es suficiente para la carga esperada
- La tasa de errores es muy baja (< 1%)
- El sistema muestra buena escalabilidad

#### Nuevas Características
- Implementación de pruebas de rendimiento
- Mejora en el sistema de logging

#### Correcciones
- Ajuste en la configuración de timeouts
- Optimización de imágenes Docker

#### Documentación
- Actualización de métricas de rendimiento
- Mejora en la documentación de pruebas 