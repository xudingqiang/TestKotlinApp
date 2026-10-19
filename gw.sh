#!/bin/bash
# Helper: run Gradle from the workspace-local distribution + writable GRADLE_USER_HOME.
# Usage: ./gw.sh [gradle args...]
cd "$(dirname "$0")" || exit 1
exec env GRADLE_USER_HOME="$PWD/gradle-home" "$PWD/gradle-dist/gradle-9.4.1/bin/gradle" --console=plain "$@"
