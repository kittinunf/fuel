#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying at $TRAVIS_BRANCH"

  modules=("fuel" "fuel-android" "fuel-rxjava" "fuel-jackson" "fuel-gson" "fuel-livedata" "fuel-moshi" "fuel-coroutines")
  for i in "${modules[@]}"
  do
    echo ">> Deploying $i ..."
    ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
    echo ">> Done deploying for $i"
  done

fi
