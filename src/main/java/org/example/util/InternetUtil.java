package org.example.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InternetUtil {

    @SneakyThrows
    public static List<String> getAllHostsInSubnet(String subnet) {
        log.info("Getting all hosts in subnet: {}", subnet);
        return Arrays
                .stream(
                        new SubnetUtils(subnet)
                                .getInfo()
                                .getAllAddresses()
                )
                .collect(Collectors.toList());
    }
}