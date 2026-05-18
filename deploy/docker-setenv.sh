#!/bin/bash
export JAVA_OPTS="-Xms256m -Xmx512m \
  -Dtickify.db.mode=${TICKIFY_DB_MODE:-embedded} \
  -Dtickify.db.name=${TICKIFY_DB_NAME:-/opt/tickify/data/tickifyDB} \
  -Dtickify.db.user=${TICKIFY_DB_USER:-tickify} \
  -Dtickify.db.password=${TICKIFY_DB_PASSWORD:-tickifypass} \
  -Dtickify.db.poolSize=${TICKIFY_DB_POOL_SIZE:-10} \
  -Dtickify.reset.token.secret=${TICKIFY_RESET_TOKEN_SECRET:-tickify-prod-secret} \
  -Dtickify.smtp.host=${TICKIFY_SMTP_HOST:-smtp.gmail.com} \
  -Dtickify.smtp.port=${TICKIFY_SMTP_PORT:-587} \
  -Dtickify.smtp.user=${TICKIFY_SMTP_USER:-} \
  -Dtickify.smtp.password=${TICKIFY_SMTP_PASSWORD:-} \
  -Dtickify.smtp.from=${TICKIFY_SMTP_FROM:-} \
  -Dtickify.app.baseUrl=${TICKIFY_APP_BASE_URL:-https://tickify.onrender.com} \
  ${CUSTOM_JAVA_OPTS:-}"
