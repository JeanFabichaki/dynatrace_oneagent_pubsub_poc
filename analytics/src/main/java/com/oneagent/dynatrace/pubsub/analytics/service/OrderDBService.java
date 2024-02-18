package com.oneagent.dynatrace.pubsub.analytics.service;

import com.oneagent.dynatrace.pubsub.analytics.config.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class OrderDBService {

    @Autowired
    private JdbcClient jdbcClient;

    public void createdOrder(Order order) {
        var created = jdbcClient.sql("INSERT INTO orders (id, client_document, val, quantity, product) values(?,?,?,?,?)")
                .params(List.of(order.id(), order.clientDocument(), order.value(), order.quantity(), order.product()))
                .update();

        log.info("Order save in database, order.id {}", order.id());
    }
}
