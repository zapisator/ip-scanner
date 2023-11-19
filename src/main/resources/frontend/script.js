function startScan() {
    var range = document.getElementById("range").value; // Получаем значение поля range
    var threads = document.getElementById("threads").value; // Получаем значение поля threads
    document.getElementById("scan-button").disabled = true; // Блокируем кнопку
    console.info("Начинаем сканирование для диапазона %s с %d потоками", range, threads); // Выводим сообщение о начале сканирования в консоль
    var xhr = new XMLHttpRequest(); // Создаем объект XMLHttpRequest
    xhr.open("POST", "/scan/start"); // Открываем соединение с сервером
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded"); // Устанавливаем заголовок с типом данных
    xhr.onload = function() { // Назначаем функцию обратного вызова, которая будет вызвана при получении ответа
        var data = xhr.response; // Получаем данные из ответа
        var status = xhr.status; // Получаем код статуса из ответа
        console.info("Статус ответа: %s", status); // Выводим статус ответа в консоль
        if (status == 200) { // Сравниваем статус ответа с числом 200
            document.getElementById("scan-message").textContent = "Scanning started. Please wait for the results."; // Выводим сообщение о начале сканирования
            getResult(range); // Передаем полное значение range в функцию getResult
            console.info("Сканирование успешно запущено"); // Выводим сообщение об успешном запуске сканирования в консоль
        } else {
            document.getElementById("scan-message").textContent = "Error: " + data; // Выводим сообщение об ошибке
            document.getElementById("scan-button").disabled = false; // Разблокируем кнопку
            console.error("Произошла ошибка при запуске сканирования: %s", data); // Выводим сообщение об ошибке в консоль
        }
    };
    xhr.send("range=" + range + "&threads=" + threads + "&dataType=text"); // Отправляем данные на сервер в формате x-www-form-urlencoded
}

function getResult(range) { // Принимаем полное значение range в качестве параметра
    console.info("Получаем результат для диапазона %s", range); // Выводим сообщение о параметрах запроса в консоль
    var xhr = new XMLHttpRequest(); // Создаем объект XMLHttpRequest
    xhr.open("GET", "/scan/result?range=" + range); // Открываем соединение с сервером и передаем параметр range в URL
    xhr.onload = function() { // Назначаем функцию обратного вызова, которая будет вызвана при получении ответа
        var data = xhr.response; // Получаем данные из ответа
        var status = xhr.status; // Получаем код статуса из ответа
        if (status == 200) { // Сравниваем статус ответа с числом 200
            document.getElementById("scan-result").textContent = data; // Выводим результат сканирования
            document.getElementById("scan-button").disabled = false; // Разблокируем кнопку
            console.info("Результат успешно получен: %s", data); // Выводим сообщение об успешном получении результата в консоль
        } else {
            document.getElementById("scan-message").textContent = "Error: " + data; // Выводим сообщение об ошибке
            console.error("Произошла ошибка при получении результата: %s", data); // Выводим сообщение об ошибке в консоль
        }
    };
    xhr.send(); // Отправляем запрос на сервер без данных
}

document.getElementById("scan-form").addEventListener("submit", function(event) { // Назначаем обработчик события submit для формы
    event.preventDefault(); // Отменяем действие по умолчанию для формы
    startScan(); // Вызываем функцию startScan
});

document.getElementById("new-button").addEventListener("click", function() { // Назначаем обработчик события click для кнопки
    location.reload(); // Перезагружаем страницу
});

