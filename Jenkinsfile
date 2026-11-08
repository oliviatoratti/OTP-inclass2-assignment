
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'oliviaet/assignment6'
        MVN = 'C:\\Program Files\\JetBrains\\IntelliJ IDEA 2026.1\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'call "%MVN%" -B clean verify'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    docker build -t %DOCKER_IMAGE%:%BUILD_NUMBER% -t %DOCKER_IMAGE%:latest .
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    bat '''
                        @echo off
                        echo %DOCKER_PASS% | docker login --username "%DOCKER_USER%" --password-stdin
                        if errorlevel 1 exit /b 1
                        docker push %DOCKER_IMAGE%:%BUILD_NUMBER%
                        if errorlevel 1 exit /b 1
                        docker push %DOCKER_IMAGE%:latest
                        if errorlevel 1 exit /b 1
                        docker logout
                    '''
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
        }
        success {
            echo 'Build, tests, Docker image build and Docker Hub push completed.'
        }
        failure {
            echo 'The pipeline failed. Check the stage logs for the error.'
        }
    }
}