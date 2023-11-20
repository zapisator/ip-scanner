# Используем образ OpenJDK для Java 8 на основе Alpine Linux
FROM adoptopenjdk/openjdk8:alpine-jre

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Обновляем список доступных пакетов
RUN apk update

# Устанавливаем openjdk8
RUN apk add openjdk8

ARG JAR_FILE

# Копируем JAR-файл с приложением внутрь контейнера
COPY ./target/${JAR_FILE} app.jar

# Команда для запуска приложения при старте контейнера
CMD ["java", "-jar", "app.jar"]

