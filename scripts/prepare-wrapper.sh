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
DIST_SHA_URL="${DISTRIBUTION_URL}.sha256"

TMP_DIR=$(mktemp -d)
cleanup() {
  rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

fetch() {
  local url="$1"
  local destination="$2"
  local allow_fail="${3:-}"
  local status=0

  if command -v curl >/dev/null 2>&1; then
    set +e
    curl -fL "$url" -o "$destination"
    status=$?
    set -e
  elif command -v wget >/dev/null 2>&1; then
    set +e
    wget -O "$destination" "$url"
    status=$?
    set -e
  else
    echo "Error: se requiere curl o wget para descargar ${url}" >&2
    exit 1
  fi

  if [[ ${status} -ne 0 ]]; then
    if [[ "${allow_fail}" == "allow-fail" ]]; then
      return "${status}"
    fi
    exit "${status}"
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
  SHA_FILE="${TMP_DIR}/distribution.sha256"
  if fetch "${DIST_SHA_URL}" "${SHA_FILE}" "allow-fail"; then
    EXPECTED_SHA=$(tr -d '\r\n' < "${SHA_FILE}")
  else
    echo "Advertencia: no se pudo descargar el archivo de checksum, se omitirá la verificación sha256." >&2
  fi
fi

if [[ -n "${EXPECTED_SHA}" ]]; then
  VERIFY_SHA=true
else
  VERIFY_SHA=false
fi

TMP_DIST="${TMP_DIR}/gradle-distribution.zip"
fetch "${DISTRIBUTION_URL}" "${TMP_DIST}"
if [[ "${VERIFY_SHA}" == true ]]; then
  CALCULATED_SHA=$(compute_sha "${TMP_DIST}")

  if [[ "${CALCULATED_SHA}" != "${EXPECTED_SHA}" ]]; then
    echo "Error: el hash sha256 de la distribución no coincide con el esperado" >&2
    echo "Esperado: ${EXPECTED_SHA}" >&2
    echo "Obtenido: ${CALCULATED_SHA}" >&2
    exit 1
  fi
fi

TMP_JAR="${TMP_DIR}/gradle-wrapper.jar"
python3 - "$TMP_DIST" "$TMP_JAR" <<'PY'
import sys
from pathlib import Path
from zipfile import ZipFile

dist_path = Path(sys.argv[1])
jar_path = Path(sys.argv[2])

with ZipFile(dist_path) as zf:
    try:
        data = zf.read("gradle/wrapper/gradle-wrapper.jar")
    except KeyError as exc:
        raise SystemExit("gradle-wrapper.jar no encontrado en la distribución") from exc

jar_path.write_bytes(data)
PY

mkdir -p "${ROOT_DIR}/gradle/wrapper"
mv "${TMP_JAR}" "${WRAPPER_JAR}"

echo "Gradle Wrapper ${GRADLE_VERSION} descargado correctamente."
