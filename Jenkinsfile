pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME = 'springboot-student-api'
        DOCKERHUB_USERNAME = 'REPLACE_WITH_DOCKERHUB_USERNAME'
        K8S_NAMESPACE = 'default'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = sh(
                        script: 'git rev-parse --short=7 HEAD',
                        returnStdout: true
                    ).trim()
                    env.FULL_IMAGE = "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean test package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $FULL_IMAGE .'
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$FULL_IMAGE"
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(
                    credentialsId: 'kubeconfig-file',
                    variable: 'KUBECONFIG'
                )]) {
                    sh '''
                        kubectl --kubeconfig="$KUBECONFIG" \
                          create namespace "$K8S_NAMESPACE" \
                          --dry-run=client -o yaml | kubectl --kubeconfig="$KUBECONFIG" apply -f -

                        sed "s|IMAGE_PLACEHOLDER|$FULL_IMAGE|g" k8s/deployment.yaml \
                          | kubectl --kubeconfig="$KUBECONFIG" -n "$K8S_NAMESPACE" apply -f -

                        kubectl --kubeconfig="$KUBECONFIG" -n "$K8S_NAMESPACE" \
                          apply -f k8s/service.yaml

                        kubectl --kubeconfig="$KUBECONFIG" -n "$K8S_NAMESPACE" \
                          rollout status deployment/student-api --timeout=180s
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI/CD completed successfully. Verify the application using the Kubernetes Service.'
        }
        failure {
            echo 'Pipeline failed. Check the failed stage logs before retrying.'
        }
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
    }
}
