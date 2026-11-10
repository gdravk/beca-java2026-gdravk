# dockerfile do ms2 (ms-transacoes), igual ao do ms1: duas etapas (multi-stage)
# 1) "compilacao": uma imagem com maven + jdk 21 gera o .jar
# 2) "execucao": uma imagem leve, so com o java 21 (jre), roda o .jar

# ---------- etapa 1: compilacao ----------
FROM maven:3.9.11-eclipse-temurin-21 AS compilacao
WORKDIR /app

# primeiro so o pom.xml, pra o docker guardar as dependencias em cache
COPY pom.xml .
RUN mvn -q dependency:go-offline

# depois o codigo, e gera o .jar (os testes rodam antes, no "mvnw verify")
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- etapa 2: execucao ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# usuario comum (sem ser root) pra rodar a aplicacao
RUN useradd --system --no-create-home app
USER app

COPY --from=compilacao /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
