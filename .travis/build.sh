#!/bin/bash
set -e

echo "Maven install (${TRAVIS_BRANCH})"
if [ "$1" != "porcelain" ]
then
  mvn clean install -B -Dcoverage.reports.dir=`pwd`/target/all --settings config/src/main/resources/ci/settings.xml
fi

echo "Skip SonarQube analysis (${TRAVIS_BRANCH})"
