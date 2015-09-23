#!/bin/bash

mode=$1

if [ "$mode" = "porcelain" ]
then
  echo Simulating Travis CI execution...
else
  echo Executing Travis CI build...
fi

if [ "${TRAVIS_PULL_REQUEST}" = "false" ];
then
  case "${TRAVIS_BRANCH}" in
    master)   ./.travis/deploy.sh $mode;;
    develop)  ./.travis/install.sh $mode;;
    feature*) ./.travis/install.sh $mode;;
    *)        ./.travis/build.sh $mode;;
  esac
else
  .travis/build.sh $mode
fi