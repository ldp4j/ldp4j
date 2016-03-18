#!/bin/bash
# Avoid aborting script on failure
set +e

function logCheckResults() {
  case "${DEBUG}" in
    trace | verbose )
      echo "-> Trace:"
      cat log.txt
      echo "-> Response:"
      cat data.txt
      ;;
    *               )
      :
      ;;
  esac
}

function checkWithCurl() {
  if [ "${DEBUG}" = "verbose" ];
  then
    curl --version
  fi
  curl --head --silent --verbose --connect-timeout 5 "$1" > data.txt 2> log.txt
  error=$?
  logCheckResults
  return "$error"
}

function checkWithWget() {
  if [ "${DEBUG}" = "verbose" ];
  then
    wget --version
  fi
  # Write response (and headers) to 'data.txt', (verbose) activity to 'log.txt'
  # including additional debug entries (e.g., requests and responses), with a
  # generic timeout of 5s, and retry only once.
  # In addition, discard the additional output (both standard and error)
  wget --output-document=data.txt --output-file=log.txt --verbose --debug --timeout=5 --tries 1 --save-headers "$1" > error.log 2>&1
  error=$?
  if [ "$error" != "0" ];
  then
    # If a failure happens, but the server sent a response...
    if [ "$(grep -c "response begin" log.txt)" -gt 0 ]
    then
      # ... we extract the response from the log...
      from=$(awk '/response begin/{ print NR; exit }' log.txt)
      to=$(awk '/response end/{ print NR; exit }' log.txt)
      head -n $((to - 1)) log.txt | tail -n $((to - from - 1)) > data.txt
      # ... and clear the failure status.
      error=0
    fi
  fi
  logCheckResults
  return "$error"
}

function checkSonarQubeServer() {
  echo "Checking SonarQube Server..."

  case "${CHECKER}" in
    curl ) checkWithCurl "$1";;
    *    ) checkWithWget "$1";;
  esac

  error=$?
  if [ "$error" = "0" ];
  then
    status=$(head -n 1 data.txt | awk '{print $2}')
    if [ "$status" != "200" ];
    then
      echo "SonarQube Server is not available (response status code: $status)"
      error=1
    fi
  else
    echo "Could not connect to SonarQube Server: "
    cat log.txt
    error=2
  fi
  rm data.txt
  rm log.txt
  return "$error"
}

function analyzeBranch() {
  checkSonarQubeServer "$1"
  if [ "$?" = "0" ];
  then
    if [ "$2" != "porcelain" ];
    then
      echo "Executing SonarQube analysis (${TRAVIS_BRANCH})..."
      # If SSL network failures happen, execute the analysis with -Djavax.net.debug=all
      mvn sonar:sonar -B -Dsonar.branch="$TRAVIS_BRANCH" -Dcoverage.reports.dir="$(pwd)/target/all" --settings config/src/main/resources/ci/settings.xml
    else
      echo "Skipped SonarQube analysis (${TRAVIS_BRANCH}): Porcelain"
    fi
  else
    echo "Skipped SonarQube analysis (${TRAVIS_BRANCH})"
  fi
}

function skipBranchAnalysis() {
  echo "Skipped SonarQube analysis (${TRAVIS_BRANCH}): Non Q.A. branch"
}

function skipPullRequestAnalysis() {
  echo "Skipped SonarQube analysis (${TRAVIS_BRANCH}): Pull request"
}

function skipBuild() {
  echo "Skipping build..."
}

function skipSonarQubeAnalysis() {
  echo "Skipping SonarQube analysis..."
}

function runSonarQubeAnalysis() {
  if [ "${TRAVIS_PULL_REQUEST}" = "false" ];
  then
    case "${TRAVIS_BRANCH}" in
      master | develop ) analyzeBranch "$1" "$2";;
      feature\/*       ) analyzeBranch "$1" "$2";;
      *                ) skipBranchAnalysis ;;
    esac
  else
    skipPullRequestAnalysis
  fi
}

if [ "${DEBUG}" = "trace" ];
then
  set -x
fi

server=http://analysis.ldp4j.org/sonar/

case "${CI}" in
  skip      ) skipBuild ;;
  noqa      ) skipSonarQubeAnalysis ;;
  porcelain ) runSonarQubeAnalysis "$server" porcelain ;;
  *         ) runSonarQubeAnalysis "$server" "$1" ;;
esac

set +x

return 0
