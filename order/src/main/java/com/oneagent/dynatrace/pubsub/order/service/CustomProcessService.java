package com.oneagent.dynatrace.pubsub.order.service;

import com.dynatrace.oneagent.sdk.api.CustomServiceTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.oneagent.dynatrace.pubsub.order.web.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomProcessService {

    @Autowired
    public OneAgentSDK oneAgentSDK;

    public void processOrder(Order order) throws InterruptedException {

        String serviceMethod = "processOrder";
        String serviceName = "CalculateOrderValue";

        var tracer = oneAgentSDK.traceCustomService(serviceMethod, serviceName);

        tracer.start();

        oneAgentSDK.addCustomRequestAttribute("order.value", order.value());
        oneAgentSDK.addCustomRequestAttribute("order.product", order.product());
        oneAgentSDK.addCustomRequestAttribute("order.clientDocument", order.clientDocument());

        Thread.sleep(1000);

        tracer.end();
    }
}
