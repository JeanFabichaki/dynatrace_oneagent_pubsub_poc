package com.oneagent.dynatrace.pubsub.order.web;

public record Order(String id, String clientDocument, Integer value, Integer quantity, String product){

}