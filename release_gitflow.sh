#!/bin/bash

mkdir -p .local

### Control that the script is run on `develop` branch
branch=`git rev-parse --abbrev-ref HEAD`
if [[ ! "$branch" = "develop" ]];
then
  echo ">> This script must be run under \`develop\` branch"
  exit 1
fi

# Check version format
if [[ ! $1 =~ ^[0-9]+.[0-9]+.[0-9]+((a|b)[0-9]+)?$ ]]; then
  echo "Wrong version format"
  echo "Usage:"
  echo " > ./release.sh <version> <release_description>"
  echo "with:"
  echo " - pre: use for pre-release"
  echo " - rel: for full release"
  echo " - version: x.y.z"
  echo " - release_description: a comment on release"
  exit 1
fi

version=$1
echo "new build version: $version"

RELEASE_OPTS="-DperformRelease -DskipTests -Denv=hsqldb"


echo "**********************************"
echo "* Preparing release..."
echo "**********************************"
mvn -B gitflow:release-start -DreleaseVersion="$version"
if [[ $? -ne 0 ]]; then
    exit 1
fi
echo "Prepare release [OK]"


echo "**********************************"
echo "* Performing release..."
echo "**********************************"
mvn clean deploy "${RELEASE_OPTS}"
if [[ $? -ne 0 ]]; then
    exit 1
fi


echo "**********************************"
echo "* Generating DB..."
echo "**********************************"
dirname=`pwd`
cd $dirname/sumaris-core
version=`grep -m1 -P "\<version>[0-9A−Z.]+(-\w*)?</version>" pom.xml | grep -oP "\d+.\d+.\d+(-\w*)?"`

# Generate the DB (run InitTest class)
mvn -Prun,hsqldb -DskipTests --quiet
if [[ $? -ne 0 ]]; then
    exit 1
fi

# Create ZIP
cd target
zip -q -r "sumaris-db-$version.zip" db
if [[ $? -ne 0 ]]; then
    exit 1
fi
echo "Generate DB [OK]"


cd $dirname
#echo "**********************************"
#echo "* Uploading artifacts to Github..."
#echo "**********************************"
#./github_gitflow.sh pre "$version"
#if [[ $? -ne 0 ]]; then
#    exit 1
#fi
#echo "Upload artifacts to github [OK]"


#echo "**********************************"
#echo "* Pushing changes to upstream..."
#echo "**********************************"
#mvn gitflow:release-finish -DfetchRemote=false
#if [[ $? -ne 0 ]]; then
#    exit 1
#fi

# Pause (if propagation is need between hosted git server and github)
#sleep 10s
#echo "Push changes to upstream [OK]"

#echo "----------------------------------"
#echo "RELEASE finished !"
#echo "----------------------------------"

#echo "Rebuild new SNAPSHOT version..."
#mvn clean install -DskipTests --quiet
#if [[ $? -ne 0 ]]; then
#    exit 1
#fi
#echo "Rebuild new SNAPSHOT version [OK]"


