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
                 
                git branch: 'main', url: 'https://your-repository-url.git'
            }
        }
        
        stage('Build with JaCoCo') {
            steps {
                script {
                    // Example build step (adjust based on your project)
                    echo 'Building the application with JaCoCo coverage...'
                    sh 'mvn clean install -DskipTests=true'  // Adjust to your build command
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    // Run SonarQube analysis on the code
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
                    // Wait for the SonarQube analysis to finish and check the quality gate status
                    echo 'Checking SonarQube quality gate status...'
                    def qualityGate = waitForQualityGate()
                    if (qualityGate.status != 'OK') {
                        error "Quality gate failed: ${qualityGate.status}"  // Fail the build if quality gate fails
                    }
                }
            }
        }

        stage('Run Tests with JaCoCo') {
            steps {
                script {
                    // Run your tests and generate JaCoCo coverage reports
                    echo 'Running tests with JaCoCo...'
                    sh 'mvn clean test'  // Replace with your test command if different
                }
            }
        }

        stage('Publish JaCoCo Coverage Report') {
            steps {
                script {
                    
                    echo 'Publishing JaCoCo coverage report...'
                    jacoco(execPattern: '**/target/*.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java')
                }
            }
        }
        stage('Calculate Cyclomatic Complexity with Lizard') {
            steps {
                script {
 
                    echo 'Running Lizard to calculate Cyclomatic Complexity...'
                    sh '''
                        # Run Lizard on the source code (adjust the directory/path as needed)
                        lizard **/*.java --output=cyclomatic_complexity_report.txt
                    '''
                    
                     
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'cyclomatic_complexity_report.txt', onlyIfSuccessful: true
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
