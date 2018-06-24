package com.lhjz.portal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.lhjz.portal.component.WsChannelInterceptor;
import com.lhjz.portal.constant.SysConstant;

@Configuration
@EnableWebSocketMessageBroker
public class WsConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Autowired
	WsChannelInterceptor channelInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {

		config.enableSimpleBroker("/channel", "/direct");
		config.setApplicationDestinationPrefixes("/chat");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();

	}

	/** 
	 * 消息传输参数配置 
	 */
	//	@Override
	//	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
	//		registry.setMessageSizeLimit(8192) //设置消息字节数大小  
	//				.setSendBufferSizeLimit(8192)//设置消息缓存大小  
	//				.setSendTimeLimit(10000); //设置消息发送时间限制毫秒  
	//	}

	/** 
	 * 输入通道参数设置 
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		//		registration.taskExecutor().corePoolSize(4) //设置消息输入通道的线程池线程数  
		//				.maxPoolSize(8)//最大线程数  
		//				.keepAliveSeconds(60);//线程活动时间  
		registration.interceptors(channelInterceptor);
	}

	/** 
	 * 输出通道参数设置 
	 */
	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		//		registration.taskExecutor().corePoolSize(4).maxPoolSize(8);
		registration.interceptors(channelInterceptor);
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(SysConstant.ONLINE_USERS);
	}

}