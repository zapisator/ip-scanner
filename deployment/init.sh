#!/usr/bin/env bash
# Установить опции для выхода при ошибке, необъявленной переменной или ошибке в конвейере
set -euo pipefail
# Установить переменные для имени скрипта
SCRIPT_NAME="$0"
# Определить функцию для вывода отладочной информации
function log () {
  # Получить имя функции, из которой вызывается лог
  local -r function_name="$1"
  # Получить сообщение, которое нужно вывести
  local -r message="$2"
  # Вывести сообщение с датой, временем и именем функции
  printf "%s: [%s # %s] %s\n" "$(date +"%Y-%m-%dT%H:%M:%S.%3N")" "$SCRIPT_NAME" "$function_name" "$message" >&2
}
# Функция, которая получает абсолютный путь к скрипту и устанавливает его в переменную окружения
function get_script_path () {
  log "get_script_path" "Получаем относительный путь к скрипту"
  local script_path="$(dirname -- "${SCRIPT_NAME}")"
  log "get_script_path" "Относительный путь к скрипту: ${script_path}"
  log "get_script_path" "Преобразовать его в абсолютный путь"
  script_path="$(cd -- "${script_path}" && pwd)"
  log "get_script_path" "Абсолютный путь к скрипту: ${script_path}"
  log "get_script_path" "Проверяем, что путь доступен"
  if [[ -z "${script_path}" ]] ; then
    log "get_script_path" "Ошибка: не удалось получить путь к скрипту"
    return 1
  fi
  log "get_script_path" "Путь доступен. Возращаем значение пути: ${script_path}"
  printf '%s\n' "${script_path}"
}
# Функция, которая получает значение JAR_FILE из docker-compose.yml и устанавливает его в переменную окружения
get_jar_file () {
  log "get_jar_file" "Получаем от docker compose значение переменной окружения JAR_FILE"
  local jar_file=$(docker compose --env-file "$ENV_FILE" config | grep -m 1 JAR_FILE | awk -F ': ' '{print $2}')
  log "get_jar_file" "Значение JAR_FILE: "$jar_file
  log "get_jar_file" "Проверяем, что значение не пустое: "$jar_file
  if [[ -z "$jar_file" ]]; then
    log "get_jar_file" "Не удалось найти переменную JAR_FILE в файле docker-compose.yml"
    return 1
  fi
  log "get_jar_file" "JAR_FILE существует. Возращаем его значение: "$jar_file
  printf '%s\n' "$jar_file"
}
# Получить путь к скрипту и становить переменные для папок проекта и развертывания и для названий файлов и папок
SCRIPT_PATH=$(get_script_path)
PROJECT_FOLDER="$(dirname "$SCRIPT_PATH")"
DEPLOYMENT_FOLDER="deployment"
IMAGE_NAME="deployment-ip-scanner"
ENV_FILE="$PROJECT_FOLDER/$DEPLOYMENT_FOLDER/.env"
JAR_FILE=$(get_jar_file)
DOCKER_FILE="docker-compose.yaml"
# Вывести значения переменных
log "init.sh" "SCRIPT_NAME="$SCRIPT_NAME
log "init.sh" "SCRIPT_PATH="$SCRIPT_PATH
log "init.sh" "PROJECT_FOLDER="$PROJECT_FOLDER
log "init.sh" "DEPLOYMENT_FOLDER="$DEPLOYMENT_FOLDER
log "init.sh" "JAR_FILE="$JAR_FILE
log "init.sh" "DOCKER_FILE="$DOCKER_FILE
log "init.sh" "ENV_FILE="$ENV_FILE
# Определить функцию для сборки jar-файла, если он отсутствует или устарел
function build_jar_file () {
  log "build_jar_file" "Переходим в папку проекта '$PROJECT_FOLDER'"
  cd "$PROJECT_FOLDER"
  log "build_jar_file" "Начинаем сборку jar-файла 'target/"$JAR_FILE". Проверяем есть ли этот файл"
  if [[ -f target/$JAR_FILE ]]; then
    log "build_jar_file" "jar-файл существует, проверяем были ли его изменения"
    if_old_rebuild_jar_file
  else
    log "build_jar_file" "jar-файла не существует. Нужно построить новый."
    build_new_jar_file
  fi
  log "build_jar_file" "Закончили сборку jar-файла"
}

function build_new_jar_file () {
  log "build_new_jar_file" "Файла не было. Собираем jar-файл заново с ./mvnw package --quiet"
  ./mvnw package --quiet || { log "build_new_jar_file" "Сборка неуспешна" >&2; exit 1; }
  log "build_new_jar_file" "Сборка успешна"
}

function remove_jar_file() {
    log "remove_jar_file" "Удаляем старый jar-файл"
    rm target/$JAR_FILE
}

function if_old_rebuild_jar_file () {
  if find src -newer target/$JAR_FILE | grep -q .; then
    log "if_old_rebuild_jar_file" "Он 'target/$JAR_FILE' существует и были изменения исходного кода. Удаляем старый jar-файл"
    remove_jar_file
    build_new_jar_file
  else
    log "if_old_rebuild_jar_file" "Jar-файл не изменился"
  fi
}

function rebuild_and_run_containers () {
  log "rebuild_and_run_containers" "Собираем образ"
  docker compose --env-file "$ENV_FILE" build
  log "rebuild_and_run_containers" "Запускаем контейнер"
  docker compose --env-file "$ENV_FILE" up --force-recreate --no-deps -d
}

function is_image_fresh () {
  log "is_image_fresh" "Проверяем, является ли образ свежим: '$IMAGE_NAME'"
  local image_date="$(date -d "$(docker image inspect $IMAGE_NAME | grep LastTagTime | awk -F ' ' '{gsub(/"/,"",$2); print $2}')" +%FT%T)"
  log "is_image_fresh" "Дата образа   : '$image_date'"
  local jar_date="$(date -d "$(stat -c %y $PROJECT_FOLDER/target/$JAR_FILE)" +%FT%T)"
  log "is_image_fresh" "Дата jar файла: '$jar_date'"
  if [[ "$image_date" < "$jar_date" ]]; then
    log "is_image_fresh" "Образ '$IMAGE_NAME' устарел"
    return 1
  else
    log "is_image_fresh" "Образ свежий"
    return 0
  fi
}

function if_old_rebuild_container () {
  log "if_old_rebuild_container" "Проверяем, является ли образ свежим"
  if is_image_fresh; then
    log "if_old_rebuild_container" "Образ свежий. Запускаем контейнер"
    docker compose --env-file "$ENV_FILE" up -d
  else
    log "if_old_rebuild_container" "Образ устарел. Пересобираем и запускаем контейнер"
    rebuild_and_run_containers
  fi
}

function build_and_run_containers () {
  log "build_and_run_containers" "Переходим в папку развертывания: '$DEPLOYMENT_FOLDER'"
  cd "$DEPLOYMENT_FOLDER"
  log "build_and_run_containers" "Начинаем сборку и запуск контейнеров. Проверяем есть ли образ '$IMAGE_NAME'"
  if [[ -z $(docker image inspect $IMAGE_NAME) ]]; then
    log "build_and_run_containers" "Образа '$IMAGE_NAME' не существует"
    rebuild_and_run_containers
  else
    log "build_and_run_containers" "Образ '$IMAGE_NAME' существует. Проверяем, является ли он свежим"
    if_old_rebuild_container
  fi
  log "build_and_run_containers" "Закончили сборку и запуск контейнеров"
}

#build_jar_file
#build_and_run_containers

# Добавить комментарии к скрипту
# Этот скрипт предназначен для автоматизации сборки и запуска проекта ip-scanner
# ip-scanner - это приложение, которое сканирует локальную сеть и выводит информацию о подключенных устройствах
# Для сборки и запуска проекта требуются следующие компоненты:
# - Maven - инструмент для управления зависимостями и сборки Java-проектов
# - Docker - платформа для создания, запуска и управления контейнерами
# - docker compose - инструмент для определения и запуска многоконтейнерных приложений с помощью Docker
# - .env - файл с переменными окружения, которые используются для настройки приложения
# Скрипт выполняет следующие действия:
# - Получает абсолютный путь к скрипту и папкам проекта и развертывания
# - Устанавливает переменные для названий файлов и папок
# - Определяет функцию для вывода отладочной информации
# - Определяет функцию для сборки jar-файла, если он отсутствует или устарел
# - Определяет функцию для сборки и запуска контейнеров, если образ устарел или отсутствует
# - Вызывает функции для сборки jar-файла и контей