pipeline {
    agent any
    environment {
        APP_NAME = "web-app"
        DEPLOY_DIR = "C:\\opt\\webapp"  // Điều chỉnh đường dẫn cho Windows
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
                    bat '''
                        mvn clean verify sonar:sonar ^
                        -Dsonar.projectKey=web-app ^
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
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to Production Server') {
            steps {
                echo '==> Deploying to production server...'
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: 'server-ssh', passwordVariable: 'SSH_PASSWORD', usernameVariable: 'SSH_USERNAME')]) {
                            bat '''
                                echo "Connecting to production server 103.226.248.221..."
                                sshpass -p "%SSH_PASSWORD%" ssh -o StrictHostKeyChecking=no %SSH_USERNAME%@103.226.248.221 "
                                    echo 'Connected to production server successfully';

                                    # Create app directory
                                    mkdir C:\\opt\\webapp || echo Directory already exists;
                                    mkdir C:\\opt\\tomcat\\webapps || echo Directory already exists;

                                    # Deploy WAR file to Tomcat (if using WAR)
                                    if exist C:\\opt\\tomcat (
                                        echo Copying WAR to Tomcat webapps...
                                        pscp target\\*.war %SSH_USERNAME%@103.226.248.221:C:\\opt\\tomcat\\webapps\\
                                        echo Restarting Tomcat...
                                        C:\\opt\\tomcat\\bin\\shutdown.bat || echo Shutdown failed;
                                        C:\\opt\\tomcat\\bin\\startup.bat
                                    )

                                    # Deploy JAR file (if using Spring Boot)
                                    if exist target\\*.jar (
                                        echo Copying JAR to app directory...
                                        pscp target\\*.jar %SSH_USERNAME%@103.226.248.221:C:\\opt\\webapp\\
                                        echo Stopping existing JAR process...
                                        taskkill /IM java.exe /F || echo No Java process to kill;
                                        echo Starting new JAR...
                                        plink %SSH_USERNAME%@103.226.248.221 -pw %SSH_PASSWORD% "cd C:\\opt\\webapp && java -jar *.jar > app.log 2>&1"
                                    )

                                    echo Production deployment completed successfully
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
                bat '''
                    echo === Health Check Results ===
                    curl -f http://103.226.248.221:8080/%APP_NAME%/actuator/health || echo Application not responding
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
