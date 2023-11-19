package org.example.frontend;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class FrontendIntegrationTest {

    private WebDriver driver;
    private static Javalin server;

    @Container
    private static final BrowserWebDriverContainer<?> CHROME_CONTAINER = new BrowserWebDriverContainer<>()
            .withCapabilities(capabilities());
    private static final int LOCAL_SERVER_PORT = 7000;
    private static final String ROOT_URL = format("http://host.testcontainers.internal:%d/", LOCAL_SERVER_PORT);


    private static Capabilities capabilities() {
        return new FirefoxOptions()
//                .addArguments("start-maximized") // open Browser in maximized mode
//                .addArguments("disable-infobars") // disabling infobars
//                .addArguments("--disable-extensions") // disabling extensions
//                .addArguments("--disable-dev-shm-usage") // overcome limited resource problems
//                .addArguments("--no-sandbox") // Bypass OS security model
//                .addArguments("--headless")
                .addArguments("");
    }


    @BeforeAll
    static void setUpServer() throws InterruptedException {
        server = Javalin
                .create(config -> {
                    config.addStaticFiles("/frontend", Location.CLASSPATH);
                })
                .start(LOCAL_SERVER_PORT);
        Thread.sleep(2000);
        org.testcontainers.Testcontainers.exposeHostPorts(LOCAL_SERVER_PORT);
        Thread.sleep(2000);
        CHROME_CONTAINER.start();
        System.out.println("\n\nCHROME Container started\n");
    }

    @AfterAll
    static void tearDownServer() {
        if (server != null) {
            server.stop();
        }
        CHROME_CONTAINER.stop();
    }

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        driver = new RemoteWebDriver(
                CHROME_CONTAINER.getSeleniumAddress(),
                new FirefoxOptions()
        );
    }

    @AfterEach
    void tearDown() {
        driver.close();
    }

    @Test
    void testStartScan() throws InterruptedException {
//        RemoteWebDriver driver = CHROME_CONTAINER.getWebDriver(); // Получаем драйвер из контейнера
//        driver.get(ROOT_URL);

//        // Создаем HTTP-клиент
//        CloseableHttpClient client = HttpClients.createDefault();
//// Создаем HTTP-запрос
//        HttpGet request = new HttpGet("http://localhost:7000/script.js");
//// Отправляем запрос и получаем ответ
//        CloseableHttpResponse response = client.execute(request);
//// Читаем содержимое ответа
//        HttpEntity entity = response.getEntity();
//        String content = EntityUtils.toString(entity);
//// Закрываем соединения
//        EntityUtils.consume(entity);
//        response.close();
//        client.close();
//// Выводим содержимое ответа
//        System.out.println(content);
//        Thread.sleep(1000000);
//
//        driver.get(ROOT_URL); // Открываем страницу входа, используя хостнейм host.testcontainers.internal для указания на хост-машину
//        System.out.println(String.join("\n", Arrays.asList(
//                        "\nHOLA\n",
//                        driver.getCurrentUrl()
//                ))
//        );
//        System.out.println("\n\nстраница целиком:\n" + driver.getPageSource() + "\n\n");



//        driver.get("https://www.testproject.io");
//        assertEquals("TestProject - Community Powered Test Automation", driver.getTitle());

//        // Находим элементы по идентификаторам
//        WebElement rangeInput = driver.findElement(By.id("range"));
//        WebElement threadsInput = driver.findElement(By.id("threads"));
//        WebElement scanButton = driver.findElement(By.id("scan-button"));
//        WebElement scanMessage = driver.findElement(By.id("scan-message"));
//        WebElement scanResult = driver.findElement(By.id("scan-result"));
//
//        // Вводим данные в поля
//        rangeInput.sendKeys("51.38.24.0/24");
//        threadsInput.sendKeys("2");
//
//        // Нажимаем на кнопку сканирования
//        scanButton.click();
//
//        // Создаем объект для ожидания
//        WebDriverWait wait = new WebDriverWait(driver, Duration.of(10, ChronoUnit.SECONDS));
//
//        // Ждем, пока появится сообщение о начале сканирования
//        wait.until(ExpectedConditions.textToBePresentInElement(scanMessage, "Scanning started. Please wait for the results."));
//
//        // Проверяем, что сообщение соответствует ожидаемому
//        assertEquals("Scanning started. Please wait for the results.", scanMessage.getText());
//
//        // Ждем, пока появится результат сканирования
//        wait.until(ExpectedConditions.visibilityOf(scanResult));
//
//        // Проверяем, что результат не пустой
//        assertFalse(scanResult.getText().isEmpty());
    }

}
