FROM eclipse-temurin:21-jdk AS builder

ENV DEBIAN_FRONTEND=noninteractive

ENV TOMCAT_VERSION=9.0.118
ENV CATALINA_HOME=/opt/tomcat
RUN mkdir -p $CATALINA_HOME && \
    curl -fsSL https://dlcdn.apache.org/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz | \
    tar xz -C $CATALINA_HOME --strip-components=1 && \
    rm -rf $CATALINA_HOME/webapps/*

RUN mkdir -p /app/lib
WORKDIR /download
RUN curl -fsSL -o derby.jar \
      https://repo1.maven.org/maven2/org/apache/derby/derby/10.17.1.0/derby-10.17.1.0.jar && \
    curl -fsSL -o derbyclient.jar \
      https://repo1.maven.org/maven2/org/apache/derby/derbyclient/10.17.1.0/derbyclient-10.17.1.0.jar && \
    curl -fsSL -o derbyshared.jar \
      https://repo1.maven.org/maven2/org/apache/derby/derbyshared/10.17.1.0/derbyshared-10.17.1.0.jar && \
    curl -fsSL -o javax.mail.jar \
      https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar && \
    curl -fsSL -o javax.servlet-api.jar \
      https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar && \
    curl -fsSL -o javax.persistence-api.jar \
      https://repo1.maven.org/maven2/javax/persistence/javax.persistence-api/2.2/javax.persistence-api-2.2.jar && \
    curl -fsSL -o jstl.jar \
      https://repo1.maven.org/maven2/javax/servlet/jstl/1.2/jstl-1.2.jar && \
    curl -fsSL -o javax.annotation-api.jar \
      https://repo1.maven.org/maven2/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar && \
    curl -fsSL -o javax.activation.jar \
      https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar

RUN for f in *.jar; do cp "$f" /app/lib/; done

COPY . /app
WORKDIR /app

RUN bash deploy/docker-build.sh

RUN cp dist/Tickify-SWP-Web-App.war $CATALINA_HOME/webapps/tickify.war && \
    mkdir -p $CATALINA_HOME/conf/Catalina/localhost && \
    cp deploy/tomcat-context.xml $CATALINA_HOME/conf/Catalina/localhost/tickify.xml

FROM eclipse-temurin:21-jre

ENV CATALINA_HOME=/opt/tomcat
ENV TICKIFY_DATA_DIR=/opt/tickify/data
ENV TICKIFY_DB_MODE=embedded
ENV TICKIFY_DB_NAME=/opt/tickify/data/tickifyDB
ENV TICKIFY_DB_USER=tickify
ENV TICKIFY_DB_PASSWORD=tickifypass
ENV TICKIFY_DB_POOL_SIZE=10
ENV TICKIFY_APP_BASE_URL=https://tickify.onrender.com

RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder $CATALINA_HOME $CATALINA_HOME
COPY deploy/docker-setenv.sh $CATALINA_HOME/bin/setenv.sh

RUN chmod +x $CATALINA_HOME/bin/*.sh && \
    mkdir -p $TICKIFY_DATA_DIR

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/tickify/ || exit 1

CMD ["/opt/tomcat/bin/catalina.sh", "run"]
