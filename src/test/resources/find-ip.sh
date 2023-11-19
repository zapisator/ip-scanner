#!/bin/zsh

# Здесь перечислите ваши известные сайты
websites=(
  "google.com"
  "youtube.com"
  "facebook.com"
  "twitter.com"
  "instagram.com"
  "linkedin.com"
  "amazon.com"
  "wikipedia.org"
  "yahoo.com"
  "netflix.com"
  "reddit.com"
  "pinterest.com"
  "tumblr.com"
  "ebay.com"
  "microsoft.com"
  "apple.com"
  "adobe.com"
  "cnn.com"
  "bbc.com"
  "nytimes.com"
  "bloomberg.com"
  "espn.com"
  "nba.com"
  "weather.com"
  "imdb.com"
  "quora.com"
  "stackoverflow.com"
  "github.com"
  "dropbox.com"
  "salesforce.com"
)

# Создайте ассоциативный массив для хранения данных
declare -A site_ip_map

# Функция для получения IP-адресов сайта
get_site_ip() {
  local site="$1"
  local ip_addresses
  ip_addresses=($(dig +short $site | awk '{print "\"" $0 "\""}' | paste -sd, -))
  site_ip_map[$site]=$ip_addresses
}

# Переберите список сайтов и получите их IP-адреса
for site in $websites; do
  get_site_ip $site
done

# Преобразуйте ассоциативный массив в JSON
json="{"
for site in ${(k)site_ip_map}; do
  json+="\n    \"$site\": [${(pj:,:)site_ip_map[$site]}]"
  if [[ $site != $websites[-1] ]]; then
    json+=","
  fi
done
json=${json%,}
json+="\n}"
# Удалите последний символ (запятую) из JSON


# Укажите имя файла для записи JSON-данных
output_file="websites_ip.json"

# Запишите JSON-данные в файл
echo $json > $output_file

echo "JSON-данные были записаны в $output_file"
