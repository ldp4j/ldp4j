#!/bin/bash

mode=$1

if [ "$mode" = "porcelain" ]
then
  echo Simulating Travis CI execution...
else
  echo Executing Travis CI build...
fi

chmod ugo+x ./.travis/deploy.sh
chmod ugo+x ./.travis/install.sh
chmod ugo+x ./.travis/build.sh

if [ "${TRAVIS_PULL_REQUEST}" = "false" ];
then
  case "${TRAVIS_BRANCH}" in
    master)   ./.travis/deploy.sh $mode;;
    develop)  ./.travis/deploy.sh $mode;;
    feature*) ./.travis/install.sh $mode;;
    *)        ./.travis/build.sh $mode;;
  esac
else
  .travis/build.sh $mode
fi