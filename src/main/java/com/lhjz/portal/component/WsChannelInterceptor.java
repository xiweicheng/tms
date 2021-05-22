package com.lhjz.portal.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.model.OnlinePayload;
import com.lhjz.portal.model.OnlinePayload.Cmd;
import com.lhjz.portal.model.WsLockPayload;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.BlogLockService;
import com.lhjz.portal.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WsChannelInterceptor extends ChannelInterceptorAdapter {

	@Autowired
	CacheManager cacheManager;

	@Lazy
	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Autowired
	BlogLockService blogLockService;

	@Autowired
	UserRepository userRepository;

	@Override
	public void postSend(org.springframework.messaging.Message<?> message, MessageChannel channel, boolean sent) {

		Map<?, ?> attrsMap = (Map<?, ?>) message.getHeaders().get("simpSessionAttributes");

		String path = String.valueOf(attrsMap.get("path"));
		String query = String.valueOf(attrsMap.get("query"));

		boolean isWsLock = path.startsWith("/ws-lock");
		String blogId = isWsLock && StringUtil.isNotEmpty(query) ? query.split("=")[1] : null;

		log.info("post send isWsLock: {}, blogId: {}", isWsLock, blogId);

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
			if (isWsLock) {
				cacheManager.getCache(SysConstant.LOCK_BLOGS).put(username + "@" + blogId + "@" + sha.getSessionId(),
						new Date());
				Boolean lockBy = blogLockService.lockBy(username, Long.valueOf(blogId));

				log.info("ws lockBy: {}", lockBy);

				wsLockSend(WsLockPayload.builder().cmd(com.lhjz.portal.model.WsLockPayload.Cmd.LOCK).locker(username)
						.blogId(Long.valueOf(blogId)).sessionId(sha.getSessionId())
						.name(userRepository.findOne(username).getName()).build());
			} else {
				wsSend(Cmd.ON, username, sha.getSessionId());
				cacheManager.getCache(SysConstant.ONLINE_USERS).put(username + "@" + sha.getSessionId(), new Date());
			}
			break;
		case CONNECTED:
			break;
		case SUBSCRIBE:
			break;
		case DISCONNECT:
			if (isWsLock) {

				ValueWrapper vw = cacheManager.getCache(SysConstant.LOCK_BLOGS)
						.get(username + "@" + blogId + "@" + sha.getSessionId());

				if (vw != null && !StringUtil.isEmpty(vw.get())) {

					cacheManager.getCache(SysConstant.LOCK_BLOGS)
							.evict(username + "@" + blogId + "@" + sha.getSessionId());

					if (!wsLockOnline(username, blogId)) {
						wsLockSend(WsLockPayload.builder().cmd(com.lhjz.portal.model.WsLockPayload.Cmd.UNLOCK)
								.locker(username).blogId(Long.valueOf(blogId)).sessionId(sha.getSessionId()).build());
						Boolean unlockBy = blogLockService.unlockBy(username, Long.valueOf(blogId));

						log.info("ws unlockBy: {}", unlockBy);
					}

				}

			} else {

				ValueWrapper vw = cacheManager.getCache(SysConstant.ONLINE_USERS)
						.get(username + "@" + sha.getSessionId());

				if (vw != null && !StringUtil.isEmpty(vw.get())) {

					cacheManager.getCache(SysConstant.ONLINE_USERS).evict(username + "@" + sha.getSessionId());

					if (!online(username)) {
						wsSend(Cmd.OFF, username, sha.getSessionId());
					}

				}
			}
			break;
		default:
			break;
		}
	}

	private void wsLockSend(WsLockPayload payload) {
		try {
			messagingTemplate.convertAndSend("/blog/lock", payload);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private boolean wsLockOnline(String username, String blogId) {

		final List<Object> res = new ArrayList<>();

		try {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<Object, Object> cache = (ConcurrentHashMap<Object, Object>) cacheManager
					.getCache(SysConstant.LOCK_BLOGS).getNativeCache();

			cache.forEachKey(1, key -> {
				String un = String.valueOf(key).split("@")[0];
				String bid = String.valueOf(key).split("@")[1];
				if (un.equals(username) && bid.equals(blogId)) {
					res.add(cache.get(key));
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return res.size() > 0;
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

		return res.size() > 0;
	}

}