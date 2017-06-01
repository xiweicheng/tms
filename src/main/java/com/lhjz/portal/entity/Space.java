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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.SpaceType;
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
public class Space implements Serializable {

	private static final long serialVersionUID = 1036120023938526638L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String name;

	@Column(length = 16777216)
	private String description;

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
	private SpaceType type = SpaceType.Own;

	@JsonIgnore
	@OneToMany(mappedBy = "space")
	Set<Blog> blogs = new HashSet<Blog>();
	
	@OneToMany(mappedBy = "space", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<SpaceAuthority> spaceAuthorities = new HashSet<>();

	@Version
	private long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public SpaceType getType() {
		return type;
	}

	public void setType(SpaceType type) {
		this.type = type;
	}

	public Set<Blog> getBlogs() {
		return blogs;
	}

	public void setBlogs(Set<Blog> blogs) {
		this.blogs = blogs;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Set<SpaceAuthority> getSpaceAuthorities() {
		return spaceAuthorities;
	}

	public void setSpaceAuthorities(Set<SpaceAuthority> spaceAuthorities) {
		this.spaceAuthorities = spaceAuthorities;
	}

	public Boolean getOpened() {
		return opened;
	}

	public void setOpened(Boolean opened) {
		this.opened = opened;
	}

	@Override
	public String toString() {
		return "Space [id=" + id + ", name=" + name + ", description=" + description + ", privated=" + privated
				+ ", opened=" + opened + ", creator=" + creator + ", updater=" + updater + ", createDate=" + createDate
				+ ", updateDate=" + updateDate + ", status=" + status + ", type=" + type + ", blogs=" + blogs
				+ ", version=" + version + "]";
	}

}
