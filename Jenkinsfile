/*
 * Parameterized pipeline for the Generic Selenium Framework.
 *
 * Jenkins prerequisites (Manage Jenkins):
 *   Plugins : Pipeline, Git, Allure Jenkins Plugin, JUnit, Timestamper,
 *             AWS Credentials (only for prod secrets via access keys)
 *   Tools   : JDK named 'JDK21', Maven named 'Maven3', Allure Commandline named 'Allure'
 *   Creds   : an "AWS Credentials" entry (default id 'aws-selenium-prod') with ssm:GetParameter
 *             rights - OR run prod on an agent that has an IAM role and leave AWS_CREDENTIALS_ID empty.
 *
 * Works on both Linux and Windows agents.
 */
pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    parameters {
        choice(name: 'ENV', choices: ['qa', 'dev', 'prod'], description: 'Target environment (loads config/<ENV>.properties)')
        choice(name: 'SUITE', choices: ['testng.xml', 'smoke.xml', 'cross-browser.xml'], description: 'TestNG suite under src/test/resources/suites')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'], description: 'Browser (ignored by cross-browser.xml, which sets its own)')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run browsers headless (keep true on CI agents)')
        choice(name: 'PARALLEL', choices: ['default', 'methods', 'classes', 'tests', 'none'], description: "TestNG parallel mode; 'default' = use the suite XML value")
        string(name: 'THREADS', defaultValue: '3', description: 'Parallel thread count')
        string(name: 'RETRY_COUNT', defaultValue: '2', description: 'Retries after the first run (2 = up to 3 attempts). Failure evidence is saved only when every attempt fails')
        string(name: 'GRID_URL', defaultValue: '', description: 'Optional Selenium Grid URL, e.g. http://grid-host:4444 (empty = local browsers)')
        string(name: 'AWS_CREDENTIALS_ID', defaultValue: 'aws-selenium-prod', description: 'Jenkins AWS credentials id used for prod secrets. Empty = use agent IAM role')
        string(name: 'AWS_REGION', defaultValue: 'ap-south-1', description: 'Region of the Parameter Store secrets')
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    // Uncomment for a nightly QA regression run:
    // triggers { cron('H 2 * * 1-5') }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.ENV} | ${params.SUITE} | ${params.BROWSER}"
                }
            }
        }

        stage('Compile') {
            steps {
                script { run('mvn -B -q clean test-compile') }
            }
        }

        stage('Run tests') {
            steps {
                script {
                    String cmd = "mvn -B test" +
                            " \"-Denv=${params.ENV}\"" +
                            " \"-DsuiteXmlFile=src/test/resources/suites/${params.SUITE}\"" +
                            " \"-Dbrowser=${params.BROWSER}\"" +
                            " \"-Dheadless=${params.HEADLESS}\"" +
                            " \"-Dparallel=${params.PARALLEL}\"" +
                            " \"-Dthreads=${params.THREADS}\"" +
                            " \"-DgridUrl=${params.GRID_URL}\"" +
                            " \"-Dretry.count=${params.RETRY_COUNT}\"" +
                            " \"-Daws.region=${params.AWS_REGION}\"" +
                            " -Dmaven.test.failure.ignore=true"   // let Allure/JUnit decide build status

                    if (params.ENV == 'prod' && params.AWS_CREDENTIALS_ID?.trim()) {
                        // Exposes AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY (masked) only for this step.
                        withCredentials([aws(credentialsId: params.AWS_CREDENTIALS_ID,
                                             accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                             secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                            withEnv(["AWS_REGION=${params.AWS_REGION}"]) {
                                run(cmd)
                            }
                        }
                    } else {
                        run(cmd)
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            allure includeProperties: false, jdk: '', commandline: 'Allure',
                   results: [[path: 'target/allure-results']]
            archiveArtifacts allowEmptyArchive: true, artifacts: 'failure-evidence/**, target/logs/**, target/surefire-reports/**'
        }
        failure {
            echo 'Build failed - check the console log and the Allure report.'
        }
    }
}

/** Runs a shell command on Linux/macOS agents and a batch command on Windows agents. */
def run(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}
