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
        K8S_NAMESPACE = 'default'
    }

    stages {
        stage('Init') {
            steps {
                script {
                    echo "Detected branch: ${env.BRANCH_NAME}"
                    def profileConfig = [
                        master : ['prod', '-prod'],
                        stage: ['stage', '-stage'],
                        develop: ['dev', '-dev']
                    ]
                    def config = profileConfig.get(env.BRANCH_NAME, ['dev', '-dev'])

                    env.SPRING_PROFILES_ACTIVE = config[0]
                    env.IMAGE_TAG = config[0]
                    env.DEPLOYMENT_SUFFIX = config[1]

                    env.IS_MASTER = env.BRANCH_NAME == 'master' ? 'true' : 'false'
                    env.IS_STAGE = env.BRANCH_NAME == 'stage' ? 'true' : 'false'
                    env.IS_DEVELOP = env.BRANCH_NAME == 'develop' ? 'true' : 'false'
                    env.IS_FEATURE = env.BRANCH_NAME.startsWith('feature/') ? 'true' : 'false'

                    echo "Spring profile: ${env.SPRING_PROFILES_ACTIVE}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                    echo "Deployment suffix: ${env.DEPLOYMENT_SUFFIX}"
                    echo "Flags: IS_MASTER=${env.IS_MASTER}, IS_STAGE=${env.IS_STAGE}, IS_DEVELOP=${env.IS_DEVELOP}, IS_FEATURE=${env.IS_FEATURE}"
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/collinx0108/ecommerce-microservice-backend-app'
            }
        }

        stage('Verify Tools') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker --version'
                sh 'kubectl config current-context'
            }
        }

        stage('Build Services') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'stage'
                    branch 'master'
                }
            }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Images of each service') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'stage'
                    branch 'master'
                }
            }
            steps {
                script {
                    SERVICES.split().each { service ->
                        sh "docker buildx build --platform linux/amd64,linux/arm64 -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} --build-arg SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} --push ./${service}"
                    }
                }
            }
        }

//        stage('Push Docker Images') {
//            when {
//                anyOf {
//                    branch 'develop'
//                    branch 'stage'
//                    branch 'master'
//                }
//            }
//            steps {
//                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'docker_hub_pwd')]) {
//                    sh "docker login -u ${DOCKERHUB_USER} -p ${docker_hub_pwd}"
//                    script {
//                        def maxRetries = 3
//                        def retryDelay = 30

//                        SERVICES.split().each { service ->
//                            def retryCount = 0
//                            def success = false
                            
//                            while (!success && retryCount < maxRetries) {
//                                try {
//                                    echo "Intentando push de ${service} (intento ${retryCount + 1}/${maxRetries})"
//                                    sh "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
//                                    success = true
//                                    echo "✅ Push exitoso para ${service}"
//                                } catch (Exception e) {
//                                    retryCount++
//                                    if (retryCount < maxRetries) {
//                                        echo "❌ Error en push de ${service}, reintentando en ${retryDelay} segundos..."
//                                        sleep retryDelay
//                                    } else {
//                                        echo "❌ Error en push de ${service} después de ${maxRetries} intentos"
//                                        error "No se pudo hacer push de la imagen ${service} después de ${maxRetries} intentos"
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        stage('Unit Tests') {
            when { branch 'develop' }
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        sh "mvn test -pl ${it}"
                    }
                }
            }
        }

        stage('Integration Tests') {
            when { branch 'stage' }
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        sh "mvn verify -pl ${it}"
                    }
                }
            }
        }

        stage('E2E Tests') {
            when { branch 'stage' }
            steps {
                script {
                    sh 'mvn verify -pl e2e-tests'
                }
                junit 'e2e-tests/target/failsafe-reports/*.xml'
            }
        }

        stage('Start containers for load and stress testing') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''
                    docker network create ecommerce-test || true

                    docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    ${DOCKERHUB_USER}/service-discovery:${IMAGE_TAG}

                    until curl -s http://localhost:8761/actuator/health | grep '"status":"UP"' > /dev/null; do
                        echo "Waiting for service discovery to be ready..."
                        sleep 10
                    done

                    docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ \\
                    -e EUREKA_INSTANCE=cloud-config-container \\
                    ${DOCKERHUB_USER}/cloud-config:${IMAGE_TAG}

                    until curl -s http://localhost:9296/actuator/health | grep '"status":"UP"' > /dev/null; do
                        echo "Waiting for cloud config to be ready..."
                        sleep 10
                    done

                    docker run -d --name order-service-container --network ecommerce-test -p 8300:8300 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=order-service-container \\
                    ${DOCKERHUB_USER}/order-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8300/order-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for order service to be ready..."
                        sleep 10
                    done

                    docker run -d --name payment-service-container --network ecommerce-test -p 8400:8400 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=payment-service-container \\
                    ${DOCKERHUB_USER}/payment-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8400/payment-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for payment service to be ready..."
                        sleep 10
                    done

                    docker run -d --name product-service-container --network ecommerce-test -p 8500:8500 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=product-service-container \\
                    ${DOCKERHUB_USER}/product-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8500/product-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for product service to be ready..."
                        sleep 10
                    done

                    docker run -d --name shipping-service-container --network ecommerce-test -p 8600:8600 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=shipping-service-container \\
                    ${DOCKERHUB_USER}/shipping-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8600/shipping-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for shipping service to be ready..."
                        sleep 10
                    done

                    docker run -d --name user-service-container --network ecommerce-test -p 8700:8700 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=user-service-container \\
                    ${DOCKERHUB_USER}/user-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8700/user-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for user service to be ready..."
                        sleep 10
                    done

                    docker run -d --name favourite-service-container --network ecommerce-test -p 8800:8800 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=favourite-service-container \\
                    ${DOCKERHUB_USER}/favourite-service:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8800/favourite-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for favourite service to be ready..."
                        sleep 10
                    done

                    docker run -d --name api-gateway-container --network ecommerce-test -p 8900:8900 \\
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=api-gateway-container \\
                    ${DOCKERHUB_USER}/api-gateway:${IMAGE_TAG}

                    until [ "$(curl -s http://localhost:8900/api-gateway/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for API gateway to be ready..."
                        sleep 10
                    done
                    '''
                }
            }
        }

        stage('Run Load Tests with Locust') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''
                    mkdir -p locust-reports

                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/order-service/locustfile.py \\
                    --host http://order-service-container:8300 \\
                    --headless -u 10 -r 2 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/order-service-report.html

                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/payment-service/locustfile.py \\
                    --host http://payment-service-container:8400 \\
                    --headless -u 10 -r 1 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/payment-service-report.html

                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/favourite-service/locustfile.py \\
                    --host http://favourite-service-container:8800 \\
                    --headless -u 10 -r 2 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/favourite-service-report.html
                    '''
                }
            }
        }

        stage('Run Stress Tests with Locust') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''
                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/order-service/locustfile.py \\
                    --host http://order-service-container:8300 \\
                    --headless -u 50 -r 5 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/order-service-stress-report.html

                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/payment-service/locustfile.py \\
                    --host http://payment-service-container:8400 \\
                    --headless -u 50 -r 5 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/payment-service-stress-report.html

                    docker run --rm --network ecommerce-test \\
                    -v $PWD/locust-reports:/mnt/locust \\
                    ${DOCKERHUB_USER}/locust:${IMAGE_TAG} \\
                    -f test/favourite-service/locustfile.py \\
                    --host http://favourite-service-container:8800 \\
                    --headless -u 50 -r 5 -t 1m \\
                    --only-summary \\
                    --html /mnt/locust/favourite-service-stress-report.html

                    echo "✅ Pruebas de estrés completadas"
                    '''
                }
            }
        }

        stage('Stop and Remove Containers') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''                    
                    docker rm -f locust || true
                    docker rm -f favourite-service-container || true
                    docker rm -f user-service-container || true
                    docker rm -f shipping-service-container || true
                    docker rm -f product-service-container || true
                    docker rm -f payment-service-container || true
                    docker rm -f order-service-container || true
                    docker rm -f cloud-config-container || true
                    docker rm -f service-discovery-container || true
                    docker rm -f zipkin-container || true

                    docker network rm ecommerce-test || true
                    '''
                }
            }
        }

        stage('Deploy Common Config') {
            steps {
                sh "kubectl apply -f k8s/common-config.yaml -n ${K8S_NAMESPACE}"
            }
        }

        stage('Deploy Core Services') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'master'
                }
            }
            steps {
                script {
                    // Configurar el contexto de Kubernetes según la rama
                    if (env.BRANCH_NAME == 'develop') {
                        sh 'az aks get-credentials --name aks-develop-ecommerce --resource-group rg-develop-ecommerce'
                    } else if (env.BRANCH_NAME == 'master') {
                        sh 'az aks get-credentials --name aks-master-ecommerce --resource-group rg-master-ecommerce'
                    }

                    sh "kubectl apply -f k8s/zipkin/ -n ${K8S_NAMESPACE}"
                    sh "kubectl rollout status deployment/zipkin -n ${K8S_NAMESPACE} --timeout=200s"

                    sh "kubectl delete -f k8s/service-discovery/ -n ${K8S_NAMESPACE} --ignore-not-found=true"
                    sh "sleep 30"
                    sh "kubectl get pods -n ${K8S_NAMESPACE}"
                    sh "kubectl get rs -n ${K8S_NAMESPACE}"
                    sh "kubectl apply -f k8s/service-discovery/ -n ${K8S_NAMESPACE}"
                    sh "kubectl get pods -n ${K8S_NAMESPACE}"
                    sh "kubectl get rs -n ${K8S_NAMESPACE}"
                    sh "kubectl set image deployment/service-discovery service-discovery=${DOCKERHUB_USER}/service-discovery:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                    sh "kubectl set env deployment/service-discovery SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                    sh "kubectl rollout status deployment/service-discovery -n ${K8S_NAMESPACE} --timeout=400s"

                    sh "kubectl apply -f k8s/cloud-config/ -n ${K8S_NAMESPACE}"
                    sh "kubectl set image deployment/cloud-config cloud-config=${DOCKERHUB_USER}/cloud-config:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                    sh "kubectl set env deployment/cloud-config SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                    sh "kubectl rollout status deployment/cloud-config -n ${K8S_NAMESPACE} --timeout=300s"
                }
            }
        }

        stage('Deploy Microservices') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'master'
                }
            }
            steps {
                script {
                    // Configurar el contexto de Kubernetes según la rama
                    if (env.BRANCH_NAME == 'develop') {
                        sh 'az aks get-credentials --name aks-develop-ecommerce --resource-group rg-develop-ecommerce'
                    } else if (env.BRANCH_NAME == 'master') {
                        sh 'az aks get-credentials --name aks-master-ecommerce --resource-group rg-master-ecommerce'
                    }

                    def appServices = ['user-service', 'product-service', 'order-service', 'payment-service', 'shipping-service', 'favourite-service', 'api-gateway', 'proxy-client']

                    for (svc in appServices) {
                        def image = "${DOCKERHUB_USER}/${svc}:${IMAGE_TAG}"

                        sh "kubectl apply -f k8s/${svc}/ -n ${K8S_NAMESPACE}"
                        sh "kubectl set image deployment/${svc} ${svc}=${image} -n ${K8S_NAMESPACE}"
                        sh "kubectl set env deployment/${svc} SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                        sh "kubectl rollout status deployment/${svc} -n ${K8S_NAMESPACE} --timeout=200s"
                    }
                }
            }
        }

        stage('Generate Release Notes') {
            when { branch 'master' }
            steps {
                script {
                    def releaseNotes = []
                    def lastTag = sh(script: 'git describe --tags --abbrev=0 || echo "v0.0.0"', returnStdout: true).trim()
                    
                    // Obtener commits desde el último tag
                    def commits = sh(script: "git log ${lastTag}..HEAD --pretty=format:'%h|%s|%an'", returnStdout: true).trim().split('\n')
                    
                    // Categorizar commits
                    def features = []
                    def fixes = []
                    def docs = []
                    def others = []
                    
                    commits.each { commit ->
                        def (hash, message, author) = commit.split('\\|')
                        if (message.toLowerCase().contains('feat:')) {
                            features << "- ${message} (${hash})"
                        } else if (message.toLowerCase().contains('fix:')) {
                            fixes << "- ${message} (${hash})"
                        } else if (message.toLowerCase().contains('docs:')) {
                            docs << "- ${message} (${hash})"
                        } else {
                            others << "- ${message} (${hash})"
                        }
                    }
                    
                    // Generar Release Notes
                    def version = sh(script: 'git describe --tags --abbrev=0 || echo "v0.0.0"', returnStdout: true).trim()
                    def newVersion = version.replace('v', '').split('\\.')
                    newVersion[2] = (newVersion[2].toInteger() + 1).toString()
                    newVersion = "v${newVersion.join('.')}"
                    
                    def releaseNotesContent = """
# Release Notes - ${newVersion}

## Cambios desde ${version}

${features.size() > 0 ? '### Nuevas Características\n' + features.join('\n') + '\n' : ''}
${fixes.size() > 0 ? '### Correcciones\n' + fixes.join('\n') + '\n' : ''}
${docs.size() > 0 ? '### Documentación\n' + docs.join('\n') + '\n' : ''}
${others.size() > 0 ? '### Otros Cambios\n' + others.join('\n') + '\n' : ''}

## Detalles Técnicos
- Branch: ${env.BRANCH_NAME}
- Commit: ${env.GIT_COMMIT}
- Build: ${env.BUILD_NUMBER}
- Fecha: ${new Date().format("yyyy-MM-dd HH:mm:ss")}
"""
                    
                    // Guardar Release Notes
                    writeFile file: 'RELEASE_NOTES.md', text: releaseNotesContent
                    
                    // Crear nuevo tag
                    sh "git tag -a ${newVersion} -m 'Release ${newVersion}'"
                    sh "git push origin ${newVersion}"
                    
                    // Publicar Release Notes como artefacto
                    archiveArtifacts artifacts: 'RELEASE_NOTES.md', fingerprint: true
                }
            }
        }
    }

    post {
        success {
            script {
                echo "✅ Pipeline completed successfully for ${env.BRANCH_NAME} branch."
                echo "📊 Environment: ${env.SPRING_PROFILES_ACTIVE}"

                if (env.BRANCH_NAME == 'master') {
                    echo '🚀 Production deployment completed successfully!'
                } else if (env.BRANCH_NAME == 'stage') {
                    echo '🎯 Staging deployment completed successfully!'
                    publishHTML([
                        reportDir: 'locust-reports',
                        reportFiles: 'order-service-report.html, payment-service-report.html, favourite-service-report.html, order-service-stress-report.html, payment-service-stress-report.html, favourite-service-stress-report.html',
                        reportName: 'Locust Stress Test Reports',
                        keepAll: true
                    ])
                } else if (env.BRANCH_NAME == 'develop') {
                    echo '🔧 Development tests completed successfully!'
                }
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed for ${env.BRANCH_NAME} branch."
                echo '🔍 Check the logs for details.'
                echo '📧 Notify the development team about the failure.'
            }
        }
        unstable {
            script {
                echo "⚠️ Pipeline completed with warnings for ${env.BRANCH_NAME} branch."
                echo '🔍 Some tests may have failed. Review test reports.'
            }
        }
    }
} 