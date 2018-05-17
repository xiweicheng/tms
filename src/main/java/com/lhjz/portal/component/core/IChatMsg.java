package com.lhjz.portal.component.core;

import java.util.List;

import com.lhjz.portal.component.core.model.ChatMsgItem;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.pojo.Enum.Action;

public interface IChatMsg {

	void put(Long cid, ChatMsgItem chatMsgItem);
	
	void put(ChatChannel chatChannel, Action action);
	
	List<ChatMsgItem> get(Long cid);
}
