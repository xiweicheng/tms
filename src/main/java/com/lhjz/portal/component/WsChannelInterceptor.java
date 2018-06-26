package com.lhjz.portal.component;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.model.OnlinePayload;
import com.lhjz.portal.model.OnlinePayload.Cmd;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WsChannelInterceptor extends ChannelInterceptorAdapter {

	@Autowired
	CacheManager cacheManager;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Override
	public void postSend(org.springframework.messaging.Message<?> message, MessageChannel channel, boolean sent) {

		StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

		Authentication auth = (Authentication) message.getHeaders().get("simpUser");
		if (auth == null || auth.getName() == null) {
			return;
		}
		String username = auth.getName();

		// ignore non-STOMP messages like heartbeat messages  
		if (sha.getCommand() == null) {
			return;
		}

		//判断客户端的连接状态  
		log.debug("cmd: " + sha.getCommand().name() + " username: " + username);

		switch (sha.getCommand()) {
		case CONNECT:
			wsSend(Cmd.ON, username);
			cacheManager.getCache(SysConstant.ONLINE_USERS).put(username, new Date());
			break;
		case CONNECTED:
			break;
		case SUBSCRIBE:
			break;
		case DISCONNECT:
			wsSend(Cmd.OFF, username);
			cacheManager.getCache(SysConstant.ONLINE_USERS).evict(username);
			break;
		default:
			break;
		}
	}

	private void wsSend(Cmd cmd, String username) {
		try {
			messagingTemplate.convertAndSend("/channel/online",
					OnlinePayload.builder().username(username).cmd(cmd).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}