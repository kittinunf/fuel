#!/bin/bash

echo $TRAVIS_BRANCH

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying"

  modules=("fuel" "fuel-rxjava" "fuel-jackson" "fuel-gson")
  for i in "${modules[@]}"
  do
    echo "Deploying $i ..."
    ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
  done

  android_modules=("fuel-android" "fuel-livedata")
  for j in "${android_modules[@]}"
  do
    echo "Deploying $j ..."
    ./gradlew :$j:clean :$j:build :$j:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false -x mavenAndroidJavadocs
  done

fi
