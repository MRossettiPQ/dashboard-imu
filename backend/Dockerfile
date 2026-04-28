# Estágio 1: Build (Compilação)
# Usando Gradle 9.4.0 com JDK 25 em base Alpine
FROM gradle:9.4.0-jdk25-alpine AS build

# Define permissões para o usuário gradle para evitar problemas de escrita
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Executa o build gerando o fast-jar otimizado do Quarkus
# O uso de --no-daemon é recomendado para ambientes de CI/Docker
RUN gradle build -Dquarkus.package.type=fast-jar --no-daemon

# Estágio 2: Run (Execução)
# Usando JDK 25 Alpine para rodar a aplicação
FROM amazoncorretto:25-alpine3.23
# Caso prefira uma versão LTS estável quando disponível, altere para o JRE correspondente

WORKDIR /deployments

# Copia apenas os artefatos necessários gerados pelo Quarkus (camadas de libs e app)
COPY --from=build /home/gradle/src/build/quarkus-app/ /deployments/

# Porta do backend configurada no seu application.yml
EXPOSE 7000

# Variáveis de ambiente padrão que podem ser sobrescritas pelo Docker Compose
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]