#!/usr/bin/env bash
set -euo pipefail

VERSION=9.5.0
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CACHE="${ROOT}/.gradle-bootstrap"
DIST="${CACHE}/gradle-${VERSION}"
ZIP="${CACHE}/gradle-${VERSION}-bin.zip"
URL="https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"
SHA_URL="${URL}.sha256"

if [[ ! -x "${DIST}/bin/gradle" ]]; then
  mkdir -p "${CACHE}"
  echo "Bootstrapping Gradle ${VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 -o "${ZIP}" "${URL}"
    EXPECTED="$(curl -fsSL "${SHA_URL}" | tr -d '[:space:]')"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "${ZIP}" "${URL}"
    EXPECTED="$(wget -qO- "${SHA_URL}" | tr -d '[:space:]')"
  else
    echo "Need curl or wget." >&2
    exit 1
  fi

  ACTUAL="$(sha256sum "${ZIP}" | awk '{print $1}')"
  [[ "${ACTUAL}" == "${EXPECTED}" ]] || {
    echo "Gradle checksum mismatch." >&2
    rm -f "${ZIP}"
    exit 1
  }

  unzip -q -o "${ZIP}" -d "${CACHE}"
fi

exec "${DIST}/bin/gradle" "$@"
