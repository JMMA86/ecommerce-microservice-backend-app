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

        stage('Run Tests') {
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

        stage('Deploy to Kubernetes') {
            steps {
                container('maven') {
                    sh '''
                        curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
                        chmod +x kubectl
                        ./kubectl create namespace ecommerce-dev --dry-run=client -o yaml | ./kubectl apply -f -
                        helm upgrade --install ecommerce ./helm-charts/ecommerce --namespace ecommerce-dev
                    '''
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