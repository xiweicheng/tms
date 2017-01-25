/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
public class Comment implements Serializable {

	private static final long serialVersionUID = -1213448577430547620L;

	@Id
	@GeneratedValue
	private Long id;

	private String targetId;

	@Column(length = 16777216)
	private String content;

	@Column(length = 16777216)
	private String voteZan;

	@Column(length = 16777216)
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	@ManyToOne
	@JoinColumn(name = "creator")
	private User creator;

	@ManyToOne
	@JoinColumn(name = "updater")
	private User updater;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private CommentType type = CommentType.Reply;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Version
	private long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
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

	public CommentType getType() {
		return type;
	}

	public void setType(CommentType type) {
		this.type = type;
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

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	@Override
	public String toString() {
		return "Comment [id=" + id + ", targetId=" + targetId + ", content=" + content + ", voteZan=" + voteZan
				+ ", voteCai=" + voteCai + ", voteZanCnt=" + voteZanCnt + ", voteCaiCnt=" + voteCaiCnt + ", creator="
				+ creator + ", updater=" + updater + ", status=" + status + ", type=" + type + ", createDate="
				+ createDate + ", updateDate=" + updateDate + ", version=" + version + "]";
	}

}