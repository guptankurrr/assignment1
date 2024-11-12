pipeline {
    agent any  
    
    environment {
        SONARQUBE_SERVER = 'SonarQube'  // This should match the name of your SonarQube server configuration in Jenkins
        SONARQUBE_TOKEN = credentials('sonar-token')  // Jenkins credential for SonarQube token
    }

    triggers {
         
        pollSCM('H/5 * * * *')   
    }

    stages {
        stage('Checkout') {
            steps {
                 
                git branch: 'main', url: '****'
            }
        }
        
   
        stage('SonarQube Analysis') {
            steps {
                script {
                   
                    echo 'Running SonarQube analysis...'
                    withSonarQubeEnv(SONARQUBE_SERVER) {
                        sh """
                            sonar-scanner \
                                -Dsonar.projectKey=my-project-key \
                                -Dsonar.sources=. \
                                -Dsonar.host.url=http://sonarqube-server-url \
                                -Dsonar.login=${SONARQUBE_TOKEN}  // Use SonarQube token for authentication
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    
                    echo 'Checking SonarQube quality gate status...'
                    def qualityGate = waitForQualityGate()
                    if (qualityGate.status != 'OK') {
                        error "Quality gate failed: ${qualityGate.status}"   
                    }
                }
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        
        failure {
            echo 'Pipeline failed.'
        }
        
        always {
            echo 'Pipeline finished.'
        }
    }
}
