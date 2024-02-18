package com.oneagent.dynatrace.pubsub.analytics.config;

import com.dynatrace.oneagent.sdk.api.IncomingMessageProcessTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.enums.MessageDestinationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.oneagent.dynatrace.pubsub.analytics.service.OrderDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.IOException;

@Configuration
@Slf4j
public class AnalyticsListener {

    @Autowired
    public OneAgentSDK oneAgentSdk;

    @Autowired
    public OrderDBService orderDBService;
    private static final String ORDER_NOTIFICATION_SUBSCRIPTION = "order-notification";

    @Bean
    CredentialsProvider googleCredentials() {
        return NoCredentialsProvider.create();
    }

    @Bean
    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public MessageHandler messageReceiver() {
        return message -> {

            var incomingMessageProcessTracer = getIncomingMessageProcessTracer(message);

            var dynatraceStringTag = message.getHeaders().get(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME, String.class);
            log.info("DynatraceStringTag {}", dynatraceStringTag);
            incomingMessageProcessTracer.setDynatraceStringTag(dynatraceStringTag);
            incomingMessageProcessTracer.start();

            System.out.println("Message Arrived! The message is: " + new String((byte[]) message.getPayload()));

            Order order = getOrder(message);
            oneAgentSdk.addCustomRequestAttribute("order.id", order.id());

            var originalMessage = message.getHeaders()
                    .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);

            originalMessage.ack();
            orderDBService.createdOrder(order);
            incomingMessageProcessTracer.end();
        };
    }

    private static Order getOrder(Message<?> message) {
        try {
            return new ObjectMapper().readValue(((byte[]) message.getPayload()), Order.class);
        } catch (IOException e) {
            log.error("Error convert json ", e);
            return null;
        }
    }

    private IncomingMessageProcessTracer getIncomingMessageProcessTracer(Message<?> message) {
        var messagingSystemInfo = oneAgentSdk.createMessagingSystemInfo("Google Pubsub", ORDER_NOTIFICATION_SUBSCRIPTION,
                MessageDestinationType.QUEUE, ChannelType.IN_PROCESS, null);
        var incomingMessageProcessTracer = oneAgentSdk.traceIncomingMessageProcess(messagingSystemInfo);
        incomingMessageProcessTracer.setVendorMessageId(message.getHeaders().getId().toString());
        return incomingMessageProcessTracer;
    }


    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("pubsubInputChannel") MessageChannel inputChannel, PubSubTemplate pubSubTemplate) {

        var adapter = new PubSubInboundChannelAdapter(pubSubTemplate, ORDER_NOTIFICATION_SUBSCRIPTION);
        adapter.setOutputChannel(inputChannel);
        adapter.setAckMode(AckMode.MANUAL);

        return adapter;
    }

}
