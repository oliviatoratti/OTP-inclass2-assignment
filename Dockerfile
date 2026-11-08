FROM maven:3.9.9-eclipse-temurin-17

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgtk-3-0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    libxinerama1 \
    libxcursor1 \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean test package

CMD ["mvn", "-B", "clean", "compile", "javafx:run"]