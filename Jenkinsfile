pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 15, unit: 'MINUTES')
    }

    stages {
        stage('Compilar') {
            steps {
                bat 'mvnw.cmd -B clean compile'
            }
        }

        stage('Pruebas') {
            steps {
                bat 'mvnw.cmd -B -Dtest=ReservationServiceTest,OrderServiceTest,ApiSecurityTest test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Empaquetar') {
            steps {
                // Las pruebas ya se ejecutaron en la etapa anterior.
                bat 'mvnw.cmd -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true
                }
            }
        }
    }
}