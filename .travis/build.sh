#!/bin/bash
# Abort script on first failure
set -e

function deploy() {
  if [ "$1" != "porcelain" ];
  then
    echo "Executing Maven deploy (${TRAVIS_BRANCH})..."
    mvn clean deploy -B -Dcodebase.directory=$(pwd) -Dcoverage.reports.dir=$(pwd)/target/all --settings config/src/main/resources/ci/settings.xml
  else
    echo "Skipped Maven deploy (${TRAVIS_BRANCH}): Porcelain"
  fi
}

function install() {
  if [ "$1" != "porcelain" ];
  then
    echo "Executing Maven install (${TRAVIS_BRANCH})..."
    mvn clean install -B -Dcoverage.reports.dir=$(pwd)/target/all --settings config/src/main/resources/ci/settings.xml
  else
    echo "Skipped Maven install (${TRAVIS_BRANCH}): Porcelain"
  fi
}

function runBuild() {
  mode=$1
  if [ "${TRAVIS_PULL_REQUEST}" = "false" ];
  then
    case "${TRAVIS_BRANCH}" in
      master | develop ) deploy "$mode";;
      feature\/*       ) install "$mode";;
      *                ) install "$mode";;
  esac
  else
    install "$mode"
  fi
}

function skipBuild() {
  echo "Skipping build..."
}

if [ "${DEBUG}" = "trace" ];
then
  set -x
fi

case "${CI}" in
  skip      ) skipBuild ;;
  porcelain ) runBuild porcelain ;;
  *         ) runBuild "$1" ;;
esac
