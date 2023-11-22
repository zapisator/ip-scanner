
function log () {
  local -r timestamp=$(date +"%Y-%m-%dT%H:%M:%S.%3N")
  local -r script_name=$(basename "$0")
  local -r caller_function_name="${FUNCNAME[1]}"
  local -r message="$1"
  printf "%s: [%s # %s] %s\n" "${timestamp}" "${script_name}" "${caller_function_name}" "${message}" >&2
}