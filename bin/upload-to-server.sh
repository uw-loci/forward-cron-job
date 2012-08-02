#!/bin/sh

SERVER=open.microscopy.wisc.edu
FOLDER=forward-cron/

# halt on errors (e.g., build failure)
set -e

# rebuild the code
mvn clean package

# copy files to the server
scp target/forward-cron-job-1.0.0-SNAPSHOT.jar $SERVER:$FOLDER
scp src/main/bin/publish.sh $SERVER:$FOLDER
