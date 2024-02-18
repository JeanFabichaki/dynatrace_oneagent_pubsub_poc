package com.oneagent.dynatrace.pubsub.order.service;

import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.OutgoingMessageTracer;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.enums.MessageDestinationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.oneagent.dynatrace.pubsub.order.web.Order;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AnalyticsPublisherService {

    private static final String TOPIC = "order";

    @Autowired
    public OneAgentSDK oneAgentSdk;

    public void publisher(String projectId, String topicId, Order order) throws InterruptedException {

        Publisher publisher = null;

        var outgoingMessageTracer = getOutgoingMessageTracer();

        try {
            oneAgentSdk.addCustomRequestAttribute("order.id", order.id());
            outgoingMessageTracer.start();

            var channelProvider = getTransportChannelProvider();
            var topicName = TopicName.ofProjectTopicName(projectId, topicId);

            publisher = Publisher.newBuilder(topicName).setChannelProvider(channelProvider)
                    .setCredentialsProvider(new NoCredentialsProvider())
                    .build();

            ByteString data = ByteString.copyFromUtf8(new ObjectMapper().writeValueAsString(order));
            var pubsubMessage = PubsubMessage.newBuilder().setData(data)
                    .putAllAttributes(ImmutableMap.of(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME, outgoingMessageTracer.getDynatraceStringTag()))
                    .build();

            log.info("Message : {}", pubsubMessage.toByteString().toStringUtf8());
            log.info("Contains Dyna Attribute : {}", pubsubMessage.containsAttributes(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));

            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            outgoingMessageTracer.setVendorMessageId(messageId); // id do pubsub
            log.info("Published message ID: {}", messageId);

        }catch (Exception ex) {
            log.error("Error publisher", ex);
            outgoingMessageTracer.error(ex);
        } finally {
            outgoingMessageTracer.end();
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }

    private OutgoingMessageTracer getOutgoingMessageTracer() {
        var messagingSystemInfo = oneAgentSdk.createMessagingSystemInfo("Google Pubsub", TOPIC, MessageDestinationType.TOPIC, ChannelType.IN_PROCESS, null);
        var outgoingMessageTracer = oneAgentSdk.traceOutgoingMessage(messagingSystemInfo);
        outgoingMessageTracer.setCorrelationId(oneAgentSdk.getTraceContextInfo().getTraceId());
        return outgoingMessageTracer;
    }

    private static TransportChannelProvider getTransportChannelProvider() {

        String hostport = System.getenv("PUBSUB_EMULATOR_HOST");

        var channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    }

}