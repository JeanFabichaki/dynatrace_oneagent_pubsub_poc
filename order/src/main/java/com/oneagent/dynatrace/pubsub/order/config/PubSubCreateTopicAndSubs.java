package com.oneagent.dynatrace.pubsub.order.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@Configuration
@Slf4j
public class PubSubCreateTopicAndSubs {


    private static final String projectId = "dynatrace-pubsub-poc";
    private static final String ORDER_NOTIFICATION_SUBSCRIPTION = "order-notification";
    private static final String TOPIC = "order";

    @EventListener(ApplicationReadyEvent.class)
    public void createTopicsAndSubs() throws IOException {
        log.info("Start Create Topics and Subscritions");

        var hostport = System.getenv("PUBSUB_EMULATOR_HOST");
        var channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();

        try {
            var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

            createTopic(channelProvider);
            createSubscription(channelProvider);
        } finally {
            channel.shutdown();
        }
    }

    private static void createSubscription(FixedTransportChannelProvider channelProvider) throws IOException {
        var subSettings = SubscriptionAdminSettings.newBuilder()
                .setCredentialsProvider(new NoCredentialsProvider())
                .setTransportChannelProvider(channelProvider)
                .build();

        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subSettings)) {
            var subscriptionOne = Subscription.newBuilder()
                    .setName(String.valueOf(ProjectSubscriptionName.of(projectId, ORDER_NOTIFICATION_SUBSCRIPTION)))
                    .setTopic(String.valueOf(TopicName.of(projectId, TOPIC)))
                    .setAckDeadlineSeconds(60)
                    .build();

            try {
                var s = subscriptionAdminClient.createSubscription(subscriptionOne);
                log.info("Sub created {}", s.getName());
            } catch (AlreadyExistsException ignore) {
               log.info("Using existing subscriptions");

            }
        }
    }

    private static void createTopic(FixedTransportChannelProvider channelProvider) throws IOException {
        var topicSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(new NoCredentialsProvider()).build();

        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicSettings)) {
            try {
                Topic response = topicAdminClient.createTopic(TopicName.ofProjectTopicName(projectId, TOPIC));
                log.info("Created Topic = {}", response.getKmsKeyName());
            } catch (AlreadyExistsException ignore) {
                log.info("Using existing topics.");
            } catch (Exception ex) {
                log.error("Error create topic", ex);
            }
        }
    }

    @Bean
    CredentialsProvider googleCredentials() {
        return NoCredentialsProvider.create();
    }

}

