#!/bin/bash
set -e

SRC_DIR=src/java
WEB_DIR=web
CONF_DIR=src/conf
LIB_DIR=lib
BUILD_DIR=build/web
CLASSES_DIR=$BUILD_DIR/WEB-INF/classes
DIST_DIR=dist
WAR_NAME=Tickify-SWP-Web-App.war

rm -rf $BUILD_DIR $DIST_DIR
mkdir -p $CLASSES_DIR
mkdir -p $DIST_DIR

CP="${LIB_DIR}/derby.jar:${LIB_DIR}/derbyclient.jar:${LIB_DIR}/derbyshared.jar:${LIB_DIR}/javax.mail.jar:${LIB_DIR}/javax.servlet-api.jar:${LIB_DIR}/javax.persistence-api.jar:${LIB_DIR}/javax.annotation-api.jar:${LIB_DIR}/javax.activation.jar:${LIB_DIR}/jstl.jar"

echo "=== Compiling Java sources ==="
find $SRC_DIR -name "*.java" > /tmp/sources.txt
javac -source 8 -target 8 -cp "$CP" -d $CLASSES_DIR @/tmp/sources.txt
rm /tmp/sources.txt
echo "Compilation complete."

echo "=== Copying web resources ==="
cp -r $WEB_DIR/* $BUILD_DIR/ 2>/dev/null || true
cp $CONF_DIR/persistence.xml $CLASSES_DIR/META-INF/ 2>/dev/null || true
mkdir -p $CLASSES_DIR/META-INF
cp $CONF_DIR/MANIFEST.MF $CLASSES_DIR/META-INF/ 2>/dev/null || true

echo "=== Creating WEB-INF/lib ==="
mkdir -p $BUILD_DIR/WEB-INF/lib
cp $LIB_DIR/*.jar $BUILD_DIR/WEB-INF/lib/ 2>/dev/null || true

echo "=== Creating WAR ==="
cd $BUILD_DIR
jar cf ../../$DIST_DIR/$WAR_NAME .
cd ../..

echo "=== WAR created: $DIST_DIR/$WAR_NAME ==="
ls -lh $DIST_DIR/$WAR_NAME
