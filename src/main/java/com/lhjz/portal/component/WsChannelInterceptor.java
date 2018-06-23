package com.lhjz.portal.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WsChannelInterceptor extends ChannelInterceptorAdapter {

	@Autowired
	CacheManager cacheManager;

	@Override
	public void postSend(org.springframework.messaging.Message<?> message, MessageChannel channel, boolean sent) {

		StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

		// ignore non-STOMP messages like heartbeat messages  
		if (sha.getCommand() == null) {
			return;
		}

		//判断客户端的连接状态  
		log.debug("cmd: " + sha.getCommand().name());

		switch (sha.getCommand()) {
		case CONNECT:
			break;
		case CONNECTED:
			break;
		case DISCONNECT:
			break;
		default:
			break;
		}
	}

}