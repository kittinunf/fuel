#!/bin/bash
if [[ "$TRAVIS_BRANCH" == release-v* ]]; then
  echo "We're on release branch, deploying"
  ./gradlew :fuel:clean :fuel:build :fuel:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
  ./gradlew :fuel-android:clean :fuel-android:build :fuel-android:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false -x mavenAndroidJavadocs
  ./gradlew :fuel-rxjava:clean :fuel-rxjava:build :fuel-rxjava:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false -x mavenAndroidJavadocs
  ./gradlew :fuel-livedata:clean :fuel-livedata:build :fuel-livedata:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false -x mavenAndroidJavadocs
fi
