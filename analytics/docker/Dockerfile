FROM openjdk:21-ea-14-jdk-slim

RUN apt-get update && apt-get install -y --no-install-recommends wget unzip && rm -rf /var/lib/apt/lists/*

RUN mkdir /data
VOLUME [ "/data" ]

ARG DYNATRACE_ENVIRONMENT
ARG DYNATRACE_TOKEN

WORKDIR .

ARG DT_API_URL="https://$DYNATRACE_ENVIRONMENT.live.dynatrace.com/api"
ARG DT_ONEAGENT_OPTIONS="flavor=default&include=java"
ENV DT_HOME="/opt/dynatrace/oneagent"
RUN mkdir -p "$DT_HOME" && \
    wget -O "$DT_HOME/oneagent.zip" "$DT_API_URL/v1/deployment/installer/agent/unix/paas/latest?Api-Token=$DYNATRACE_TOKEN&$DT_ONEAGENT_OPTIONS" && \
    unzip -d "$DT_HOME" "$DT_HOME/oneagent.zip" && \
    rm "$DT_HOME/oneagent.zip"
ENTRYPOINT [ "/opt/dynatrace/oneagent/dynatrace-agent64.sh" ]

COPY analytics.jar app.jar

CMD ["java", "-jar", "app.jar"]
