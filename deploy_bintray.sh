#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-* ]]; then

  echo "We're on release branch, deploying at $TRAVIS_BRANCH"

  for i in $(ls -d */);
  do
    m=${i%%/}
    if [[ $m == fuel* ]]; then
      echo ">> Deploying $m ..."
      ./gradlew :$m:clean :$m:build :$m:bintrayUpload -PBINTRAY_USER=$BINTRAY_USER -PBINTRAY_KEY=$BINTRAY_KEY -PdryRun=false -Ppublish=true
    fi
  done

fi
