#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TEMP="$(mktemp -d)"
trap 'rm -rf "$TEMP"' EXIT
javac -d "$TEMP" \
  app/src/main/java/com/focuslock/app/DayKeys.java \
  app/src/main/java/com/focuslock/app/SafetyPolicy.java \
  app/src/main/java/com/focuslock/app/FocusContent.java \
  app/src/main/java/com/focuslock/app/IntervalCounter.java \
  tests/CoreSmokeTest.java
java -cp "$TEMP" CoreSmokeTest
