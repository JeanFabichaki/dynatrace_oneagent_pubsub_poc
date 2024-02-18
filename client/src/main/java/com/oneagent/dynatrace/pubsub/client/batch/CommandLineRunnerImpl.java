package com.oneagent.dynatrace.pubsub.client.batch;

import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {

    public static final String ORDER_APP = "http://order-app:8080";
    @Autowired
    public OneAgentSDK oneAgentSDK;

    @Override
    public void run(String... args) throws Exception {

        Thread.sleep(5000L);

        while(true) {

            String serviceMethod = "sendOrders";
            String serviceName = "ClientService";

            var tracer = oneAgentSDK.traceCustomService(serviceMethod, serviceName);

            tracer.start();

            var rand = new Random();

            var order = new Order(UUID.randomUUID().toString(), "0604862198",
                    rand.nextInt(1000, 9999),
                    rand.nextInt(),List.of("IPhone", "Ipad", "Macbook")
                    .get(new Random().nextInt(3)));

            oneAgentSDK.addCustomRequestAttribute("order.id", order.id());

            var restClient = RestClient.builder()
                    .baseUrl(ORDER_APP)
                    .build();

            restClient.post()
                    .uri("/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MappingJacksonValue(order))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Order Id {} sent", order.id());

            tracer.end();

            Thread.sleep(1000L);
        }

    }
}