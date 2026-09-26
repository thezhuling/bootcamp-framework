#!/usr/bin/env bash
# One-shot bootstrap for the local Nacos in docker-compose.yml. Safe to re-run:
# admin user and namespace are skipped when present, and a config that already exists in
# Nacos is left alone (Nacos is the source of truth once seeded) unless FORCE=1.
#
#   1. wait until the server is ready
#   2. create the admin user (Nacos 3.x ships without one; console asks on first visit otherwise)
#   3. log in, create the shared namespace
#   4. publish every seed/*.yml as dataId=<file name> in that namespace/group;
#      seed-local/<same name> (git-ignored) wins over seed/<name> so real values never hit git
set -euo pipefail

cd "$(dirname "$0")"
[[ -f ./.env ]] || { echo ".env missing — cp .env.example .env and set NACOS_AUTH_TOKEN (openssl rand -base64 48)"; exit 1; }
# shellcheck disable=SC1091
set -a; source ./.env; set +a
[[ -n "${NACOS_AUTH_TOKEN:-}" ]] || { echo "NACOS_AUTH_TOKEN is empty in .env"; exit 1; }

API="http://127.0.0.1:8848/nacos"
CONSOLE="http://127.0.0.1:8880"

echo "waiting for nacos on ${API} ..."
for _ in $(seq 1 60); do
  if [[ "$(curl -s -m 3 -o /dev/null -w '%{http_code}' "${CONSOLE}/v3/console/health/readiness")" == "200" ]]; then
    break
  fi
  sleep 3
done
curl -fs -m 3 "${CONSOLE}/v3/console/health/readiness" >/dev/null || { echo "nacos not ready"; exit 1; }

# --- admin user ------------------------------------------------------------
# server/state reports auth_admin_request=true until an admin exists.
if curl -s -m 5 "${CONSOLE}/v3/console/server/state" | grep -q '"auth_admin_request":"true"'; then
  echo "creating admin user ${NACOS_ADMIN_USER}"
  out=$(curl -s -m 5 -X POST "${API}/v3/auth/user/admin" --data-urlencode "password=${NACOS_ADMIN_PASSWORD}")
  echo "  ${out}"
  echo "${out}" | grep -q '"code":0' || { echo "admin init failed"; exit 1; }
else
  echo "admin user already initialised"
fi

# --- login -----------------------------------------------------------------
login=$(curl -s -m 5 -X POST "${API}/v3/auth/user/login" \
  --data-urlencode "username=${NACOS_ADMIN_USER}" --data-urlencode "password=${NACOS_ADMIN_PASSWORD}")
token=$(sed -nE 's/.*"accessToken":"([^"]+)".*/\1/p' <<<"${login}")
[[ -n "${token}" ]] || { echo "login failed: ${login}"; exit 1; }

# --- namespace -------------------------------------------------------------
if curl -s -m 5 "${API}/v3/admin/core/namespace/list?accessToken=${token}" | grep -q "\"namespace\":\"${NACOS_NAMESPACE}\""; then
  echo "namespace ${NACOS_NAMESPACE} already exists"
else
  echo "creating namespace ${NACOS_NAMESPACE}"
  out=$(curl -s -m 5 -X POST "${API}/v3/admin/core/namespace" \
    --data-urlencode "namespaceId=${NACOS_NAMESPACE}" \
    --data-urlencode "namespaceName=${NACOS_NAMESPACE}" \
    --data-urlencode "namespaceDesc=bootcamp-framework local dev" \
    --data-urlencode "accessToken=${token}")
  echo "  ${out}"
  echo "${out}" | grep -q '"code":0' || { echo "namespace create failed"; exit 1; }
fi

# --- seed configs ----------------------------------------------------------
for f in seed/*.yml; do
  dataId=$(basename "${f}")
  src="${f}"
  [[ -f "seed-local/${dataId}" ]] && src="seed-local/${dataId}"
  if [[ "${FORCE:-0}" != "1" ]] && curl -s -m 5 \
      "${API}/v3/admin/cs/config?dataId=${dataId}&groupName=${NACOS_GROUP}&namespaceId=${NACOS_NAMESPACE}&accessToken=${token}" \
      | grep -q '"code":0'; then
    echo "keeping existing ${dataId} (FORCE=1 to overwrite from ${src})"
    continue
  fi
  echo "publishing ${dataId} from ${src} -> ${NACOS_NAMESPACE}/${NACOS_GROUP}"
  out=$(curl -s -m 5 -X POST "${API}/v3/admin/cs/config" \
    --data-urlencode "dataId=${dataId}" \
    --data-urlencode "groupName=${NACOS_GROUP}" \
    --data-urlencode "namespaceId=${NACOS_NAMESPACE}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${src}" \
    --data-urlencode "accessToken=${token}")
  echo "  ${out}"
  echo "${out}" | grep -q '"code":0' || { echo "publish ${dataId} failed"; exit 1; }
done

echo
echo "done. console: ${CONSOLE}  (login ${NACOS_ADMIN_USER}/${NACOS_ADMIN_PASSWORD})"
echo "services: server-addr 127.0.0.1:8848, namespace ${NACOS_NAMESPACE}, group ${NACOS_GROUP}"
