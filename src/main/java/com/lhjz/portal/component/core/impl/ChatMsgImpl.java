package com.lhjz.portal.component.core.impl;

import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.component.core.model.ChatMsgItem;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;
import com.lhjz.portal.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class ChatMsgImpl implements IChatMsg {

    private static final long EXPIRE = 5L; // 过期时间间隔 5min

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<ChatMsgItem>> map = new ConcurrentHashMap<>();

    @Value("${tms.chat.channel.chatmsg.off}")
    Boolean off; // 是否关闭

    @Override
    public void put(Long cid, ChatMsgItem chatMsgItem) {

        if (Boolean.TRUE.equals(off)) {
            return;
        }

        try {
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
                        BeanUtils.copyProperties(chatMsgItem, msg);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public void put(ChatChannel chatChannel, Action action, ChatMsgType type, String username, String atUsernames,
                    ChatReply chatReply) {

        if (Boolean.TRUE.equals(off)) {
            return;
        }

        if (chatChannel == null) {
            return;
        }

        username = username == null ? WebUtil.getUsername() : username;

        Long rid = chatReply != null ? chatReply.getId() : null;

        put(chatChannel.getChannel().getId(),
                ChatMsgItem.builder().uuid(UUID.randomUUID().toString()).id(chatChannel.getId()).rid(rid).action(action)
                        .type(type).username(username).atUsernames(atUsernames).version(chatChannel.getVersion())
                        .openEdit(chatChannel.getOpenEdit()).expire(LocalDateTime.now().plusMinutes(EXPIRE)).build());
    }

    @Override
    public List<ChatMsgItem> get(Long cid) {

        return map.get(cid);
    }

    @Scheduled(fixedRate = 6000)
    public void chatMsgScheduledTask() {

        if (Boolean.TRUE.equals(off)) {
            return;
        }

        try {
            map.forEachValue(1, list -> {
                list.removeIf(msg -> {
                    boolean expired = msg.getExpire().isBefore(LocalDateTime.now());
                    log.debug("expired status {}, chatmsg {}", expired, msg);
                    return expired;
                });
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
