FROM openjdk:17-alpine

RUN apk add --no-cache curl ca-certificates && \
    curl -sSLo /tmp/rds-ca.pem \
      https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2024/rds-ca-rsa4096-g1.pem && \
    keytool -importcert -trustcacerts -alias rds-ca \
      -file /tmp/rds-ca.pem -keystore "$JAVA_HOME/lib/security/cacerts" \
      -storepass changeit -noprompt

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
