#!/usr/bin/env bash
set -euo pipefail

# Preconfigured JWTs (override via environment if needed)
ADMIN_JWT=${ADMIN_JWT:-"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aXRvIiwidXNlcklkIjoiMSIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzU4Mjc2MTMxLCJleHAiOjE3NTgzNjI1MzF9.RBzVuUDRE_te6LimHsIlVe5sKFeD-YPvVkI9e4zufUM"}
USER_JWT=${USER_JWT:-"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aXRvMSIsInVzZXJJZCI6IjIiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzU4Mjc2MTcwLCJleHAiOjE3NTgzNjI1NzB9.4RNTyEm87EVS-RPZt00Oj6ijZuGSjBvwgHQXZ2XJHlY"}

BASE_ACC=http://localhost:8088
BASE_PROD=http://localhost:8089

log() { echo "[$(date +%H:%M:%S)] $*"; }

log "Ensure services are up"
docker start account-service-app >/dev/null 2>&1 || true
docker start product-service-app >/dev/null 2>&1 || true
sleep 4

log "== CREATE PRODUCT (ADMIN) =="
CREATE=$(curl -sS -X POST "$BASE_PROD/api/product" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${ADMIN_JWT}" \
  --data '{"name":"E2E Prod","description":"E2E","price":88.88}')
echo "$CREATE" > /tmp/create.json
if command -v jq >/dev/null 2>&1; then cat /tmp/create.json | jq .; else cat /tmp/create.json; fi
PROD_ID=$(cat /tmp/create.json | jq -r .id 2>/dev/null || echo)

if [[ -n "${PROD_ID}" && "${PROD_ID}" != "null" ]]; then
  log "== UPDATE PRODUCT (ADMIN) id=$PROD_ID =="
  UPDATE=$(curl -sS -X PATCH "$BASE_PROD/api/product/$PROD_ID" \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${ADMIN_JWT}" \
    --data '{"name":"E2E Prod v2"}')
  if command -v jq >/dev/null 2>&1; then echo "$UPDATE" | jq .; else echo "$UPDATE"; fi
else
  log "No product id returned from create"
fi

log "== ASYNC CONFIG STATUS (ADMIN) =="
curl -sS "$BASE_PROD/api/async-test/config-status" -H "Authorization: Bearer ${ADMIN_JWT}" | cat

echo
log "== KAFKA USER EVENT (open endpoint) =="
curl -sS -X POST "$BASE_PROD/api/user" | cat

echo
log "== CIRCUIT BREAKER TEST: stop account-service =="
docker stop account-service-app >/dev/null 2>&1 || true
sleep 2
log "Call product endpoint (should fallback)"
curl -sS "$BASE_PROD/api/product?userId=1" | head -c 500 | cat

echo
log "Restart account-service"
docker start account-service-app >/dev/null 2>&1 || true
sleep 4

log "== ACCOUNT-SERVICE LOGS (Kafka/user/product) =="
docker logs --tail=200 account-service-app 2>/dev/null | grep -Ei "product event|kafka|user" -n | tail -n 30 || true

echo
log "== PRODUCT-SERVICE LOGS (producer/fallback) =="
docker logs --tail=200 product-service-app 2>/dev/null | grep -Ei "product.*kafka|fallback|circuit" -n | tail -n 30 || true

#!/usr/bin/env bash
set -euo pipefail

ADMIN_JWT=${ADMIN_JWT:-"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aXRvIiwidXNlcklkIjoiMSIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzU4Mjc2MTMxLCJleHAiOjE3NTgzNjI1MzF9.RBzVuUDRE_te6LimHsIlVe5sKFeD-YPvVkI9e4zufUM"}
USER_JWT=${USER_JWT:-"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aXRvMSIsInVzZXJJZCI6IjIiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzU4Mjc2MTcwLCJleHAiOjE3NTgzNjI1NzB9.4RNTyEm87EVS-RPZt00Oj6ijZuGSjBvwgHQXZ2XJHlY"}
BASE_ACC=http://localhost:8088
BASE_PROD=http://localhost:8089

log() { echo "[$(date +%H:%M:%S)] $*"; }

# Ensure account-service is running
if ! docker ps --format {{.Names}} | grep -q ^account-service-app; then
  log "Starting account-service-app..."
  docker start account-service-app >/dev/null || true
  sleep 5
fi

log "== CREATE PRODUCT (ADMIN) =="
CREATE=$(curl -sS -X POST "$BASE_PROD/api/product" \
  -H Content-Type:
