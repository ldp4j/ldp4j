#!/bin/bash
set -e

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
  echo "ERROR: Unknown command '${1}'."
  exit 1
}

function prepareBuild() {
  if [ "$#" != "2" ];
  then
    echo "ERROR: No encryption password specified."
    exit 2
  fi

  if [ "$1" != "porcelain" ];
  then
    echo "Preparing Build's bill-of-materials..."
    echo "- Decrypting private key..."
    openssl aes-256-cbc -pass pass:"$2" -in config/src/main/resources/ci/secring.gpg.enc -out local.secring.gpg -d
    echo "- Decrypting public key..."
    openssl aes-256-cbc -pass pass:"$2" -in config/src/main/resources/ci/pubring.gpg.enc -out local.pubring.gpg -d
    if [[ -a .git/shallow ]];
    then
      echo "- Unshallowing Git repository..."
      git fetch --unshallow
    fi
  else
    echo "Skipped preparation of the Build's bill-of-materials"
  fi
}

mode=$1
shift
if [ "$mode" = "porcelain" ];
then
  action=$1
  shift
else
  action=$mode
  mode=execute
fi

case "$action" in
  backup-maven-repo ) backupMavenRepo "$mode" "$@";;
  restore-maven-repo) restoreMavenRepo "$mode" "$@";;
  prepare-build-bom ) prepareBuild "$mode" "$@";;
  *                 ) fail "$action";;
esac
