package com.lhjz.portal.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

		log.info("post send message: {} channel: {} sent: {}", message, channel, sent);

		Map<?, ?> map = (Map<?, ?>) message.getHeaders().get(SysConstant.NATIVE_HEADERS);
		List<?> list = (map != null && map.containsKey(SysConstant.LOCK_BLOG_ID))
				? (List<?>) map.get(SysConstant.LOCK_BLOG_ID)
				: null;

		Long blogId = null;
		if (list != null && list.size() > 0) { // 博文编辑锁拦截器自行处理
			blogId = Long.valueOf(list.get(0).toString());
		}

		log.info("post send blogId: {}", blogId);

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
		log.info("post send cmd: {} username: {}", sha.getCommand().name(), username);

		switch (sha.getCommand()) {
		case CONNECT:
			wsSend(Cmd.ON, username, sha.getSessionId());
			cacheManager.getCache(SysConstant.ONLINE_USERS).put(username + "@" + sha.getSessionId(), new Date());
			if (blogId != null) {
				cacheManager.getCache(SysConstant.LOCK_BLOGS).put(username + "@" + blogId + "@" + sha.getSessionId(),
						new Date());
			}
			break;
		case CONNECTED:
			break;
		case SUBSCRIBE:
			break;
		case DISCONNECT:
			if (!online(username)) {
				wsSend(Cmd.OFF, username, sha.getSessionId());
			}
			cacheManager.getCache(SysConstant.ONLINE_USERS).evict(username + "@" + sha.getSessionId());

			if (blogId != null) {
				cacheManager.getCache(SysConstant.LOCK_BLOGS).evict(username + "@" + blogId + "@" + sha.getSessionId());
			}
			break;
		default:
			break;
		}
	}

	private void wsSend(Cmd cmd, String username, String sessionId) {
		try {
			messagingTemplate.convertAndSend("/channel/online",
					OnlinePayload.builder().username(username).sessionId(sessionId).cmd(cmd).build());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 判断用户是否超过一个会话存在
	private boolean online(String username) {

		final List<Object> res = new ArrayList<>();

		try {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<Object, Object> cache = (ConcurrentHashMap<Object, Object>) cacheManager
					.getCache(SysConstant.ONLINE_USERS).getNativeCache();

			cache.forEachKey(1, key -> {
				String un = String.valueOf(key).split("@")[0];
				if (un.equals(username)) {
					res.add(cache.get(key));
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return res.size() > 1;
	}

}