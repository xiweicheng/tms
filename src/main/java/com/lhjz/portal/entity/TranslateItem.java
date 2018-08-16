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

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
@Data
@EqualsAndHashCode(of = { "id" })
@ToString(excludes = { "translate", "language", "translateItemHistories" })
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
	Set<TranslateItemHistory> translateItemHistories = new HashSet<>();

}
