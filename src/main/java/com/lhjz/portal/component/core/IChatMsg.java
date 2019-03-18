package com.lhjz.portal.component.core;

import java.util.List;

import com.lhjz.portal.component.core.model.ChatMsgItem;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;

public interface IChatMsg {

	void put(Long cid, ChatMsgItem chatMsgItem);

	void put(ChatChannel chatChannel, Action action, ChatMsgType type, String username, String atUsernames,
			ChatReply chatReply);

	List<ChatMsgItem> get(Long cid);
}
