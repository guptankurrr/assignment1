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
