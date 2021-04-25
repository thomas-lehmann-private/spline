pipeline {
    agent any
  
    options  {
        timestamps()
    }

    stages {
        stage('Build') {
            steps { 
                bat '''
                    ./mvnw clean package verify
                '''
            }
        }
    }

    post { 
        always { 
            junit 'target/**/TEST*.xml'

            recordIssues enabledForFailure: true, tool: checkStyle()
            recordIssues enabledForFailure: true, tool: spotBugs()
        }
    }
}
