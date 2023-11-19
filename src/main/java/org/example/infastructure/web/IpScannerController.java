package org.example.infastructure.web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.IpScannerService;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jetty.http.HttpStatus.Code.INTERNAL_SERVER_ERROR;
import static org.eclipse.jetty.http.HttpStatus.Code.OK;

@RequiredArgsConstructor
@Slf4j
public class IpScannerController {

    private final IpScannerService ipScannerService;

    public void startScan(Context context) {
        final String range = context.formParam("range");
        final String threadParam = context.formParam("threads");
        final int threads = Integer.parseInt(requireNonNull(threadParam));

        log.info("Начинаем сканирование для диапазона {} с {} потоками", range, threads); // переводим логирование о начале сканирования
        try {
            ipScannerService.startScan(range, threads);
            context.status(OK.getCode());
            log.info("Сканирование успешно запущено"); // переводим логирование об успешном запуске сканирования
        } catch (Exception e) {
            log.error("Произошла ошибка при сканировании: {}", e.getMessage()); // логируем исключение
            context.status(INTERNAL_SERVER_ERROR.getCode()).result(e.getMessage());
        }
    }

    public void getResult(Context context) {
        final String range = context.queryParam("range");
        log.info("Получаем результат для range {}", range); // переводим логирование о параметрах запроса
        final String result = ipScannerService.getResult(range);

        context
                .status(OK.getCode())
                .result(result);
        log.info("Результат успешно отправлен"); // переводим логирование об успешной отправке результата
}

    public void register(Javalin app) {
        app.post("/scan/start", this::startScan);
        app.get("/scan/result", this::getResult);
    }
}


