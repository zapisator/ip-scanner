Добрый день.

Я хочу представить вам отчет о проделанной работе над тестовым заданием по созданию веб-приложения для сканирования
IP-адресов. В этом отчете я расскажу о целях и требованиях задания, о технологиях и инструментах, которые я использовал
для его реализации, о функциональности и особенностях моего решения, а также о проблемах и перспективах его развития.

# Цели и требования

Тестовое задание состояло в том, чтобы разработать веб-приложение, которое позволяет пользователю ввести диапазон
IP-адресов и получить информацию о том, какие из них доступны для подключения, а также о доменных именах, которые
присутствуют в SSL сертификатах данных IP-адресов, если они есть. Приложение должно иметь простой и удобный интерфейс.
Приложение должно быть написано на языке Java 8 с использованием фреймворка
Javalin и библиотеки Apache Http Client. Приложение не должно использовать Spring Framework, React, Angular, jQuery и
другие запрещенные технологии.

# Запуск

```Sh
./mvnw package
```
Команда   из корня проекта создаст запускаемый IpScanner-1.0.0.jar

```Sh
java -jar ./target/IpScanner-1.0.0.jar
```
Запустит программу. Интерфейс доступен
```Sh
http://localhost:7000/
```

# Технологии и инструменты

Для реализации тестового задания я использовал следующие технологии и инструменты:

__Java 8__ - основной язык программирования, на котором написано приложение. Я выбрал эту версию Java, так как она
соответствует требованиям задания и имеет широкую поддержку и совместимость с различными библиотеками и фреймворками.

__Javalin__ - легковесный фреймворк для создания веб-приложений на Java и Kotlin. Я выбрал этот фреймворк, так как он
позволяет быстро и просто создавать RESTful API и веб-интерфейс с минимальным количеством кода и конфигурации.

__Apache Http Client__ - библиотека для работы с HTTP-протоколом на Java. Я использовал эту библиотеку для реализации
сканирования IP-адресов с помощью отправки HTTP-запросов и получения HTTP-ответов. Я также использовал эту библиотеку
для получения SSL сертификатов и выполнения в них поиска доменных имен.

__Maven__ - инструмент для управления проектами и сборки приложений на Java. Я использовал Maven для описания структуры
и зависимостей проекта, а также для автоматизации процесса сборки, тестирования и запуска приложения. Я также
использовал Maven Wrapper, чтобы обеспечить запуск приложения с наименьшими зависимостями от внешних обстоятельств.

__Selenium__ - фреймворк для автоматизации тестирования веб-приложений. Я использовал Selenium для написания
интеграционных тестов, которые проверяют работоспособность и корректность веб-интерфейса приложения. Я начал работу над
тестированием в разных браузерах и операционных системах с помощью Selenium Grid, но не успел ее завершить из-за
трудностей с зависимостями и версией Java.

__Docker__ - платформа для создания, запуска и управления контейнерами с приложениями. Я использовал Docker для создания
изолированного и воспроизводимого окружения для запуска приложения и тестов. Я также использовал Docker Compose, чтобы
описать и координировать работу нескольких контейнеров, таких как приложение и тесты.

__GitHub__ - платформа для хранения, управления и совместной работы над исходным кодом проекта. Я использовал GitHub для
размещения репозитория с моим проектом, а также для демонстрации его работодателю. Я также использовал GitHub Actions,
чтобы автоматизировать процесс сборки, тестирования и публикации приложения при каждом изменении кода.

# Функциональность и особенности

Мое решение представляет собой веб-приложение, которое состоит из двух основных компонентов: серверной части, написанной
на Java с использованием Javalin и Apache Http Client, и клиентской части, написанной на HTML и CSS. Серверная часть
отвечает за обработку HTTP-запросов от клиента, выполнение сканирования IP-адресов, получение SSL сертификатов и поиска
доменных имен в них и отправку результатов обратно клиенту. Клиентская часть отвечает за отображение веб-интерфейса
приложения, ввод диапазона IP-адресов, отправку запроса на сканирование серверу и получение и визуализацию результатов.

Веб-интерфейс приложения состоит из трех основных элементов: поля для ввода диапазона IP-адресов, кнопки для запуска
сканирования и таблицы для отображения результатов. Пользователь может ввести диапазон IP-адресов с маской, например,
51.38.24.0/24, и нажать кнопку “Scan”. Приложение проверит корректность введенного диапазона и отправит запрос на
сервер. Сервер разделит диапазон на равные части и распределит их между потоками, которые будут сканировать IP-адреса.
Для каждого IP-адреса сервер попытается установить HTTPS-соединение и получить SSL сертификат, если он есть. Затем
сервер выполнит поиск доменных имен в теле сертификата и сохранит их в текстовый файл. После завершения сканирования
сервер отправит результаты обратно клиенту, который отобразит их в таблице. Таблица содержит следующие колонки:
IP-адрес, статус (доступен или нет), доменные имена (если есть) и ссылка на текстовый файл с результатами.

# Проблемы и перспективы

В ходе выполнения тестового задания я в ходе выполнения тестового задания я столкнулся с некоторыми проблемами и
трудностями, которые я хочу описать в этом разделе. Я также хочу указать на возможные направления для дальнейшего
развития и улучшения моего решения.

Одной из проблем, с которой я столкнулся, была работа с SSL сертификатами. Я не нашел готового решения для получения SSL
сертификата по IP-адресу с помощью Apache Http Client, поэтому я пришел к собственному способу, который заключается в
создании кастомного SSL контекста, который игнорирует ошибки валидации сертификата, и использовании его для установки
HTTPS-соединения с IP-адресом. Затем я извлекаю сертификат из этого соединения и выполняю в нем поиск доменных имен.
Этот способ работает, но он не очень эффективен и безопасен, так как он не проверяет подлинность и целостность
сертификата. Возможно, есть более оптимальный и надежный способ для решения этой задачи, но я не успел его найти и
реализовать.

Другой проблемой было тестирование в разных браузерах и операционных системах с помощью Selenium Grid. Я начал работу
над этой функцией, но не успел ее завершить из-за трудностей с зависимостями и версией Java. Я столкнулся с ошибкой,
которая не была достаточно описана в интернете. Возможно проблема в том, что моя версия Java не совместима с версией
Selenium, которую я использовал. Я также попытался использовать другие браузеры, но они тоже
не работали. Я не знаю, в чем именно причина этой ошибки, но я думаю, что это связано с конфигурацией Docker или Maven.
Я хотел бы разобраться с этой проблемой, но за это время я не успел.

Кроме этих проблем, я также столкнулся с некоторыми трудностями при изучении новых технологий и фреймворков, таких как
Javalin, Apache Http Client, Selenium и отдельные детали уже известных мне технологий. Я не имел опыта работы с ними до
этого задания, поэтому мне пришлось
читать документацию, смотреть примеры и решать ошибки. Это было интересно и полезно, но также занимало много времени и
усилий. Я думаю, что я мог бы сделать проект лучше и быстрее, если бы я знал эти технологии заранее.

Несмотря на эти проблемы и трудности, я также вижу перспективы для дальнейшего развития и улучшения моего решения. Вот
некоторые из них:

- **Улучшить алгоритм сканирования** - я мог бы оптимизировать алгоритм сканирования IP-адресов, используя более
  эффективные и безопасные способы для получения и обработки SSL сертификатов, а также для распределения нагрузки между
  потоками. Я мог бы также добавить возможность остановки, возобновления и отмены сканирования, а также сохранения и
  загрузки результатов.
- **Улучшить веб-интерфейс** - я мог бы улучшить веб-интерфейс приложения, сделав его более красивым и функциональным. Я
  мог бы использовать Bootstrap или другие библиотеки для стилизации элементов, а также добавить валидацию, подсказки и
  сообщения об ошибках для поля ввода. Я мог бы также добавить асинхронное обновление результатов с помощью веб-сокетов,
  а также географическую информацию и визуализацию с помощью внешнего API и библиотеки Leaflet.
- **Улучшить тестирование** - я мог бы улучшить тестирование приложения, добавив больше тестовых случаев, покрывающих
  различные сценарии и граничные условия. Я мог бы также завершить работу над тестированием в разных браузерах и ОС с
  помощью Selenium Grid, а также добавить другие виды тестирования, такие как юнит-тестирование, нагрузочное
  тестирование и безопасностное тестирование.
- **Сделать более легким в установке и запуске**

# Заключение

В заключение, я хочу сказать, что я сделал свой проект с интересом и удовольствием, так как это было для меня новым и
сложным опытом. Я изучил много новых технологий и фреймворков, которые я считаю полезными и важными для разработки
веб-приложений на Java. Я также показал свои навыки и креативность, добавив некоторые дополнительные функции, которые
выходят за рамки требований, но не нарушают запреты. Я признаю, что мой проект не идеален и имеет некоторые проблемы и
недостатки, которые я хочу исправить и улучшить. Я надеюсь, что вы оцените мою работу и дадите мне обратную связь и
рекомендации. Спасибо за ваше внимание и время.