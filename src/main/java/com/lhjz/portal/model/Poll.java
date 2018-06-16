package com.lhjz.portal.model;

import java.util.List;

import com.lhjz.portal.component.core.model.ChatMsgItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poll {

	private Long channelId;
	private Long lastChatChannelId;
	private Boolean isAt;
	private Long countRecent;
	private Long countAt;
	private Long countMyRecentSchedule;
	private List<ChatMsgItem> chatMsgItems;

}
