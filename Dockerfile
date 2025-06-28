FROM openjdk:17-jdk

# 1) CA 패키지 설치 + 디렉터리 생성
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /usr/local/share/ca-certificates

# 2) RDS CA 다운로드 → 시스템 truststore 등록
RUN curl -sSLo /usr/local/share/ca-certificates/rds-ca.pem \
      https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2019/rds-ca-rsa2048-g1.pem \
 && update-ca-certificates

# 3) JDK cacerts 에도 동일 CA 등록
RUN keytool -import -trustcacerts -alias rds-ca \
      -file /usr/local/share/ca-certificates/rds-ca.pem \
      -keystore "${JAVA_HOME}/lib/security/cacerts" \
      -storepass changeit -noprompt

# 4) 애플리케이션 JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
