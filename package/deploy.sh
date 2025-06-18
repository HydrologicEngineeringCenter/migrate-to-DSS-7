#!/usr/bin/env bash
set -euo pipefail

# ──────────────── CONFIGURATION ────────────────
GROUP_ID="mil.army.usace.hec"
ARTIFACT_ID="migrate-to-dss-7"
VERSION="1.0-SNAPSHOT"

# Repository settings
REPO_URL="https://www.hec.usace.army.mil/nexus/repository/maven-snapshots/"
#REPO_URL="https://www.hec.usace.army.mil/nexus/repository/maven-releases/"

# ──────────────── DISTRIBUTION FILES ────────────────
FILES="migrate-to-dss-7-linux.tar.gz,migrate-to-dss-7-mac.zip,migrate-to-dss-7-win.zip"
CLASSIFIERS="linux,mac,win"
TYPES="tar.gz,zip,zip"

# ──────────────── DEPLOY ────────────────
echo "📦 Deploying all artifacts under $GROUP_ID:$ARTIFACT_ID:$VERSION …"

mvn -s settings.xml deploy:deploy-file \
  -DrepositoryId=nexus \
  -Durl="$REPO_URL" \
  -DgroupId="$GROUP_ID" \
  -DartifactId="$ARTIFACT_ID" \
  -Dversion="$VERSION" \
  -Dpackaging=pom \
  -Dfile=pom.xml \
  -Dfiles="$FILES" \
  -Dclassifiers="$CLASSIFIERS" \
  -Dtypes="$TYPES"

echo "All distributions uploaded successfully."