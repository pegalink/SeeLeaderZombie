#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PARENT_DIR="$(dirname "${SCRIPT_DIR}")"
BUILDS_DIR="${PARENT_DIR}/builds"

mkdir -p "${BUILDS_DIR}"

echo "=========================================="
echo " Building all SeeLeaderZombie mod variants"
echo " Target Output: ${BUILDS_DIR}"
echo "=========================================="

PROJECTS=(
    "SeeLeaderZombie-main"
    "SeeLeaderZombie-NeoForge-26.1"
    "SeeLeaderZombie-NeoForge-26.1.1"
    "SeeLeaderZombie-NeoForge-26.1.2"
    "SeeLeaderZombie-NeoForge-26.2"
)

for proj in "${PROJECTS[@]}"; do
    PROJ_PATH="${PARENT_DIR}/${proj}"
    if [ -d "${PROJ_PATH}" ]; then
        echo ""
        echo "--> Building ${proj}..."
        # Ensure Gradle wrapper is present in the project directory
        if [ ! -f "${PROJ_PATH}/gradlew" ] && [ -f "${SCRIPT_DIR}/gradlew" ]; then
            cp "${SCRIPT_DIR}/gradlew" "${PROJ_PATH}/"
            [ -f "${SCRIPT_DIR}/gradlew.bat" ] && cp "${SCRIPT_DIR}/gradlew.bat" "${PROJ_PATH}/"
            [ -d "${SCRIPT_DIR}/gradle" ] && cp -r "${SCRIPT_DIR}/gradle" "${PROJ_PATH}/"
        fi

        cd "${PROJ_PATH}"
        if [ -f "./gradlew" ]; then
            chmod +x gradlew || true
            ./gradlew build --no-daemon
        elif [ -f "${SCRIPT_DIR}/gradlew" ]; then
            chmod +x "${SCRIPT_DIR}/gradlew" || true
            "${SCRIPT_DIR}/gradlew" build --no-daemon
        elif command -v gradle >/dev/null 2>&1; then
            gradle build
        else
            echo "Error: Neither gradlew wrapper nor system 'gradle' command was found."
            exit 1
        fi
        
        if [ -d "build/libs" ]; then
            find build/libs -maxdepth 1 -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -exec cp {} "${BUILDS_DIR}/" \;
        fi
    else
        echo "Warning: Directory ${proj} not found, skipping."
    fi
done

echo ""
echo "=========================================="
echo " Build process completed!"
echo " Compiled JARs gathered in: ${BUILDS_DIR}"
echo "=========================================="
ls -la "${BUILDS_DIR}"
