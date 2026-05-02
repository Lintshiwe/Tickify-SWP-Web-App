#!/bin/bash
export TICKIFY_SMTP_HOST=$(grep '^TICKIFY_SMTP_HOST=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_SMTP_PORT=$(grep '^TICKIFY_SMTP_PORT=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_SMTP_USER=$(grep '^TICKIFY_SMTP_USER=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_SMTP_PASSWORD=$(grep '^TICKIFY_SMTP_PASSWORD=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_SMTP_FROM=$(grep '^TICKIFY_SMTP_FROM=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_RESET_TOKEN_SECRET=$(grep '^TICKIFY_RESET_TOKEN_SECRET=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_APP_BASE_URL=$(grep '^TICKIFY_APP_BASE_URL=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_DB_USER=$(grep '^TICKIFY_DB_USER=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_DB_PASSWORD=$(grep '^TICKIFY_DB_PASSWORD=' /opt/tickify/.env | cut -d= -f2-)
export TICKIFY_DB_POOL_SIZE=$(grep '^TICKIFY_DB_POOL_SIZE=' /opt/tickify/.env | cut -d= -f2-)

# Set defaults for missing values
: ${TICKIFY_SMTP_HOST:=smtp.gmail.com}
: ${TICKIFY_SMTP_PORT:=587}
: ${TICKIFY_DB_USER:=tickify}
: ${TICKIFY_DB_PASSWORD:=tickifypass}
: ${TICKIFY_DB_POOL_SIZE:=10}
: ${TICKIFY_RESET_TOKEN_SECRET:=tickify-prod-secret}
: ${TICKIFY_APP_BASE_URL:=https://tickify.sladedeploy.co.za}

# Pass to JVM args (without password to avoid space issues)
export JAVA_OPTS="-Xms256m -Xmx512m \
  -Dtickify.db.mode=embedded \
  -Dtickify.db.name=/opt/tickify/data/tickifyDB \
  -Dtickify.db.user=$TICKIFY_DB_USER \
  -Dtickify.db.password=$TICKIFY_DB_PASSWORD \
  -Dtickify.db.poolSize=$TICKIFY_DB_POOL_SIZE \
  -Dtickify.reset.token.secret=$TICKIFY_RESET_TOKEN_SECRET \
  -Dtickify.smtp.host=$TICKIFY_SMTP_HOST \
  -Dtickify.smtp.port=$TICKIFY_SMTP_PORT \
  -Dtickify.smtp.user=$TICKIFY_SMTP_USER \
  -Dtickify.smtp.from=$TICKIFY_SMTP_FROM \
  -Dtickify.app.baseUrl=$TICKIFY_APP_BASE_URL"
