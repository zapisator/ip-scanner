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
  printf "%s: [%s # %s] %s\n" "${timestamp}" "${script_name}" "${caller_function_name}" "${message}" >&2
}

function get_script_abs_path () {
   log "Получаем относительный путь к скрипту"
   local script_path="$(dirname -- "${SCRIPT_NAME}")"
   log "Относительный путь к скрипту: ${script_path} преобразовываем в абсолютный путь"
   script_path="$(cd -- "${script_path}" && pwd)"
   log "Абсолютный путь к скрипту: ${script_path}. Проверяем, что путь доступен"
   if [[ -z "${script_path}" ]] ; then
     log "Ошибка: не удалось получить путь к скрипту"
     return 1
   fi
   log "Путь доступен. Возращаем значение пути: ${script_path}"
   printf '%s\n' "${script_path}"
 }

function get_jar_file () {
  log "Получаем от docker compose значение переменной окружения JAR_FILE"
  local jar_file=$(docker compose --env-file "${ENV_FILE}" config | grep -m 1 JAR_FILE | awk -F ': ' '{print $2}')
  log "Значение JAR_FILE: ${jar_file}. Проверяем, что значение не пустое"
  if [[ -z "${jar_file}" ]]; then
    log "Не удалось найти переменную JAR_FILE в файле docker-compose.yml"
    return 1
  fi
  log "JAR_FILE существует. Возращаем его значение: ${jar_file}"
  printf '%s\n' "${jar_file}"
}

DOCKER_COMPOSE_NAME="docker-compose.yaml"
IMAGE_NAME="deployment-ip-scanner"
SCRIPT_PATH="$(get_script_abs_path)"
PROJECT_FOLDER="$(dirname "${SCRIPT_PATH}")"
DEPLOYMENT_FOLDER="${PROJECT_FOLDER}/deployment"
DOCKER_COMPOSE_FILE="${DEPLOYMENT_FOLDER}/${DOCKER_COMPOSE_NAME}"
ENV_FILE="${DEPLOYMENT_FOLDER}/.env"
TARGET_FOLDER="${PROJECT_FOLDER}/target"
JAR_FILE="${TARGET_FOLDER}/$(get_jar_file)"
SRC_FOLDER="${PROJECT_FOLDER}/src"
IMAGE_FILE="${DEPLOYMENT_FOLDER}/${IMAGE_NAME}"

log "SCRIPT_NAME=${SCRIPT_NAME}"
log "DOCKER_COMPOSE_NAME=${DOCKER_COMPOSE_NAME}"
log "IMAGE_NAME=${IMAGE_NAME}"
log "SCRIPT_PATH=${SCRIPT_PATH}"
log "PROJECT_FOLDER=${PROJECT_FOLDER}"
log "DEPLOYMENT_FOLDER=${DEPLOYMENT_FOLDER}"
log "DOCKER_COMPOSE_FILE=${DOCKER_COMPOSE_FILE}"
log "ENV_FILE=${ENV_FILE}"
log "TARGET_FOLDER=${TARGET_FOLDER}"
log "JAR_FILE=${JAR_FILE}"
log "SRC_FOLDER=${SRC_FOLDER}"
log "IMAGE_FILE=${IMAGE_FILE}"

cd ${PROJECT_FOLDER}

function build_jar_file () {
  log "Начинаем сборку jar-файла '${JAR_FILE}'. Проверяем есть ли этот файл"
  if [[ -f ${JAR_FILE} ]]; then
    log "jar-файл существует, проверяем были ли его изменения"
    rebuild_jar_file_if_needed
  else
    log "jar-файла не существует. Нужно построить новый."
    build_new_jar_file
  fi
  log "Закончили сборку jar-файла"
}

function build_new_jar_file () {
  log "Файла не было. Собираем jar-файл заново с ./mvnw package --quiet"
  ./mvnw package --quiet || { log "Сборка неуспешна" >&2; exit 1; }
  log "Сборка успешна"
}

function remove_jar_file() {
  log "Удаляем старый jar-файл: ${JAR_FILE}"
  rm "${JAR_FILE}"
}

function rebuild_jar_file_if_needed () {
  log "Проверяем старше ли файлы в папке src '${SRC_FOLDER}', чем файл jar '${JAR_FILE}'"
  if find "${SRC_FOLDER}" -newer "${JAR_FILE}" | grep -q .; then
    log "Он '${JAR_FILE}' существует и были изменения исходного кода. Удаляем старый jar-файл"
    remove_jar_file
    build_new_jar_file
  else
    log "Jar-файл не изменился"
  fi
}

function run_container () {
  local option="$1"
  log "Запускаем контейнер с опцией ${option}"
  docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" up ${option} -d
}

function rebuild_and_run_containers () {
  log "Собираем образ"
  docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" build
  log "Запускаем контейнер"
  run_container --force-recreate --no-deps
}

function is_image_up_to_date () {
  log "Получаем дату образа"
  local image_date="$(date -d "$(docker image inspect ${IMAGE_NAME} | grep LastTagTime | awk -F ' ' '{gsub(/"/,"",$2); print $2}')" +%FT%T)"
  log "Дата образа   : '${image_date}'. Получаем дату jar-файла"
  local jar_date="$(date -d "$(stat -c %y ${JAR_FILE})" +%FT%T)"
  log "Дата jar файла: '${jar_date}'. Сравниваем даты"
  if [[ "${image_date}" < "${jar_date}" ]]; then
    log "Образ '${IMAGE_NAME}' устарел"
    return 1
  else
    log "Образ свежий"
    return 0
  fi
}

function run_container () {
  log "Получаем опцию для запуска контейнера"
  local option="$1"
  log "Запускаем контейнер с опцией ${option}"
  docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" up "${option}" --detach
}

function if_old_rebuild_container () {
  log "Проверяем, является ли образ свежим"
  if is_image_up_to_date; then
    log "Образ свежий. Запускаем контейнер"
    docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" up --detach
  else
    log "Образ устарел. Пересобираем и запускаем контейнер"
    rebuild_and_run_containers
  fi
}

function build_and_run_containers () {
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
  log "Закончили сборку и запуск контейнеров"
}

build_jar_file
build_and_run_containers

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