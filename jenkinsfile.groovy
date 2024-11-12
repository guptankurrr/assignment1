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
                 
                git branch: 'main', url: 'https://github.com/guptankurrr/jenkins-example.git'
            }
        }
        
        stage('Build with JaCoCo') {
            steps {
                script {
                    
                    echo 'Building the application with JaCoCo coverage...'
                    sh 'mvn clean install -DskipTests=true' 
                }
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

        stage('Run Tests with JaCoCo') {
            steps {
                script {
                      
                    echo 'Running tests with JaCoCo...'
                    sh 'mvn clean test'  
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
        stage('Run OWASP Dependency-Check') {
            steps {
                script {
                   
                    echo 'Running OWASP Dependency-Check to scan for vulnerabilities...'
                    sh '''
                        dependency-check --project "my-project" \
                                         --scan . \
                                         --out ./dependency-check-report \
                                         --format "HTML" \
                                         --failBuildOnCVSS 7
                    '''
                }
            }
        }

        stage('Publish OWASP Dependency-Check Report') {
            steps {
                script {
                   
                    echo 'Publishing OWASP Dependency-Check report...'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'dependency-check-report/*.html', onlyIfSuccessful: true
                }
            }
        }
    post {
        success {
            echo 'Build succeeded!'
          
            emailext(
                to: 'guptankurrr@gmail.com',
                subject: "Build ${currentBuild.fullDisplayName} succeeded",
                body: "The build ${currentBuild.fullDisplayName} has successfully passed.",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        
        failure {
            echo 'Build failed!'
            
            emailext(
                to: 'guptankurrr@gmail.com',
                subject: "Build ${currentBuild.fullDisplayName} failed",
                body: "The build ${currentBuild.fullDisplayName} has failed. Please check the logs for details.",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        
        always {
            echo 'Pipeline completed.'
        }
    }
}
