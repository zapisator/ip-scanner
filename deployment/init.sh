#!/usr/bin/env bash

# Установить опции для выхода при ошибке, необъявленной переменной или ошибке в конвейере

set -euo pipefail

# Установить переменные для имени скрипта

SCRIPT_NAME="$0"

function log () {
  local -r timestamp=$(date +"%Y-%m-%dT%H:%M:%S.%3N")
  local -r script_name=$(basename "$0")
  local -r caller_function_name="${FUNCNAME[1]}"
  local -r message="$1"
  printf "%s: [%s # %s] %s\n" "$timestamp" "$script_name" "$caller_function_name" "$message" >&2
}

# Функция, которая получает абсолютный путь к скрипту и устанавливает его в переменную окружения



function get_script_path () {
   log "Получаем относительный путь к скрипту"
   local script_path="$(dirname -- "${SCRIPT_NAME}")"
   log "Относительный путь к скрипту: ${script_path}"
   log "Преобразовать его в абсолютный путь"
   script_path="$(cd -- "${script_path}" && pwd)"
   log "Абсолютный путь к скрипту: ${script_path}"
   log "Проверяем, что путь доступен"
   if [[ -z "${script_path}" ]] ; then
     log "Ошибка: не удалось получить путь к скрипту"
     return 1
   fi
   log "Путь доступен. Возращаем значение пути: ${script_path}"
   printf '%s\n' "${script_path}"
 }


# Функция, которая получает значение JAR_FILE из docker-compose.yml и устанавливает его в переменную окружения

get_jar_file () {
  log "Получаем от docker compose значение переменной окружения JAR_FILE"
  local jar_file=$(docker compose --env-file "$ENV_FILE" config | grep -m 1 JAR_FILE | awk -F ': ' '{print $2}')
  log "Значение JAR_FILE: "$jar_file
  log "Проверяем, что значение не пустое: "$jar_file
  if [[ -z "$jar_file" ]]; then
    log "Не удалось найти переменную JAR_FILE в файле docker-compose.yml"
    return 1
  fi
  log "JAR_FILE существует. Возращаем его значение: "$jar_file
  printf '%s\n' "$jar_file"
}

# Получить путь к скрипту и становить переменные для папок проекта и развертывания и для названий файлов и папок

SCRIPT_PATH=$(get_script_path)
PROJECT_FOLDER="$(dirname "$SCRIPT_PATH")"
DEPLOYMENT_FOLDER=$PROJECT_FOLDER/"deployment"
IMAGE_NAME="deployment-ip-scanner"
ENV_FILE="$DEPLOYMENT_FOLDER/.env"
JAR_FILE=$(get_jar_file)
DOCKER_FILE="docker-compose.yaml"

# Вывести значения переменных

log "" "SCRIPT_NAME="$SCRIPT_NAME
log "" "SCRIPT_PATH="$SCRIPT_PATH
log "" "PROJECT_FOLDER="$PROJECT_FOLDER
log "" "DEPLOYMENT_FOLDER="$DEPLOYMENT_FOLDER
log "" "JAR_FILE="$JAR_FILE
log "" "DOCKER_FILE="$DOCKER_FILE
log "" "ENV_FILE="$ENV_FILE

# Определить функцию для сборки jar-файла, если он отсутствует или устарел

function build_jar_file () {
  log "Получаем путь к папке проекта"
  local project_folder="$PROJECT_FOLDER"
  log "Сохраняем текущую папку в стеке и переходим в папку проекта '${project_folder}'"
  pushd "${project_folder}"
  log "Начинаем сборку jar-файла 'target/${JAR_FILE}'. Проверяем есть ли этот файл"
  if [[ -f target/${JAR_FILE} ]]; then
    log "jar-файл существует, проверяем были ли его изменения"
    rebuild_jar_file_if_needed
  else
    log "jar-файла не существует. Нужно построить новый."
    build_new_jar_file
  fi
  log "Возращаемся в предыдущую папку из стека"
  popd
  log "Закончили сборку jar-файла"
}

function build_new_jar_file () {
  log "Файла не было. Собираем jar-файл заново с ./mvnw package --quiet"
  local result
  result=$(./mvnw package --quiet) || { log "Сборка неуспешна" >&2; exit 1; }
  log "Сборка успешна"
}

function remove_jar_file() {
  log "Получаем название jar-файла"
  local jar_file="$JAR_FILE"
  log "Удаляем старый jar-файл"
  rm target/"$jar_file"
}

function rebuild_jar_file_if_needed () {
  log "Получаем название jar-файла"
  local jar_file="$JAR_FILE"
  if find src -newer target/"$jar_file" | grep -q .; then
    log "Он 'target/$jar_file' существует и были изменения исходного кода. Удаляем старый jar-файл"
    remove_jar_file
    build_new_jar_file
  else
    log "Jar-файл не изменился"
  fi
}

function run_container () {
  local option="$1"
  log "Запускаем контейнер с опцией ${option}"
  docker compose --env-file "${ENV_FILE}" up ${option} -d
}

function rebuild_and_run_containers () {
  log "Получаем имя файла с переменными окружения"
  local env_file="$ENV_FILE"
  log "Собираем образ"
  docker compose --env-file "$env_file" build
  log "Запускаем контейнер"
  run_container --force-recreate --no-deps
}

function is_image_up_to_date () {
  log "Получаем дату образа"
  local image_date="$(date -d "$(docker image inspect $IMAGE_NAME | grep LastTagTime | awk -F ' ' '{gsub(/"/,"",$2); print $2}')" +%FT%T)"
  log "Дата образа   : '$image_date'"
  log "Получаем дату jar-файла"
  local jar_date="$(date -d "$(stat -c %y $PROJECT_FOLDER/target/$JAR_FILE)" +%FT%T)"
  log "Дата jar файла: '$jar_date'"
  log "Сравниваем даты"
  if [[ "$image_date" < "$jar_date" ]]; then
    log "Образ '$IMAGE_NAME' устарел"
    return 1
  else
    log "Образ свежий"
    return 0
  fi
}

function run_container () {
  log "Получаем опцию для запуска контейнера"
  local option="$1"
  log "Запускаем контейнер с опцией $option"
  docker compose --env-file "$ENV_FILE" up "$option" -d
}

function if_old_rebuild_container () {
  log "Проверяем, является ли образ свежим"
  if is_image_up_to_date; then
    log "Образ свежий. Запускаем контейнер"
    docker compose --env-file "$ENV_FILE" up -d
  else
    log "Образ устарел. Пересобираем и запускаем контейнер"
    rebuild_and_run_containers
  fi
}

function build_and_run_containers () {
  log "Получаем путь к папке развертывания"
  local deployment_folder="$DEPLOYMENT_FOLDER"
  log "Сохраняем текущую папку в стеке и переходим в папку развертывания: '${deployment_folder}'"
  pushd "${deployment_folder}"
  log "Начинаем сборку и запуск контейнеров. Проверяем есть ли образ '${IMAGE_NAME}'"
  if [[ -z $(docker image inspect ${IMAGE_NAME}) ]]; then
    log "Образа '${IMAGE_NAME}' не существует"
    rebuild_and_run_containers
  else
    log "Образ '${IMAGE_NAME}' существует. Проверяем, является ли он свежим"
    if is_image_up_to_date; then
      log "Образ свежий. Запускаем контейнер"
      run_container --no-deps
    else
      log "Образ устарел. Пересобираем и запускаем контейнер"
      rebuild_and_run_containers
    fi
  fi
  log "Возращаемся в предыдущую папку из стека"
  popd
  log "Закончили сборку и запуск контейнеров"
}

build_jar_file
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