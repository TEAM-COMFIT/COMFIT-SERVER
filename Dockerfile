# Java 21 베이스 이미지
FROM eclipse-temurin:21-jdk-slim

# 작업 디렉터리 설정
WORKDIR /app

# Gradle wrapper와 설정 파일 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# JAR 파일 위치 확인 및 복사 (plain.jar 제외)
RUN find /app/build/libs -name "*.jar" ! -name "*plain.jar" -exec cp {} /app/app.jar \;

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]