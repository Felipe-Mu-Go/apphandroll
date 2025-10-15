#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd "${SCRIPT_DIR}/.." && pwd)
PROPERTIES_FILE="${ROOT_DIR}/gradle/wrapper/gradle-wrapper.properties"
WRAPPER_JAR="${ROOT_DIR}/gradle/wrapper/gradle-wrapper.jar"

if [[ -f "${WRAPPER_JAR}" ]]; then
  exit 0
fi

if [[ ! -f "${PROPERTIES_FILE}" ]]; then
  echo "Error: no se encontró ${PROPERTIES_FILE}" >&2
  exit 1
fi

EXPECTED_SHA=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --sha256)
      if [[ $# -lt 2 ]]; then
        echo "Error: --sha256 requiere un valor" >&2
        exit 1
      fi
      EXPECTED_SHA="$2"
      shift 2
      ;;
    *)
      echo "Error: argumento desconocido '$1'" >&2
      exit 1
      ;;
  esac
done

read_distribution_url() {
  python3 - "$PROPERTIES_FILE" <<'PY'
import sys
from pathlib import Path

prop_path = Path(sys.argv[1])
for line in prop_path.read_text().splitlines():
    if line.startswith("distributionUrl="):
        url = line.split("=", 1)[1].strip()
        url = url.replace("\\:", ":").replace("\\", "")
        print(url)
        break
else:
    sys.exit("distributionUrl no encontrado")
PY
}

DISTRIBUTION_URL=$(read_distribution_url)

if [[ -z "${DISTRIBUTION_URL}" ]]; then
  echo "Error: distributionUrl no encontrado en ${PROPERTIES_FILE}" >&2
  exit 1
fi

if [[ ! "${DISTRIBUTION_URL}" =~ gradle-([^/-]+)- ]]; then
  echo "Error: no se pudo deducir la versión de Gradle a partir de ${DISTRIBUTION_URL}" >&2
  exit 1
fi

GRADLE_VERSION="${BASH_REMATCH[1]}"
WRAPPER_BASE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.jar"
SHA_BASE_URL="${WRAPPER_BASE_URL}.sha256"

TMP_DIR=$(mktemp -d)
cleanup() {
  rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

fetch() {
  local url="$1"
  local destination="$2"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$url" -o "$destination"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$destination" "$url"
  else
    echo "Error: se requiere curl o wget para descargar ${url}" >&2
    exit 1
  fi
}

compute_sha() {
  local file="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file" | awk '{print $1}'
  else
    echo "Error: no se encontró una utilidad sha256 (sha256sum o shasum)" >&2
    exit 1
  fi
}

if [[ -z "${EXPECTED_SHA}" ]]; then
  SHA_FILE="${TMP_DIR}/gradle-wrapper.jar.sha256"
  fetch "${SHA_BASE_URL}" "${SHA_FILE}"
  EXPECTED_SHA=$(awk 'NF {print $1; exit}' "${SHA_FILE}" | tr -d '\r\n')
fi

if [[ -z "${EXPECTED_SHA}" ]]; then
  echo "Error: no se pudo obtener el hash sha256 esperado" >&2
  exit 1
fi

TMP_JAR="${TMP_DIR}/gradle-wrapper.jar"
fetch "${WRAPPER_BASE_URL}" "${TMP_JAR}"
CALCULATED_SHA=$(compute_sha "${TMP_JAR}")

if [[ "${CALCULATED_SHA}" != "${EXPECTED_SHA}" ]]; then
  echo "Error: el hash sha256 del wrapper no coincide con el esperado" >&2
  echo "Esperado: ${EXPECTED_SHA}" >&2
  echo "Obtenido: ${CALCULATED_SHA}" >&2
  exit 1
fi

mkdir -p "${ROOT_DIR}/gradle/wrapper"
mv "${TMP_JAR}" "${WRAPPER_JAR}"

echo "Gradle Wrapper ${GRADLE_VERSION} descargado correctamente."
