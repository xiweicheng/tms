/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.BlogType;
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
public class Blog implements Serializable {

	private static final long serialVersionUID = -2895818776405578846L;

	@Id
	@GeneratedValue
	private Long id;

	private String title;

	@Column(length = 16777216)
	private String content;

	@Column
	private Boolean openEdit = Boolean.FALSE;

	@Column
	private Boolean privated = Boolean.FALSE;
	
	@Column
	private Boolean opened = Boolean.FALSE;

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
	private BlogType type = BlogType.Own;

	@Column(length = 16777216)
	private String voteZan;

	@Column(length = 16777216)
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	private Long readCnt;

	@ManyToOne
	@JoinColumn(name = "space")
	private Space space;

	@OneToMany(mappedBy = "blog", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<BlogAuthority> blogAuthorities = new HashSet<>();

	@Version
	private long version;

	public String getContent() {
		return content;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public User getCreator() {
		return creator;
	}

	public Long getId() {
		return id;
	}

	public Boolean getOpenEdit() {
		return openEdit;
	}

	public Boolean getPrivated() {
		return privated;
	}

	public Status getStatus() {
		return status;
	}

	public String getTitle() {
		return title;
	}

	public BlogType getType() {
		return type;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public User getUpdater() {
		return updater;
	}

	public long getVersion() {
		return version;
	}

	public String getVoteCai() {
		return voteCai;
	}

	public Integer getVoteCaiCnt() {
		return voteCaiCnt;
	}

	public String getVoteZan() {
		return voteZan;
	}

	public Integer getVoteZanCnt() {
		return voteZanCnt;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOpenEdit(Boolean openEdit) {
		this.openEdit = openEdit;
	}

	public void setPrivated(Boolean privated) {
		this.privated = privated;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(BlogType type) {
		this.type = type;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public void setUpdater(User updater) {
		this.updater = updater;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void setVoteCai(String voteCai) {
		this.voteCai = voteCai;
	}

	public void setVoteCaiCnt(Integer voteCaiCnt) {
		this.voteCaiCnt = voteCaiCnt;
	}

	public void setVoteZan(String voteZan) {
		this.voteZan = voteZan;
	}

	public void setVoteZanCnt(Integer voteZanCnt) {
		this.voteZanCnt = voteZanCnt;
	}

	public Long getReadCnt() {
		return readCnt;
	}

	public void setReadCnt(Long readCnt) {
		this.readCnt = readCnt;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public Set<BlogAuthority> getBlogAuthorities() {
		return blogAuthorities;
	}

	public void setBlogAuthorities(Set<BlogAuthority> blogAuthorities) {
		this.blogAuthorities = blogAuthorities;
	}

	public Boolean getOpened() {
		return opened;
	}

	public void setOpened(Boolean opened) {
		this.opened = opened;
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
		Blog other = (Blog) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Blog [id=" + id + ", title=" + title + ", content=" + content + ", openEdit=" + openEdit + ", privated="
				+ privated + ", opened=" + opened + ", creator=" + creator + ", updater=" + updater + ", createDate="
				+ createDate + ", updateDate=" + updateDate + ", status=" + status + ", type=" + type + ", voteZan="
				+ voteZan + ", voteCai=" + voteCai + ", voteZanCnt=" + voteZanCnt + ", voteCaiCnt=" + voteCaiCnt
				+ ", readCnt=" + readCnt + ", space=" + space + ", version=" + version + "]";
	}

}
