pipeline {
    environment {
    registry = "wayapaychat-container-registry/waya-billspayment-model"
    registryCredential = 'DigitalOcean-registry-for-development'
    dockerImage = ''
    }
      /*	parameters {
	    strings(name: 'FROM_BUILD' defaultValue: '', description: 'Build Source')
	} */
    agent any

    tools {
        jdk 'jdk-11'
        maven 'mvn3.6.3'
    }
	    stages {
		
		 stage('Checkout') {
            		steps {
			
				sh "git branch"
                		sh "ls -lart ./*"
            		}
        	}     
		
	    
        stage('compile') {
            steps {
               sh "mvn clean install -DskipTests"
            }
                
        }
	  
	stage('Code Quality Check via SonarQube') {
		/*environment {
                 SCANNER_HOME = tool 'SonarQubeScanner'
                 ORGANIZATION = "igorstojanovski-github"
                 PROJECT_NAME = "igorstojanovski_jenkins-pipeline-as-code" */
             steps {
                 script {
                     def scannerHome = tool 'Jenkins-sonar-scanner';
                     withSonarQubeEnv("Jenkins-sonar-scanner") {
                     sh "${tool("Jenkins-sonar-scanner")}/bin/sonar-scanner \
		     -Dsonar.projectName=waya-bills-payment-service \
	             -Dsonar.projectKey=waya-bills-payment-service \
	             -Dsonar.sources=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev \
		     -Dsonar.projectBaseDir=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev \
                     -Dsonar.sources=. \
		     -Dsonar.projectVersion=1.0 \
                     -Dsonar.language=java \
                     -Dsonar.java.binaries=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/target/classes \
                     -Dsonar.sourceEncoding=UTF-8 \
                     -Dsonar.test.inclusions=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/src \
		     -Dsonar.junit.reportsPath=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/target/surefire-reports \
                     -Dsonar.surefire.reportsPath=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/target/surefire-reports \
                     -Dsonar.jacoco.reportPath=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/target/coverage-reports/jacoco-unit.exec \
                     -Dsonar.java.coveragePlugin=/var/jenkins_home/workspace/waya-2.0-bills-payment-service-dev/target/jacoco  \
		     -Dsonar.host.url=https://sonarqube.waya-pay.com \
		     -Dsonar.verbose=true "
		     /*-Dsonar.login=af09e850c06ef09772eb902254bb8f532d13297a" */
               }
           }
       }
   }
	    //stage("Quality Gate") {
		//	steps {
		//	    	timeout(time: 1, unit: 'HOURS') {
                   	    		// Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    	    		// true = set pipeline to UNSTABLE, false = don't
                  // 	    		waitForQualityGate abortPipeline: true
		//		}
            	//	}
        	//}

	    
    stage('Building image') {
      steps{
        script {
          /*dockerImage = docker.build registry + ":$BUILD_NUMBER" */
	    dockerImage=docker.build registry
        }
      }
    }
    stage('Deploy Image') {
      steps{
         script {
            docker.withRegistry( 'https://registry.digitalocean.com/wayapaychat-container-registry', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }
	

stage('Remove Unused docker image') {
      steps{
	     cleanWs()
        /* sh "docker rmi $registry:$BUILD_NUMBER" */
	   sh "docker rmi $registry"
      }
    } 
		    
    stage ('Starting the deployment job') {
	    steps {
                build job: 'waya-2.0-bills-payment-service-deploy-dev', 
		parameters: [[$class: 'StringParameterValue', name: 'FROM_BUILD', value: "${BUILD_NUMBER}"]
	        ]
	    }	    
    }	  
  
 }

}
