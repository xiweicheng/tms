package com.lhjz.portal.component.core.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.component.core.model.ChatMsgItem;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.pojo.Enum.Action;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatMsgImpl implements IChatMsg {

	private final ConcurrentHashMap<Long, CopyOnWriteArrayList<ChatMsgItem>> map = new ConcurrentHashMap<>();

	private final static long EXPIRE = 5 * 60 * 1000L; // 过期时间间隔 5min

	@Override
	public void put(Long cid, ChatMsgItem chatMsgItem) {

		if (!map.containsKey(cid)) {
			log.debug("put map key {}", cid);
			map.putIfAbsent(cid, new CopyOnWriteArrayList<>());
		}

		CopyOnWriteArrayList<ChatMsgItem> msgs = map.get(cid);

		if (!msgs.contains(chatMsgItem)) {
			log.debug("add map key {}; list item {}", cid, chatMsgItem);
			msgs.addIfAbsent(chatMsgItem);
		} else {
			for (ChatMsgItem msg : msgs) {
				if (msg.equals(chatMsgItem)) {
					log.debug("update list item {} to {}", msg, chatMsgItem);
					msg.setAction(chatMsgItem.getAction());
					msg.setVersion(chatMsgItem.getVersion());
					msg.setExpire(chatMsgItem.getExpire());
					break;
				}
			}
		}

	}

	@Override
	public void put(ChatChannel chatChannel, Action action) {
		if (chatChannel == null) {
			return;
		}
		put(chatChannel.getChannel().getId(), ChatMsgItem.builder().id(chatChannel.getId()).action(action)
				.version(chatChannel.getVersion()).expire(LocalTime.now().plusNanos(EXPIRE)).build());
	}

	@Override
	public List<ChatMsgItem> get(Long cid) {

		return map.get(cid);
	}

	@Scheduled(fixedRate = 6000)
	public void chatMsgScheduledTask() {

		log.debug("scheduled task: {}, rate: {}", "chatmsg", 6000);

		map.forEachValue(1, list -> {
			list.removeIf(msg -> {
				boolean expired = msg.getExpire().isBefore(LocalTime.now());
				log.debug("expired status {}, chatmsg {}", expired, msg);
				return expired;
			});
		});
	}

}
