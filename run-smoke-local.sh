#!/bin/sh -xe

#sm2 --stop CREDENTIAL_MANAGEMENT_ALL
#sleep 5
#sm2 --start CREDENTIAL_MANAGEMENT_ALL --wait 60 --noprogress



if [ $? != 0 ]
then
    echo "Failed to start all services"
    exit 1
fi

sbt -DrunLocal=true -Dperftest.runSmokeTest=true -DjourneysToRun.0=acf-verified-context Gatling/test
