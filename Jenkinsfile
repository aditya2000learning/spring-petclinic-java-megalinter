pipeline {
    agent { label 'Master'}
    triggers { pollSCM('* * * * *') }
    stages{
        stage('vcs') {
            steps {
                git branch: 'declarative',
                    url: 'https://github.com/Bharatkumar5690/spring-petclinic.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }
        stage('test code by using sonarqube') {
            steps {
                archiveArtifacts artifacts: '**/target/gameoflife.war',
                                    onlyIfSuccessful: true,
                                    allowEmptyArchive: true
                junit testResults: '**/surefire-reports/TEST-*.xml'
            }
        }
    }
}
