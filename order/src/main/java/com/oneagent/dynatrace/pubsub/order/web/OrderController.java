package com.oneagent.dynatrace.pubsub.order.web;

import com.oneagent.dynatrace.pubsub.order.service.CustomProcessService;
import com.oneagent.dynatrace.pubsub.order.service.AnalyticsPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class OrderController {

    private static final String TOPIC = "order";

    private static final String projectId = "dynatrace-pubsub-poc";
    @Autowired
    private CustomProcessService customProcessService;

    @Autowired
    private AnalyticsPublisherService analyticsPublisherService;

    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    public String createOrder(@RequestBody Order order) throws IOException, ExecutionException, InterruptedException {
        log.info("order : {}", order);
        customProcessService.processOrder(order);
        analyticsPublisherService.publisher(projectId, TOPIC, order);
        return "Message published successfully";
    }
}
