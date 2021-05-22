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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = { "name" })
@ToString(exclude = { "languages", "users", "translates", "watchers", "language" })
public class Project implements Serializable, Comparable<Project> {

	private static final long serialVersionUID = -278833537706540131L;

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@Column(length = 2000)
	private String description;

	private String creator;

	private String updater;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Version
	private long version;

	@ManyToMany(mappedBy = "projects")
	private Set<Language> languages = new HashSet<>();

	@ManyToMany(mappedBy = "projects")
	private Set<User> users = new HashSet<User>();

	@JsonIgnore
	@OneToMany(mappedBy = "project")
	private Set<Translate> translates = new HashSet<>();

	@ManyToMany(mappedBy = "watcherProjects")
	private Set<User> watchers = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "language_id")
	private Language language;

	@Override
	public int compareTo(Project o) {
		return getName().compareToIgnoreCase(o.getName());
	}

}
