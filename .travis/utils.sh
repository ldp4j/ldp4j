#!/bin/bash

function backupMavenRepo() {
  if [ "$1" != "porcelain" ];
  then
    mkdir /tmp/cache-trick
    mv "$HOME/.m2/repository/org/ldp4j" /tmp/cache-trick/
  else
    echo "Skipped Maven Repo backup"
  fi
}

function restoreMavenRepo() {
  if [ "$1" != "porcelain" ];
  then
    mv /tmp/cache-trick/ldp4j "$HOME/.m2/repository/org/"
  else
	  echo "Skipped Maven Repo restoration"
  fi
}

function fail() {
  echo "Unknown command ${1}"
}

action=$1
mode=$2

case "$action" in
  backup-maven-repo)   backupMavenRepo "$mode";;
  restore-maven-repo)  restoreMavenRepo "$mode";;
  *)                   fail "$action";;
esac
