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
    }

    post {
        always {
            container('docker') {
                sh 'docker system prune -f'
            }
        }
    }
}