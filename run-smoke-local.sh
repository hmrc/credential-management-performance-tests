#!/bin/sh -xe

sm2 \
  --start CREDENTIAL_MANAGEMENT_ALL --wait 60 \
  --appendArgs '{"ACCOUNT_CONTEXT_FIXER_FRONTEND" : ["-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes", "-Dfeatures.testOnlyNinoAccess=true"],
  "ACCOUNT_CONTEXT_FIXER" : ["-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes", "-Dfeatures.testOnlyNinoAccess=true"]}'


if [ $? != 0 ]
then
    echo "Failed to start all services"
    exit 1
fi


sbt -DrunLocal=true -Dperftest.runSmokeTest=true -DjourneysToRun.0=cm-ropcRegister-journey Gatling/test
