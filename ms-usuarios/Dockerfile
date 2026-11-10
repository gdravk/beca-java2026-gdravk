# dockerfile do ms1 (ms-usuarios), em duas etapas (multi-stage):
# 1) "compilacao": uma imagem com maven + jdk 21 gera o .jar
# 2) "execucao": uma imagem leve, so com o java 21 (jre), roda o .jar
# assim a imagem final nao carrega o maven nem o codigo-fonte, e quem clonar o repositorio
# nao precisa nem ter java instalado: o docker faz tudo

# ---------- etapa 1: compilacao ----------
FROM maven:3.9.11-eclipse-temurin-21 AS compilacao
WORKDIR /app

# primeiro so o pom.xml: o docker guarda essa etapa em cache, entao as dependencias
# so sao baixadas de novo quando o pom.xml muda (e nao a cada mudanca no codigo)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# depois o codigo, e gera o .jar
# -DskipTests: os testes rodam antes, no "mvnw verify" (com o relatorio do jacoco); aqui so empacota
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- etapa 2: execucao ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# usuario comum (sem ser root) pra rodar a aplicacao: se alguem invadir o container, nao vira administrador
RUN useradd --system --no-create-home app
USER app

# so o .jar sai da etapa de compilacao
COPY --from=compilacao /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
