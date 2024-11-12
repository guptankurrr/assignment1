pipeline {
    agent any  
    
    triggers {
         
        pollSCM('H/5 * * * *')   
    }

    stages {
        stage('Checkout') {
            steps {
                 
                git branch: 'main', url: '****'
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
