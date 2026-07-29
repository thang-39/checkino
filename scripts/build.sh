#!/usr/bin/env bash
#
# ng build → copy vào static/app/ → mvn package → một jar duy nhất (DECISIONS.md § D5).
#
# Cố ý là shell script, KHÔNG phải frontend-maven-plugin: D5 và PLAN.md § 2.4 nói cùng một
# câu — với một người thì script đơn giản hơn và không cần plugin.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

STATIC_APP="backend/src/main/resources/static/app"

echo "==> [1/4] npm ci"
npm ci --prefix frontend

echo "==> [2/4] ng build (production)"
npm run build --prefix frontend -- --configuration production

echo "==> [3/4] copy bundle vào $STATIC_APP"
# Angular (application builder) đẻ ra thêm một tầng browser/. Phải copy NỘI DUNG của tầng đó,
# không phải cả dist/frontend/ — nếu sai thì index.html không nằm ở gốc static/app/ và
# fallback SPA fail âm thầm (200 nhưng trang trắng).
SRC="frontend/dist/frontend/browser"
[[ -f "$SRC/index.html" ]] || { echo "LỖI: không thấy $SRC/index.html — output Angular đổi cấu trúc?" >&2; exit 1; }
rm -rf "$STATIC_APP"
mkdir -p "$STATIC_APP"
cp -R "$SRC"/. "$STATIC_APP"/

echo "==> [4/4] mvn package"
mvn -B -f backend/pom.xml package

echo
echo "✓ jar: $(ls -1 backend/target/checkino-*.jar)"
