package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.example.domain.IpScannerServiceImpl;
import org.example.infastructure.web.IpScannerController;

public class Main {

    public static void main(String[] args) {
        final Javalin server = Javalin
                .create(config -> config.addStaticFiles("/frontend", Location.CLASSPATH))
                .start(7000);
        final IpScannerServiceImpl scannerService = new IpScannerServiceImpl();
        final IpScannerController controller = new IpScannerController(scannerService);

        controller.register(server);

    }
}