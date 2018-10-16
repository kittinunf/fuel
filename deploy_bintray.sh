#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying at $TRAVIS_BRANCH"

  for i in $(ls -d */);
  do
    m=${i%%/}
    if [[ $m == fuel* ]]; then
      echo ">> Deploying $m ..."
      ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
    fi
  done

fi
