package com.soldesk.team_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    // 프레임워크의 messageBrokerTaskScheduler를 주입하지 않습니다. (순환 참조 방지)
    // 우리만의 스케줄러 빈을 별도 이름으로 생성해서 사용합니다.
    @Bean(name = "wsHeartbeatScheduler")
    public ThreadPoolTaskScheduler wsHeartbeatScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(1);
        ts.setThreadNamePrefix("ws-heartbeat-");
        ts.initialize();
        return ts;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        var broker = registry.enableSimpleBroker("/topic");
        // ★ 우리가 만든 스케줄러로 하트비트 연결
        broker.setTaskScheduler(wsHeartbeatScheduler());
        broker.setHeartbeatValue(new long[]{10000, 10000}); // 10초/10초
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
