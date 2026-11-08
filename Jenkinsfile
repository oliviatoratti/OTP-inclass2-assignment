pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'oliviaet/assignment6'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$BUILD_NUMBER -t $DOCKER_IMAGE:latest .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'oliviaet',
                        passwordVariable: 'Olivia04!'
                    )
                ]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push $DOCKER_IMAGE:$BUILD_NUMBER'
                    sh 'docker push $DOCKER_IMAGE:latest'
                }
            }
        }
    }
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts(
                artifacts: 'target/site/jacoco/**',
                allowEmptyArchive: true
            )
        }
    }
}