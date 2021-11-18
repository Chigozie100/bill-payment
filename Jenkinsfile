pipeline {
    agent any
    environment {
        registry='863852973330.dkr.ecr.eu-west-1.amazonaws.com/wayallpayment-model-'
        aws_Account_id='863852973330'
        aws_default_region='eu-west-1'
        image_repo_name='waya-infra-staging-registry'
        image_tag='latest'
    }
    tools {
        jdk 'jdk-11'
        gradle 'gradle-6.8.1'
    }
    stages{
        stage("compile") {
            steps{
                script {
                    sh 'gradle clean build'
                }
            }   
        }
        stage("Building Image") {
            steps{
                script {
                    dockerImage = docker.build registry
                }
            }   
        }
        stage("Logging into AWS ECR") {
            steps{
                script {
                    sh 'aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 863852973330.dkr.ecr.eu-west-1.amazonaws.com'
                }
            }   
        }
       
        stage("pushing to ECR") {
            steps{
                script {
                    sh "aws ecr describe-repositories --repository-names billpayment-model || aws ecr create-repository --repository-name billpayment-model --image-scanning-configuration scanOnPush=true"
                    sh 'docker push 863852973330.dkr.ecr.eu-west-1.amazonaws.com/billpayment-model'
                }
            }   
        }
        stage("Deploying to EKS cluster") {
            steps{
                script {
                    withCredentials([kubeconfigFile(credentialsId: 'kuberenetes-config', variable: 'KUBECONFIG')]) {
                      dir('kubernetes/'){
                          
                          sh "helm upgrade --install billpayment-model ./base --kubeconfig ~/.kube/config \
                          --set ingress.enabled=false \
                          --set fullnameOverride=billpayment-model \
                          --set autoscaling.enaled=false \
                          --set service.ingress=traefik \
                          --set service.type=ClusterIP \
                          --set service.port=8181 \
                          --set config.EUREKA_SERVER_URL=http://172.20.159.73:8761 \
                          --set config.BOOTSTRAP_SERVICE=Waya-infra-staging-kafka-cluster-kafka-config6166923521113784206 \
                          --set config.POSTGRES_URL=jdbc:postgresql://waya-infra-staging-database-staging-env-staging.c7gddqax0vzn.eu-west-1.rds.amazonaws.com:5432/BillsDBStaging \
                          --set config.POSTGRES_USERNAME=wayapayuser \
                          --set config.POSTGRES_PASSWORD=FrancisJude2020waya \
                          --set 'tolerations[0].effect=NoSchedule' \
                          --set 'tolerations[0].key=dev' \
                          --set 'tolerations[0].operator=Exists'"
                      }
                   }
                }
            }
        }   
    }
}
