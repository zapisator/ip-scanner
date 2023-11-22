#!/usr/bin/env bash
source util/utils.sh

# Этот скрипт предназначен для автоматизации сборки и запуска проекта ip-scanner
# ip-scanner - это приложение, которое сканирует переданные ему ip в формате xx.xx.xx.xx/mask,
# получает досупные ssl сертификаты с каждого адреса и выбирает из каждого сертификата домены,
# сохраняет найденное в файл
#
# Для сборки и запуска проекта требуются следующие компоненты:
# - Docker - платформа для создания, запуска и управления контейнерами
# - docker compose - инструмент для определения и запуска многоконтейнерных приложений с помощью Docker
# Скрипт выполняет следующие действия:
# - Выполняет сборку jar-файла, образа и контейнера докера и запуск контейнера

# Установить опции для выхода при ошибке, необъявленной переменной или ошибке в конвейере
set -euo pipefail

DOCKER_COMPOSE_NAME="docker-compose.yaml"
IMAGE_NAME="deployment-ip-scanner"
SCRIPT_PATH="$(get_script_abs_path)"
PROJECT_FOLDER="$(dirname "${SCRIPT_PATH}")"
DEPLOYMENT_FOLDER="${PROJECT_FOLDER}/deployment"
DOCKER_COMPOSE_FILE="${DEPLOYMENT_FOLDER}/${DOCKER_COMPOSE_NAME}"
ENV_FILE="${DEPLOYMENT_FOLDER}/.env"
TARGET_FOLDER="${PROJECT_FOLDER}/target"
JAR_FILE="${TARGET_FOLDER}/$(get_jar_file ${DOCKER_COMPOSE_FILE} ${ENV_FILE})"
SRC_FOLDER="${PROJECT_FOLDER}/src"
IMAGE_FILE="${DEPLOYMENT_FOLDER}/${IMAGE_NAME}"

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

function remove_jar_file() {
  log "Удаляем старый jar-файл: ${JAR_FILE}"
  rm "${JAR_FILE}"
}

function run_container () {
  log "Получаем опцию для запуска контейнера"
  local option="$1"
  log "Запускаем контейнер с опцией ${option}"
  docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" up "${option}" --detach
}

function rebuild_and_run_containers () {
  log "Собираем образ"
  docker compose --file ${DOCKER_COMPOSE_FILE} --env-file "${ENV_FILE}" build
  log "Запускаем контейнер"
  run_container --force-recreate --no-deps
}

function build_new_jar_file () {
  log "Файла не было. Собираем jar-файл заново с ./mvnw package --quiet"
  ./mvnw package --quiet || { log "Сборка неуспешна" >&2; exit 1; }
  log "Сборка успешна"
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

cd ${PROJECT_FOLDER}
build_jar_file
build_and_run_containers
