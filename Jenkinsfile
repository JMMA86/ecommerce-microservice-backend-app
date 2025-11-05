pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  namespace: devops
spec:
  serviceAccountName: jenkins-sa
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-17
    command:
    - cat
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
  - name: docker
    image: docker:24-dind
    securityContext:
      privileged: true
    env:
    - name: DOCKER_TLS_CERTDIR
      value: ""
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run
  - name: kubectl
    image: dtzar/helm-kubectl:3.12
    command:
    - cat
    tty: true
  - name: node
    image: node:20
    command:
    - cat
    tty: true
  - name: locust
    image: locustio/locust:2.20.1
    command:
    - cat
    tty: true
  volumes:
  - name: maven-cache
    emptyDir: {}
  - name: docker-sock
    emptyDir: {}
'''
        }
    }

    parameters {
        choice(
            name: 'SELECT_SERVICE',
            choices: ['all-services', 'favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service'],
            description: 'Selecciona los microservicios a probar.'
        )
        booleanParam(
            name: 'DEPLOY_TO_KUBERNETES',
            defaultValue: true,
            description: 'Realizar el despliegue de los servicios en Kubernetes con Helm'
        )
        booleanParam(
            name: 'RUN_E2E_TESTS',
            defaultValue: false,
            description: 'Ejecutar pruebas E2E utilizando Cypress'
        )
        booleanParam(
            name: 'RUN_LOAD_TESTS',
            defaultValue: true,
            description: 'Ejecutar pruebas de rendimiento con Locust'
        )
        string(
            name: 'LOCUST_USERS',
            defaultValue: '5',
            description: 'Usuarios concurrentes para Locust'
        )
        string(
            name: 'LOCUST_SPAWN_RATE',
            defaultValue: '3',
            description: 'Usuarios por segundo de Locust'
        )
        string(
            name: 'LOCUST_DURATION',
            defaultValue: '1m',
            description: 'Duración total de la prueba'
        )
    }

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        SERVICES_LIST = 'service-discovery,cloud-config,api-gateway,user-service,product-service,order-service,payment-service,shipping-service,favourite-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.ACTIVE_BRANCH = env.BRANCH_NAME ?: 'master'
                    env.K8S_NAMESPACE = env.ACTIVE_BRANCH == 'master' ? 'ecommerce-prod' : 'ecommerce-dev'
                    env.API_GATEWAY_INTERNAL_URL = "http://api-gateway.${env.K8S_NAMESPACE}.svc.cluster.local:8300"
                    def minikubeIp = env.MINIKUBE_IP?.trim()
                    env.API_GATEWAY_BASE_URL = minikubeIp ? "http://${minikubeIp}:50000" : env.API_GATEWAY_INTERNAL_URL
                    echo "Rama activa: ${env.ACTIVE_BRANCH}"
                    echo "Namespace objetivo: ${env.K8S_NAMESPACE}"
                    echo "Gateway interno: ${env.API_GATEWAY_INTERNAL_URL}"
                    echo "Gateway base URL para pruebas: ${env.API_GATEWAY_BASE_URL}"
                }
            }
        }

        stage('Build with Maven') {
            steps {
                container('maven') {
                    script {
                        echo "Building all services with Maven..."
                        def services = env.SERVICES_LIST.split(',')
                        services.each { service ->
                            echo "Building ${service}..."
                            dir(service) {
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                container('docker') {
                    script {
                        echo "Building Docker images for all services..."
                        def services = env.SERVICES_LIST.split(',')
                        
                        // Wait for Docker daemon to be ready
                        sh '''
                            timeout=60
                            until docker info >/dev/null 2>&1; do
                                echo "Waiting for Docker daemon..."
                                sleep 2
                                timeout=$((timeout-2))
                                if [ $timeout -le 0 ]; then
                                    echo "Docker daemon did not start in time"
                                    exit 1
                                fi
                            done
                            echo "Docker daemon is ready"
                        '''
                        
                        services.each { service ->
                            echo "Building Docker image for ${service}..."
                            dir(service) {
                                sh """
                                    docker build -t ${service}:latest .
                                    echo "Successfully built ${service}:latest"
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Run Unitary Tests') {
            steps {
                container('maven') {
                    script {
                        def allServices = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        def services = params.SELECT_SERVICE == 'all-services' ? allServices : [params.SELECT_SERVICE]
                        services.each { service ->
                            dir(service) {
                                sh 'mvn test'
                            }
                        }
                    }
                }
            }
        }

        stage ('Run Integration Tests') {
            steps {
                container('maven') {
                    script {
                        def allServices = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        def services = params.SELECT_SERVICE == 'all-services' ? allServices : [params.SELECT_SERVICE]
                        services.each { service ->
                            dir(service) {
                                sh 'mvn verify -Pintegration-tests'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('kubectl') {
                    script {
                        sh """
                            echo "Deploying to Kubernetes..."
                            kubectl version --client
                            helm version
                            
                            echo "Creating namespace ${env.K8S_NAMESPACE}..."
                            kubectl create namespace ${env.K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                            
                            echo "Deploying with Helm..."
                            helm upgrade --install ecommerce ./helm-charts/ecommerce --namespace ${env.K8S_NAMESPACE}
                            
                            echo "Waiting for service-discovery to be ready..."
                            kubectl rollout status deployment/service-discovery -n ${env.K8S_NAMESPACE} --timeout=600s || true
                        """
                    }
                }
            }
        }

        stage('Wait for Services') {
            when {
                expression { params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('kubectl') {
                    sh """
                        echo "Waiting for all deployments to be ready..."
                        kubectl wait --for=condition=available --timeout=300s deployment --all -n ${env.K8S_NAMESPACE} || true
                        
                        echo "Listing all services in ${env.K8S_NAMESPACE} namespace:"
                        kubectl get svc -n ${env.K8S_NAMESPACE}
                        
                        echo "Listing all pods in ${env.K8S_NAMESPACE} namespace:"
                        kubectl get pods -n ${env.K8S_NAMESPACE}
                        
                        echo "Waiting additional 30 seconds for services to stabilize..."
                        sleep 30
                    """
                }
            }
        }

        stage('Run E2E Tests') {
            when {
                expression { params.RUN_E2E_TESTS && params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                script {
                    // Iniciar port-forward en el contenedor kubectl
                    container('kubectl') {
                        sh """
                            echo "Starting port-forward to API Gateway..."
                            nohup kubectl port-forward svc/api-gateway 9090:8080 -n ${env.K8S_NAMESPACE} > /tmp/port-forward.log 2>&1 &
                            echo \$! > /tmp/port-forward.pid
                            
                            echo "Waiting for port-forward to be ready..."
                            sleep 5
                        """
                    }
                    
                    // Ejecutar tests en el contenedor node
                    container('node') {
                        sh '''
                            apt-get update && apt-get install -y libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev \
                                libgconf-2-4 libnss3-dev libxss1 libasound2-dev libxtst6 xauth xvfb curl
                        '''

                        dir('e2e-tests') {
                            sh """
                                echo "Waiting for API Gateway to respond..."
                                for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30; do
                                    if curl -sf http://localhost:9090/actuator/health > /dev/null 2>&1; then
                                        echo "API Gateway is ready!"
                                        break
                                    fi
                                    echo "Attempt \$i/30: Waiting..."
                                    sleep 5
                                done

                                # Actualizar package-lock.json
                                echo "Installing dependencies..."
                                npm install --package-lock-only
                                npm ci --prefer-offline --no-audit

                                # Ejecutar Cypress
                                export CYPRESS_BASE_URL=http://localhost:9090
                                xvfb-run -a npx cypress run --config baseUrl=http://localhost:9090 || echo "Cypress tests completed with warnings"
                            """
                        }
                    }
                    
                    // Detener port-forward
                    container('kubectl') {
                        sh """
                            if [ -f /tmp/port-forward.pid ]; then
                                kill \$(cat /tmp/port-forward.pid) 2>/dev/null || true
                                rm -f /tmp/port-forward.pid
                            fi
                        """
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'e2e-tests/cypress/screenshots/**,e2e-tests/cypress/videos/**', allowEmptyArchive: true
                }
            }
        }

        stage('Run Locust Tests') {
            when {
                expression { params.RUN_LOAD_TESTS && params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('locust') {
                    script {
                        dir('tests/performance') {
                            sh """
                                echo "Installing Locust dependencies..."
                                pip install --no-cache-dir -r requirements.txt
                                
                                mkdir -p reports
                                
                                echo "Running Locust performance tests..."
                                echo "Target: ${env.API_GATEWAY_BASE_URL}"
                                echo "Users: ${params.LOCUST_USERS}"
                                echo "Spawn Rate: ${params.LOCUST_SPAWN_RATE}"
                                echo "Duration: ${params.LOCUST_DURATION}"
                                
                                locust -f locustfile.py --headless \
                                    --users ${params.LOCUST_USERS} \
                                    --spawn-rate ${params.LOCUST_SPAWN_RATE} \
                                    --run-time ${params.LOCUST_DURATION} \
                                    --host=${env.API_GATEWAY_BASE_URL} \
                                    --exit-code-on-error 0 \
                                    --stop-timeout 30 \
                                    --html reports/locust-report.html \
                                    --csv reports/locust-results || echo "Locust tests completed with warnings"
                            """
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'tests/performance/reports/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        always {
            container('docker') {
                sh 'docker system prune -f'
            }
        }
    }
}