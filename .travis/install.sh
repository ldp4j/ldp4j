#!/bin/bash
set -e

echo "Maven install (${TRAVIS_BRANCH})"
if [ "$1" != "porcelain" ]
then
  mvn clean install -B -Dcoverage.reports.dir=`pwd`/target/all --settings config/src/main/resources/ci/settings.xml
fi

echo "SonarQube analysis (${TRAVIS_BRANCH})"
if [ "$1" != "porcelain" ]
then
  mvn sonar:sonar -B -Dsonar.branch=$TRAVIS_BRANCH -Dcoverage.reports.dir=`pwd`/target/all --settings config/src/main/resources/ci/settings.xml
fi