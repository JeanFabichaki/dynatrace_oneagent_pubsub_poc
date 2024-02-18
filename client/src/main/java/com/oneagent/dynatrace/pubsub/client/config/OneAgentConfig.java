package com.oneagent.dynatrace.pubsub.client.config;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Log4j2
public class OneAgentConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public OneAgentSDK oneAgentSdk() {
        var agent = OneAgentSDKFactory.createInstance();
        agent.setLoggingCallback(new StdErrLoggingCallback());
        log.info("Bean OneAgentSDK created!");
        return agent;
    }
}
