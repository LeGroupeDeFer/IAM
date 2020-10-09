FROM alpine:latest

ENV DEPS="libressl bash wget zlib-dev openjdk11-jre inotify-tools npm" \
    DUMB_INIT_VERSION=1.2.2 \
    SBT_VERSION=1.4.0 \
    FLYWAY_VERSION=7.0.1

# Dependencies
RUN apk add --update --no-cache ${DEPS}

# Dumb-init
RUN wget -O /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v${DUMB_INIT_VERSION}/dumb-init_${DUMB_INIT_VERSION}_amd64 \
 && chmod +x /usr/local/bin/dumb-init

# SBT
RUN mkdir -p /usr/local/sbt \
 && wget -q https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz -O - \
    | gunzip -c - | tar xf - -C /usr/local/sbt --strip-components 1 \
 && ln -sv /usr/local/sbt/bin/sbt /usr/bin/ \
 && cd /tmp \
 && sbt sbtVersion

# Flyway
RUN mkdir -p /usr/local/flyway \
 && wget -q https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/${FLYWAY_VERSION}/flyway-commandline-${FLYWAY_VERSION}-linux-x64.tar.gz -O - \
 | gunzip -c - | tar xf - -C /usr/local/flyway --strip-components 1 \
 && ln -s /usr/local/flyway/flyway /usr/local/bin \
 && rm -rf /usr/local/flyway/jre \
 && ln -s /usr/lib/jvm/java-11-openjdk /usr/local/flyway/jre

# tz
RUN apk add --update --no-cache tzdata \
 && cp /usr/share/zoneinfo/Europe/Brussels /etc/localtime \
 && echo "Europe/Brussels" > /etc/timezone \
 && apk del tzdata

VOLUME /usr/src/app
EXPOSE 8000

ENTRYPOINT ["/usr/local/bin/dumb-init", "--"]
CMD ["/usr/src/app/scripts/entrypoint.sh"]
