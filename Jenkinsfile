pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                withMaven(
                        maven : 'Default'
                ) {
                    // Run the maven build
                    sh "mvn clean package"
                }
            }
        }
    }
}