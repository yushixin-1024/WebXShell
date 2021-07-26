package org.point.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 连接端点
    @Value("${shell.websocket.endpoint}")
    private String endpoint;
    // 允许跨域域名
    @Value("${shell.websocket.allowedOrigins}")
    private String allowedOrigins;
    // 一对一前缀
    @Value("${shell.websocket.user}")
    private String user;
    // 广播前缀
    @Value("${shell.websocket.topic}")
    private String topic;
    // 向服务端发送消息前缀
    @Value("${shell.websocket.app}")
    private String app;

    /**
     * 配置STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint(endpoint)
            .setAllowedOriginPatterns(allowedOrigins)
            .withSockJS();
    }

    /**
     * 配置消息代理选项
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 开启一对一、广播消息代理
        registry.enableSimpleBroker(app, user, topic);
        // 客户端向服务端发送消息时的前缀(客户端发送消息路径)
        registry.setApplicationDestinationPrefixes(app);
        // 给指定用户发送一对一消息的前缀(客户端订阅消息路径)
        registry.setUserDestinationPrefix(user);
    }
}
