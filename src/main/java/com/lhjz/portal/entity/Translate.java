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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotBlank;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Search;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
public class Translate implements Serializable {

	/** serialVersionUID (long) */
	private static final long serialVersionUID = 4730479799042412659L;

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Column(nullable = false, name = "_key")
	private String key;

	@Column(length = 2000)
	private String description;

	@NotBlank
	private String creator;

	private String updater;

	private String translator;

	@Lob
	@Column
	private String search;

	@Version
	private long version;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date translateDate;

	@ManyToOne()
	@JoinColumn(name = "project_id")
	private Project project;

	@OneToMany(mappedBy = "translate", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Set<TranslateItem> translateItems = new HashSet<TranslateItem>();

	@ManyToMany(mappedBy = "watcherTranslates")
	Set<User> watchers = new HashSet<User>();

	@OneToMany(mappedBy = "translate", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<Label> labels = new HashSet<Label>();

	@ManyToMany(mappedBy = "fileTranslates", cascade = CascadeType.REMOVE)
	Set<File> files = new HashSet<File>();

	@Override
	public String toString() {

		return Search.instance().translate(this).toString();
	}

}
