package com.oneagent.dynatrace.pubsub.client.batch;

public record Order(String id, String clientDocument, Integer value, Integer quantity, String product){

}