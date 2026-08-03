#!/usr/bin/env bash
#
# Builds every SeeLeaderZombie variant (each loader x each supported Minecraft version)
# from the single source tree and gathers the jars into builds/.
#
#   ./build_all.sh                              # everything
#   ./build_all.sh --loader fabric              # one loader, every version
#   ./build_all.sh --mc 26.1.2 --mc 26.2        # every loader, selected versions
#
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${ROOT_DIR}"

OUTPUT_DIR="${ROOT_DIR}/builds"
ALL_LOADERS=(neoforge fabric)

mapfile -t ALL_VERSIONS < <(
    find versions -maxdepth 1 -name '*.properties' -printf '%f\n' | sed 's/\.properties$//' | sort -V
)

if [ ${#ALL_VERSIONS[@]} -eq 0 ]; then
    echo "Error: no version definitions found in versions/" >&2
    exit 1
fi

loaders=()
versions=()

# Not every Minecraft version has both loaders - each version file says which ones it builds.
loaders_for_version() {
    local declared
    declared=$(sed -n 's/^loaders=//p' "${ROOT_DIR}/versions/$1.properties" | tail -n 1)
    if [ -z "${declared}" ]; then
        echo "${ALL_LOADERS[@]}"
    else
        echo "${declared}" | tr ',' ' '
    fi
}

version_supports_loader() {
    local supported
    for supported in $(loaders_for_version "$1"); do
        [ "${supported}" = "$2" ] && return 0
    done
    return 1
}

usage() {
    cat >&2 <<EOF
Usage: $(basename "$0") [--loader <${ALL_LOADERS[*]}>]... [--mc <version>]...

Supported Minecraft versions: ${ALL_VERSIONS[*]}
With no arguments, every loader is built for every version.
EOF
}

while [ $# -gt 0 ]; do
    case "$1" in
        --loader)
            [ $# -ge 2 ] || { echo "Error: --loader needs a value" >&2; exit 1; }
            loaders+=("$2"); shift 2 ;;
        --mc)
            [ $# -ge 2 ] || { echo "Error: --mc needs a value" >&2; exit 1; }
            versions+=("$2"); shift 2 ;;
        -h|--help)
            usage; exit 0 ;;
        *)
            echo "Error: unknown argument '$1'" >&2; usage; exit 1 ;;
    esac
done

[ ${#loaders[@]} -eq 0 ] && loaders=("${ALL_LOADERS[@]}")
[ ${#versions[@]} -eq 0 ] && versions=("${ALL_VERSIONS[@]}")

for loader in "${loaders[@]}"; do
    if [ ! -d "${ROOT_DIR}/${loader}" ]; then
        echo "Error: unknown loader '${loader}' (expected one of: ${ALL_LOADERS[*]})" >&2
        exit 1
    fi
done

for version in "${versions[@]}"; do
    if [ ! -f "${ROOT_DIR}/versions/${version}.properties" ]; then
        echo "Error: unknown Minecraft version '${version}' (expected one of: ${ALL_VERSIONS[*]})" >&2
        exit 1
    fi
done

if [ ! -x ./gradlew ]; then
    chmod +x ./gradlew
fi

mkdir -p "${OUTPUT_DIR}"

echo "=========================================="
echo " Building SeeLeaderZombie"
echo " Loaders:  ${loaders[*]}"
echo " Versions: ${versions[*]}"
echo " Output:   ${OUTPUT_DIR}"
echo "=========================================="

failures=()

for version in "${versions[@]}"; do
    for loader in "${loaders[@]}"; do
        if ! version_supports_loader "${version}" "${loader}"; then
            echo ""
            echo "--> skipping ${loader} / Minecraft ${version} (that version builds for: $(loaders_for_version "${version}"))"
            continue
        fi

        echo ""
        echo "--> ${loader} / Minecraft ${version}"

        # Each variant reuses the same build directory, so clear the jars first and only
        # collect what this run produced.
        rm -rf "${ROOT_DIR}/${loader}/build/libs"

        # -Ploaders keeps each variant independent: a problem with one loader's Gradle plugin
        # cannot fail the other loader's build.
        if ./gradlew ":${loader}:build" -Pmc="${version}" -Ploaders="${loader}" --no-daemon; then
            find "${ROOT_DIR}/${loader}/build/libs" -maxdepth 1 -name '*.jar' \
                ! -name '*-sources.jar' ! -name '*-javadoc.jar' \
                -exec cp {} "${OUTPUT_DIR}/" \;
        else
            echo "!!! FAILED: ${loader} / ${version}" >&2
            failures+=("${loader}/${version}")
        fi
    done
done

echo ""
echo "=========================================="
if [ ${#failures[@]} -gt 0 ]; then
    echo " Build finished with failures: ${failures[*]}"
else
    echo " Build process completed successfully!"
fi
echo " Compiled JARs gathered in: ${OUTPUT_DIR}"
echo "=========================================="
ls -la "${OUTPUT_DIR}"

[ ${#failures[@]} -eq 0 ]
