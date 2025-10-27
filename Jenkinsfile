pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        KUBECONFIG = credentials('kubeconfig')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/JMMA86/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['service-discovery', 'cloud-config', 'api-gateway', 'proxy-client', 'user-service', 'product-service', 'favourite-service', 'order-service', 'shipping-service', 'payment-service']
                    services.each { service ->
                        sh "docker build -t ${DOCKER_REGISTRY}/ecommerce/${service}:latest ./${service}"
                        sh "docker push ${DOCKER_REGISTRY}/ecommerce/${service}:latest"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/namespace.yml'
                sh 'kubectl apply -f k8s/configmap.yml'
                sh 'kubectl apply -f k8s/'
            }
        }
    }

    post {
        always {
            sh 'docker system prune -f'
        }
    }
}