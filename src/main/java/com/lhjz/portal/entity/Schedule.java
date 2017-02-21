/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.ScheduleKnowStatus;
import com.lhjz.portal.pojo.Enum.SchedulePriority;
import com.lhjz.portal.pojo.Enum.ScheduleType;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Schedule implements Serializable {

	private static final long serialVersionUID = -2295930155409621488L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String title;

	@Column(length = 2000)
	private String description;

	@Column
	private String place;

	@Column
	private Boolean privated = Boolean.FALSE;

	@ManyToOne
	@JoinColumn(name = "creator")
	@CreatedBy
	private User creator;

	@ManyToOne
	@JoinColumn(name = "updater")
	@LastModifiedBy
	private User updater;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	private Date updateDate;

	@Enumerated(EnumType.STRING)
	@Column
	private ScheduleType type = ScheduleType.Task;

	@Enumerated(EnumType.STRING)
	@Column
	private SchedulePriority priority = SchedulePriority.Middle;

	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date endDate;

	@Column
	private Long remind; // 单位:分钟

	@Enumerated(EnumType.STRING)
	@Column
	private ScheduleKnowStatus knowStatus = ScheduleKnowStatus.No;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Version
	private long version;

	@ManyToMany(mappedBy = "actSchedules")
	Set<User> actors = new HashSet<User>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public User getUpdater() {
		return updater;
	}

	public void setUpdater(User updater) {
		this.updater = updater;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

	public SchedulePriority getPriority() {
		return priority;
	}

	public void setPriority(SchedulePriority priority) {
		this.priority = priority;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
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

	public ScheduleKnowStatus getKnowStatus() {
		return knowStatus;
	}

	public void setKnowStatus(ScheduleKnowStatus knowStatus) {
		this.knowStatus = knowStatus;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Set<User> getActors() {
		return actors;
	}

	public void setActors(Set<User> actors) {
		this.actors = actors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schedule other = (Schedule) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Schedule [id=" + id + ", title=" + title + ", description=" + description + ", place=" + place
				+ ", privated=" + privated + ", creator=" + creator + ", updater=" + updater + ", createDate="
				+ createDate + ", updateDate=" + updateDate + ", type=" + type + ", priority=" + priority + ", channel="
				+ channel + ", startDate=" + startDate + ", endDate=" + endDate + ", remind=" + remind + ", knowStatus="
				+ knowStatus + ", status=" + status + ", version=" + version + "]";
	}

}
