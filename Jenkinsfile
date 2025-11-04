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
'''
        }
    }

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/JMMA86/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Build with Maven') {
            steps {
                container('maven') {
                    script {
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
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
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
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
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
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
            steps {
                container('kubectl') {
                    sh '''
                        kubectl version --client
                        helm version
                        kubectl create namespace ecommerce-dev --dry-run=client -o yaml | kubectl apply -f -
                        helm upgrade --install ecommerce ./helm-charts/ecommerce --namespace ecommerce-dev
                    '''
                }
            }
        }

        stage('Wait for Services') {
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
            steps {
                container('node') {
                    sh 'apt-get update && apt-get install -y libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev libgconf-2-4 libnss3-dev libxss1 libasound2-dev libxtst6 xauth xvfb curl'
                    dir('e2e-tests') {
                        sh 'npm install'
                        sh '''
                            # Test connectivity to API Gateway before running tests
                            echo "Testing connectivity to API Gateway..."
                            max_attempts=30
                            attempt=1
                            
                            while [ $attempt -le $max_attempts ]; do
                                echo "Attempt $attempt of $max_attempts..."
                                if curl -f http://api-gateway.ecommerce-dev.svc.cluster.local:8300/actuator/health; then
                                    echo "API Gateway is ready!"
                                    break
                                fi
                                
                                if [ $attempt -eq $max_attempts ]; then
                                    echo "API Gateway did not become ready in time"
                                    exit 1
                                fi
                                
                                echo "Waiting 10 seconds before retry..."
                                sleep 10
                                attempt=$((attempt + 1))
                            done
                            
                            # Run Cypress tests
                            export CYPRESS_BASE_URL=http://api-gateway.ecommerce-dev.svc.cluster.local:8300
                            export CYPRESS_EUREKA_URL=http://service-discovery.ecommerce-dev.svc.cluster.local:8761
                            xvfb-run -a npx cypress run --config baseUrl=http://api-gateway.ecommerce-dev.svc.cluster.local:8300
                        '''
                    }
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