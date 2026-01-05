FROM amazoncorretto:21-alpine

WORKDIR /app

# 이미 빌드된 JAR 복사
COPY build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]