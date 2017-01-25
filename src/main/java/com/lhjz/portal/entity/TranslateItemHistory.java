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
public class TranslateItemHistory implements Serializable,
		Comparable<TranslateItemHistory> {

	private static final long serialVersionUID = 6527806743802607179L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 16777216)
	private String itemContent;

	private String itemCreator;

	private String creator;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date itemCreateDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Version
	private long version;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "translate_item_id")
	private TranslateItem translateItem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getItemContent() {
		return itemContent;
	}

	public void setItemContent(String itemContent) {
		this.itemContent = itemContent;
	}

	public String getItemCreator() {
		return itemCreator;
	}

	public void setItemCreator(String itemCreator) {
		this.itemCreator = itemCreator;
	}

	public Date getItemCreateDate() {
		return itemCreateDate;
	}

	public void setItemCreateDate(Date itemCreateDate) {
		this.itemCreateDate = itemCreateDate;
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

	public TranslateItem getTranslateItem() {
		return translateItem;
	}

	public void setTranslateItem(TranslateItem translateItem) {
		this.translateItem = translateItem;
	}

	@Override
	public String toString() {
		return "TranslateItemHistory [id=" + id + ", itemContent="
				+ itemContent + ", itemCreator=" + itemCreator + ", creator="
				+ creator + ", status=" + status + ", itemCreateDate="
				+ itemCreateDate + ", createDate=" + createDate + ", version="
				+ version + "]";
	}

	@Override
	public int compareTo(TranslateItemHistory o) {
		return -this.getCreateDate().compareTo(o.getCreateDate());
	}

}
