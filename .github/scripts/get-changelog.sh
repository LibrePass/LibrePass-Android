#!/bin/sh

VERSION_CODE=$(sed -n 's/.*versionCode\s*=\s*\([0-9]\+\).*/\1/p' app/build.gradle.kts)
CHANGELOG=$(cat fastlane/metadata/android/en-US/changelogs/$VERSION_CODE.txt)

echo -n "$CHANGELOG"
