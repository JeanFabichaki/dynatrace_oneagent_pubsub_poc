package com.oneagent.dynatrace.pubsub.order.config;

import com.dynatrace.oneagent.sdk.api.LoggingCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StdErrLoggingCallback implements LoggingCallback {

    @Override
    public void error(String message) {
        log.error("[OneAgent SDK ERROR]: {}", message);
    }

    @Override
    public void warn(String message) {
        log.error("[OneAgent SDK WARNING]: {}", message);
    }
}
