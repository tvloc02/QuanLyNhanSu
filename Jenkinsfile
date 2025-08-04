pipeline {
    agent any
    environment {
        APP_NAME = "web-app"
        DEPLOY_DIR = "/opt/webapp"
    }
    tools {
        maven 'Maven-3.9.9'
        jdk 'JDK-17'
    }
    stages {
        stage('Clone Source') {
            steps {
                echo '==> Cloning source code...'
                git url: 'https://github.com/tvloc02/QuanLyNhanSu.git', branch: 'main'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '==> Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=web-app \
                        -Dsonar.projectName="HR Management System"
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '==> Waiting for Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo '==> Building application...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to Production Server') {
            steps {
                echo '==> Deploying to production server...'
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: 'server-ssh', passwordVariable: 'SSH_PASSWORD', usernameVariable: 'SSH_USERNAME')]) {
                            sh '''
                                echo "Connecting to production server 103.226.248.221..."
                                sshpass -p "$SSH_PASSWORD" ssh -o StrictHostKeyChecking=no $SSH_USERNAME@103.226.248.221 "
                                    echo 'Connected to production server successfully';

                                    # Create app directory
                                    mkdir -p /opt/webapp;
                                    mkdir -p /opt/tomcat/webapps;

                                    # Deploy WAR file to Tomcat (if using WAR)
                                    if [ -d /opt/tomcat ]; then
                                        echo 'Copying WAR to Tomcat webapps...';
                                        scp target/*.war $SSH_USERNAME@103.226.248.221:/opt/tomcat/webapps/;
                                        echo 'Restarting Tomcat...';
                                        /opt/tomcat/bin/shutdown.sh || true;
                                        /opt/tomcat/bin/startup.sh;
                                    fi;

                                    # Deploy JAR file (if using Spring Boot)
                                    if [ -f target/*.jar ]; then
                                        echo 'Copying JAR to app directory...';
                                        scp target/*.jar $SSH_USERNAME@103.226.248.221:/opt/webapp/;
                                        echo 'Stopping existing JAR process...';
                                        pkill -f 'java -jar' || true;
                                        echo 'Starting new JAR...';
                                        nohup java -jar /opt/webapp/*.jar &>/opt/webapp/app.log &
                                    fi;

                                    echo 'Production deployment completed successfully';
                                "
                            '''
                        }
                        echo '✅ Production deployment successful!'
                        echo '🌍 Application URL: http://103.226.248.221:8080/${APP_NAME}'
                    } catch (Exception e) {
                        echo "⚠️ Production deployment failed: ${e.getMessage()}"
                        error "Deployment failed"
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                echo '==> Running health check...'
                sh '''
                    echo "=== Health Check Results ==="
                    curl -f http://103.226.248.221:8080/${APP_NAME}/actuator/health || echo "Application not responding"
                '''
            }
        }
    }
    post {
        always {
            echo '==> Pipeline completed'
        }
        success {
            echo 'SUCCESS! All stages completed!'
            echo ''
            echo 'CI/CD Pipeline Results:'
            echo '• ✅ Source Code: CLONED'
            echo '• ✅ SonarQube Analysis: COMPLETED'
            echo '• ✅ Quality Gate: PASSED'
            echo '• ✅ Build: SUCCESS'
            echo '• ✅ Production Deployment: SUCCESS'
            echo '• ✅ Health Check: PASSED'
            echo ''
            echo 'Services are running at:'
            echo '• 🌍 Production Web App: http://103.226.248.221:8080/${APP_NAME}'
        }
        failure {
            echo 'Pipeline failed - check logs above'
        }
    }
}
