package com.oneagent.dynatrace.pubsub.client.config;

import com.dynatrace.oneagent.sdk.api.LoggingCallback;

public class StdErrLoggingCallback implements LoggingCallback {

    @Override
    public void error(String message) {
        System.err.println("[OneAgent SDK ERROR]: " + message);
    }

    @Override
    public void warn(String message) {
        System.err.println("[OneAgent SDK WARNING]: " + message);
    }
}
