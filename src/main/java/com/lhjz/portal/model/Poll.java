package com.lhjz.portal.model;

import java.io.Serializable;

public class Poll implements Serializable {

	private static final long serialVersionUID = 5754209321167949289L;
	private Long channelId;
	private Long lastChatChannelId;
	private Boolean isAt;
	private Long countRecent;
	private Long countAt;
	private Long countMyRecentSchedule;

	public Poll() {
		super();
	}

	public Poll(Long channelId, Long lastChatChannelId, Boolean isAt, Long countRecent, Long countAt, Long countMyRecentSchedule) {
		super();
		this.channelId = channelId;
		this.lastChatChannelId = lastChatChannelId;
		this.isAt = isAt;
		this.countRecent = countRecent;
		this.countAt = countAt;
		this.countMyRecentSchedule = countMyRecentSchedule;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Long getLastChatChannelId() {
		return lastChatChannelId;
	}

	public void setLastChatChannelId(Long lastChatChannelId) {
		this.lastChatChannelId = lastChatChannelId;
	}

	public Boolean getIsAt() {
		return isAt;
	}

	public void setIsAt(Boolean isAt) {
		this.isAt = isAt;
	}

	public Long getCountRecent() {
		return countRecent;
	}

	public void setCountRecent(Long countRecent) {
		this.countRecent = countRecent;
	}

	public Long getCountAt() {
		return countAt;
	}

	public void setCountAt(Long countAt) {
		this.countAt = countAt;
	}

	public Long getCountMyRecentSchedule() {
		return countMyRecentSchedule;
	}

	public void setCountMyRecentSchedule(Long countMyRecentSchedule) {
		this.countMyRecentSchedule = countMyRecentSchedule;
	}
	

}
