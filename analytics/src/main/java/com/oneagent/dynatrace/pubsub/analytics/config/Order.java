package com.oneagent.dynatrace.pubsub.analytics.config;

public record Order(String id, String clientDocument, Integer value, Integer quantity, String product){

}