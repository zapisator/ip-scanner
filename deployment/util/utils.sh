
function log () {
  local -r timestamp=$(date +"%Y-%m-%dT%H:%M:%S.%3N")
  local -r script_name=$(basename "$0")
  local -r caller_function_name="${FUNCNAME[1]}"
  local -r message="$1"
  printf "%s: [%s # %s] %s\n" "${timestamp}" "${script_name}" "${caller_function_name}" "${message}" >&2
}

function get_script_abs_path () {
   log "Получаем относительный путь к скрипту"
   local script_name=$(basename "$0")
   local script_path="$(dirname -- "${script_name}")"
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
   local docker_compose_file="$1"
   local env_file="$2"
   log "Получаем от docker compose значение переменной окружения JAR_FILE"
   local jar_file=$(docker compose --file ${docker_compose_file} --env-file "${env_file}" config | grep -m 1 JAR_FILE | awk -F ': ' '{print $2}')
   log "Значение JAR_FILE: ${jar_file}. Проверяем, что значение не пустое"
   if [[ -z "${jar_file}" ]]; then
     log "Не удалось найти переменную JAR_FILE в файле docker-compose.yml"
     return 1
   fi
   log "JAR_FILE существует. Возращаем его значение: ${jar_file}"
   printf '%s\n' "${jar_file}"
 }