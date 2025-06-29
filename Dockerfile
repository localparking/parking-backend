FROM openjdk:17-alpine

# 1) 필요한 패키지 설치
RUN apk add --no-cache curl ca-certificates && update-ca-certificates

# 2) RDS CA 다운로드 후 JDK truststore에 등록
RUN set -eux && \
    curl -sSLo /tmp/rds-ca.pem \
         https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2019/rds-ca-rsa2048-g1.pem && \
    keytool -importcert -trustcacerts \
        -alias rds-ca \
        -file /tmp/rds-ca.pem \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit -noprompt && \
    rm /tmp/rds-ca.pem

# 3) 애플리케이션 JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
