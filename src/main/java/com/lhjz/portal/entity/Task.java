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
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TaskPriority;
import com.lhjz.portal.pojo.Enum.TaskType;

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
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString(exclude = { "subtasks", "labels", "links", "attachments", "comments" })
@EqualsAndHashCode(of = "id")
public class Task implements Serializable {

	private static final long serialVersionUID = 5158822915599451409L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project")
	private TProject project;

	private Long projectTaskId;

	private String title;

	@Column(length = 16777216)
	private String description;

	@ManyToOne
	@JoinColumn(name = "state")
	private TStatus state;

	@ManyToOne
	@JoinColumn(name = "module")
	private TModule module;

	@OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<TLabel> labels = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "effect_version")
	private TVersion effectVersion;

	@ManyToOne
	@JoinColumn(name = "resolved_version")
	private TVersion resolvedVersion;

	@ManyToOne
	@JoinColumn(name = "epic")
	private TEpic epic;

	@ManyToOne
	@JoinColumn(name = "reporter")
	private User reporter;

	@ManyToOne
	@JoinColumn(name = "operator")
	private User operator;

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
	@Column(nullable = false)
	private TaskType type = TaskType.Task;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskPriority priority = TaskPriority.Medium;

	@OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<TLink> links = new HashSet<>();

	@OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<TAttachment> attachments = new HashSet<>();

	@OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<TComment> comments = new HashSet<>();

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "parent_task")
	private Task parentTask;

	@OneToMany(mappedBy = "parentTask", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<Task> subtasks = new HashSet<>();

	@Version
	private long version;

}
