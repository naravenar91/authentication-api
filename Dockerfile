# Usamos el JRE en lugar del JDK para el runtime (más ligero y seguro)
FROM eclipse-temurin:17-jre-alpine

# Creamos un usuario de sistema para no ejecutar la app como root (seguridad)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Definimos el argumento del nombre del archivo JAR
ARG JAR_FILE=target/auth-api-0.0.1-SNAPSHOT.jar

# Copiamos el JAR específico
COPY ${JAR_FILE} app.jar

# Optimizaciones de memoria para contenedores Java
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app.jar"]