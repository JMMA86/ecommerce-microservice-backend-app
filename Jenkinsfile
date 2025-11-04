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
  - name: docker
    image: docker:dind
    securityContext:
      privileged: true
    command:
    - dockerd
    tty: true
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
                        def allServices = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        def services = params.SELECT_SERVICE == 'ALL' ? allServices : [params.SELECT_SERVICE]
                        services.each { service ->
                            dir(service) {
                                sh 'mvn clean package -DskipTests'
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
                        def services = params.SELECT_SERVICE == 'ALL' ? allServices : [params.SELECT_SERVICE]
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
                        def services = params.SELECT_SERVICE == 'ALL' ? allServices : [params.SELECT_SERVICE]
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
                    sh """
                        kubectl version --client
                        helm version
                        kubectl create namespace ${env.K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                        helm upgrade --install ecommerce ./helm-charts/ecommerce --namespace ${env.K8S_NAMESPACE}
                    """
                }
            }
        }

        stage('Wait for Services') {
            when {
                expression { params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('kubectl') {
                    sh '''
                        echo "Waiting for deployments to be ready..."
                        kubectl wait --for=condition=available --timeout=300s deployment --all -n ecommerce-dev || true
                        
                        echo "Listing all services in ecommerce-dev namespace:"
                        kubectl get svc -n ecommerce-dev
                        
                        echo "Listing all pods in ecommerce-dev namespace:"
                        kubectl get pods -n ecommerce-dev
                        
                        echo "Waiting additional 30 seconds for services to stabilize..."
                        sleep 30
                    '''
                }
            }
        }

        stage('Run E2E Tests') {
            when {
                expression { params.RUN_E2E_TESTS && params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('node') {
                    script {
                        sh 'apt-get update && apt-get install -y libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev libgconf-2-4 libnss3-dev libxss1 libasound2-dev libxtst6 xauth xvfb curl'
                        def baseUrl = env.API_GATEWAY_BASE_URL
                        dir('e2e-tests') {
                            sh 'npm install'
                            sh """
                                echo "Testing connectivity to API Gateway (${baseUrl})..."
                                max_attempts=30
                                attempt=1

                                while [ \$attempt -le \$max_attempts ]; do
                                    echo "Attempt \$attempt of \$max_attempts..."
                                    if curl -f ${baseUrl}/actuator/health; then
                                        echo "API Gateway is ready!"
                                        break
                                    fi

                                    if [ \$attempt -eq \$max_attempts ]; then
                                        echo "API Gateway did not become ready in time"
                                        exit 1
                                    fi

                                    echo "Waiting 10 seconds before retry..."
                                    sleep 10
                                    attempt=\$((attempt + 1))
                                done

                                export CYPRESS_BASE_URL=${baseUrl}
                                xvfb-run -a npx cypress run --config baseUrl=${baseUrl}
                            """
                        }
                    }
                }
            }
        }

        stage('Run Locust Tests') {
            when {
                expression { params.RUN_LOAD_TESTS && params.DEPLOY_TO_KUBERNETES }
            }
            steps {
                container('locust') {
                    dir('tests/performance') {
                        sh '''
                            pip install --no-cache-dir -r requirements.txt
                            mkdir -p reports
                            locust -f locustfile.py --headless \
                                --users ${LOCUST_USERS} \
                                --spawn-rate ${LOCUST_SPAWN_RATE} \
                                --run-time ${LOCUST_DURATION} \
                                --host=${API_GATEWAY_BASE_URL} \
                                --exit-code-on-error 2 \
                                --stop-timeout 30 \
                                --html reports/locust-report.html \
                                --csv reports/locust-results
                        '''
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