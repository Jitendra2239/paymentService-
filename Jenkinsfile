@Library("shared libraries") _
pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                script {
                    clone("https://github.com/Jitendra2239/paymentservice.git","master")
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    echo "🔨 Building Docker image..."
                    sh "docker build -t productservice:latest ."
                }
            }
        }

        stage('Dockerhub push') {
            steps {
                script {
                    echo "Pushing image to DockerHub..."
                    withCredentials([usernamePassword(
                            credentialsId: 'dockerhub',
                            usernameVariable: 'USERNAME',
                            passwordVariable: 'PASSWORD')]) {

                        sh "docker login -u ${env.USERNAME} -p ${env.PASSWORD}"
                        sh "docker image tag productservice:latest nlog10n/productservice:latest"
                        sh "docker push nlog10n/productservice:latest"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'
                sh "docker run -d -p 8080:8080 productservice:latest"
            }
        }
    }

    post {
        success {
            echo "✅ Docker image built successfully: productservice:latest"
        }
        failure {
            echo "❌ Docker build failed!"
        }
    }
}