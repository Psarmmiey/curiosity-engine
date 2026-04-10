#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
# Licensed under the Apache License, Version 2.0
#

##############################################################################
# Gradle start up script for UN*X
##############################################################################
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

MAX_FD=maximum
warn() { echo "$*"; }
die() { echo; echo "ERROR: $*"; echo; exit 1; }

# OS specific support
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  Darwin*  ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
  NONSTOP* ) nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command
if [ -n "$JAVA_HOME" ]; then
  JAVACMD=$JAVA_HOME/bin/java
  if [ ! -x "$JAVACMD" ]; then
    die "JAVA_HOME is set to an invalid directory: $JAVA_HOME"
  fi
else
  JAVACMD=java
  which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command found."
fi

# Increase the maximum file descriptors
if [ "$cygwin" = "false" ] && [ "$darwin" = "false" ] && [ "$nonstop" = "false" ]; then
  case $MAX_FD in
    max*) MAX_FD=$(ulimit -H -n) ;;
  esac
  ulimit -n "$MAX_FD" 2>/dev/null || warn "Could not set maximum file descriptor limit: $MAX_FD"
fi

# Add default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

exec "$JAVACMD" \
  $DEFAULT_JVM_OPTS \
  $JAVA_OPTS \
  $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
