pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {

             stage('SonarQube Analysis') {
            steps {
                script {
                 
                    sh """
                        mvn clean verify sonar:sonar -Dsonar.projectKey=my-project-key \
                        -Dsonar.host.url=http://localhost:9000 \
                        -Dsonar.login=${SONARQUBE_TOKEN}
                    """
                     
                }
            }
        }
        
    }
 
    qualityGate {
        
        failBuild: true
       }
}
