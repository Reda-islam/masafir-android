#!/bin/sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here if needed
DEFAULT_JVM_OPTS=""

APP_HOME=$(dirname "$0")
APP_NAME="Gradle"

# Look for the Gradle distribution in the wrapper folder
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Gradle wrapper jar not found: $GRADLE_WRAPPER_JAR"
    exit 1
fi

java $DEFAULT_JVM_OPTS -cp "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
