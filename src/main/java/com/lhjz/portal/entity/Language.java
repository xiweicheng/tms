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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.Data;
import lombok.ToString;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Entity
@Data
@ToString(exclude = { "projects", "translateItems" })
public class Language implements Serializable {

	private static final long serialVersionUID = -4972944149048452619L;

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@Column(length = 2000)
	private String description;

	private String creator;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Version
	private long version;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "language_project", joinColumns = { @JoinColumn(name = "language_id") }, inverseJoinColumns = {
			@JoinColumn(name = "project_id") })
	private Set<Project> projects = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "language")
	private Set<TranslateItem> translateItems = new HashSet<>();

}
