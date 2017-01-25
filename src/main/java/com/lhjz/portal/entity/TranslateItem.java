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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
public class TranslateItem implements Serializable {

	private static final long serialVersionUID = -7590249673888211416L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 16777216)
	private String content;

	private String creator;

	private String updater;

	private String translator;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date translateDate;

	@Version
	private long version;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "translate_id")
	private Translate translate;

	@ManyToOne
	@JoinColumn(name = "language_id")
	private Language language;

	@OneToMany(mappedBy = "translateItem", cascade = { CascadeType.REMOVE })
	Set<TranslateItemHistory> translateItemHistories = new HashSet<TranslateItemHistory>();

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

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Translate getTranslate() {
		return translate;
	}

	public void setTranslate(Translate translate) {
		this.translate = translate;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public String getTranslator() {
		return translator;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getTranslateDate() {
		return translateDate;
	}

	public void setTranslateDate(Date translateDate) {
		this.translateDate = translateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		return result;
	}

	public Set<TranslateItemHistory> getTranslateItemHistories() {
		return translateItemHistories;
	}

	public void setTranslateItemHistories(
			Set<TranslateItemHistory> translateItemHistories) {
		this.translateItemHistories = translateItemHistories;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TranslateItem other = (TranslateItem) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TranslateItem [id=" + id + ", content=" + content
				+ ", creator=" + creator + ", updater=" + updater
				+ ", translator=" + translator + ", status=" + status
				+ ", createDate=" + createDate + ", updateDate=" + updateDate
				+ ", translateDate=" + translateDate + ", version=" + version
				+ "]";
	}

}
