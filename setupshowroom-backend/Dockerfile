FROM eclipse-temurin:21-alpine

RUN addgroup setupshowroom && adduser --disabled-password --no-create-home --ingroup setupshowroom setupshowroom
USER setupshowroom

LABEL org.opencontainers.image.description="setup-showroom Backend"
ADD target/backend.jar backend.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/backend.jar", "1>/dev/stdout", "2>/dev/stderr"]
