package com.lhjz.portal.pojo;

import java.util.Date;

import org.hibernate.validator.constraints.NotBlank;

public class ScheduleForm {

	@NotBlank(message = "计划内容不能为空！")
	private String title;
	private String description;
	private String place;
	private Boolean privated = Boolean.FALSE;
	private String type;
	private String priority;
	private Long channelId;
	private Date startDate;
	private Date endDate;
	private Long remind; // 单位:分钟
	private String actors;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Boolean getPrivated() {
		return privated;
	}

	public void setPrivated(Boolean privated) {
		this.privated = privated;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getRemind() {
		return remind;
	}

	public void setRemind(Long remind) {
		this.remind = remind;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	@Override
	public String toString() {
		return "ScheduleForm [title=" + title + ", description=" + description + ", place=" + place + ", privated="
				+ privated + ", type=" + type + ", priority=" + priority + ", channelId=" + channelId + ", startDate="
				+ startDate + ", endDate=" + endDate + ", remind=" + remind + ", actors=" + actors + "]";
	}

}
