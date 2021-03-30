# Project Title 
WAYA-PAY-CHAT-2.0-BILLSPAYMENT

## Summary 
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites) 
  - [Dependencies](#dependencies)
  - [Setup](#setup)
- [Screenshots](#screenshots) 
- [Repo Structure](#repo-structure) 
- [Running the tests](#running-the-tests) 
- [Deployment](#deployment) 
- [Contributing](#contributing) 
- [Versioning](#versioning) 
- [Authors](#authors)
- [License](#license)
- [Acknowledgements](#acknowledgements) 
- [Additional information](#additional-information)  
  
## Getting Started
This is a service which helps process billspayment via vendors/thirdparties like ITEX, BAXi or QUICKTELLER. Below is a link to the process flow.
````
https://wayapaychatng-my.sharepoint.com/:i:/g/personal/stanley_obidiagha_wayapaychat_com/ET8UiI1oZahLsWggeXo_lwgBOPX9xCNYC7uuLriaoO-Iiw?e=fPpDdV
````

### Prerequisites 
- jdk 8 (https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
- maven 3.6.2 (https://maven.apache.org/download.cgi)
- git 2.23.0 (https://git-scm.com/downloads)
- intellij 2020.2 (https://www.jetbrains.com/idea/download/)

### Dependencies   
Billspayment Service is dependent on 
- WayaPay Dispute/Complaint Service
- WayaPay Wallet Service
- WayaPay Auth Service
- Capricorn/BAXI Service
- Interswitch/QuickTeller Service
- Itex Service

### Setup 
- clone source code
- cd to root directory
- Establish access to NIBSS environment
- run -> mvn -U clean install
- run -> mvn spring-boot:run

- Note that to run it locally, no need to alter the application.properties file.

## Screenshot(s)
``` 
https://wayapaychatng-my.sharepoint.com/:i:/g/personal/stanley_obidiagha_wayapaychat_com/EYAZXFoGaLtHq_eqX4Ns4NEBq0WutgaHrA7WW3ToomeIBw?e=RfzGMF
``` 
Expectation : A Swagger Document Page containing API Documentation

## Repo Structure 
 
 ```
/
├─ src/
│  ├─ main/
│  │  ├─ java/                                                  # actual java code
│  │  │  ├─ com/wayapay/thirdpartyintegrationservice/           # parent folder
│  │  │  │  ├─ annotations/                                     # annotation used in event operation
│  │  │  │  ├─ config/                                          # application configurations
│  │  │  │  ├─ controller/                                      # restful api controllers
│  │  │  │  ├─ dto/                                             # data transfer objects
│  │  │  │  ├─ event/                                           # event operations
│  │  │  │  ├─ exceptionhandling/                               # service that helps to handle exceptions and used for validation
│  │  │  │  ├─ interceptors/                                    # app interceptor
│  │  │  │  ├─ kafka/                                           # kafka data structure
│  │  │  │  ├─ model/                                           # app database entities 
│  │  │  │  ├─ repo/                                            # app database access service 
│  │  │  │  ├─ responsehelper/                                  # app response object
│  │  │  │  ├─ service/                                         # core business logic
│  │  │  │  ├─ util/                                            # app common operations
│  │  ├─ resources/                                             # app externalized configurations
│  ├─ test/                                   
│  │  ├─ java/                                                  # java test code 
│  │  │  ├─ com/wayapay/thirdpartyintegrationservice/             
│  │  │  │  ├─ service/                                         # test scripts
├─ .gitignore                                                   # git source code management
├─ pom.xml                                                      # dependency management file
└─ README.md                                                    # This file

```

## Running the tests
- run -> mvn test 
Note :
 - once the above cmd is complete, the coverage is automatically generated after 1 min.
 - coverage can be found in the /target/site/jacoco directory
 - to view the coverage report, open /target/site/jacoco/index.html on the browser.

## Deployment
As a developer, Kindly do the following
 - git commit your changes.
 - git push your changes to its feature branch
 - merge to dev branch  

## Contributing 

## Versioning 
This section should contain the versioning table of the application. Format shown below: 
Version No 
Description of change 
Date 
| Version No    | Description of change | Date       |
| ------------- |:---------------------:| ----------:|
| 1.0.0.        | New deployment        | 22/03/2021 |


## Authors 
- Obidiagha Stanley 
  
## License
- This project remains the intellectual property of WayaPay.
  
## Acknowledgments 
 
## Additional Information 