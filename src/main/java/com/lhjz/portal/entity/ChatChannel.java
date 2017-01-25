/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
import com.lhjz.portal.pojo.Enum.ChatType;
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
public class ChatChannel implements Serializable {

	private static final long serialVersionUID = 2823425313949085614L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

	@Column(length = 16777216)
	private String content;
	
	private Boolean openEdit;

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
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private ChatType type = ChatType.Msg;
	
	@Column(length = 16777216)
	private String voteZan;

	@Column(length = 16777216)
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	@Version
	private long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getOpenEdit() {
		return openEdit;
	}

	public void setOpenEdit(Boolean openEdit) {
		this.openEdit = openEdit;
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

	public ChatType getType() {
		return type;
	}

	public void setType(ChatType type) {
		this.type = type;
	}

	public String getVoteZan() {
		return voteZan;
	}

	public void setVoteZan(String voteZan) {
		this.voteZan = voteZan;
	}

	public String getVoteCai() {
		return voteCai;
	}

	public void setVoteCai(String voteCai) {
		this.voteCai = voteCai;
	}

	public Integer getVoteZanCnt() {
		return voteZanCnt;
	}

	public void setVoteZanCnt(Integer voteZanCnt) {
		this.voteZanCnt = voteZanCnt;
	}

	public Integer getVoteCaiCnt() {
		return voteCaiCnt;
	}

	public void setVoteCaiCnt(Integer voteCaiCnt) {
		this.voteCaiCnt = voteCaiCnt;
	}

	@Override
	public String toString() {
		return "ChatChannel [id=" + id + ", channel=" + channel + ", content=" + content + ", openEdit=" + openEdit
				+ ", creator=" + creator + ", updater=" + updater + ", createDate=" + createDate + ", updateDate="
				+ updateDate + ", status=" + status + ", type=" + type + ", voteZan=" + voteZan + ", voteCai=" + voteCai
				+ ", voteZanCnt=" + voteZanCnt + ", voteCaiCnt=" + voteCaiCnt + ", version=" + version + "]";
	}

}
