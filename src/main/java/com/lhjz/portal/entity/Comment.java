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
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Editor;
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
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "uuid" }) }, indexes = { @Index(columnList = "uuid") })
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString(exclude = { "labels" })
public class Comment implements Serializable {

	private static final long serialVersionUID = -1213448577430547620L;

	@Id
	@GeneratedValue
	private Long id;

	private String targetId;

	@Lob
	@Column
	private String content;

	@Lob
	@Column
	private String voteZan;

	@Lob
	@Column
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;
	
	@Column(length = 100)
	private String uuid;

	@ManyToOne
	@JoinColumn(name = "creator")
	@CreatedBy
	private User creator;

	@ManyToOne
	@JoinColumn(name = "updater")
	@LastModifiedBy
	private User updater;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private Editor editor;

	@Enumerated(EnumType.STRING)
	@Column
	private CommentType type = CommentType.Blog;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	private Date updateDate;

	@Version
	private long version;

	@OneToMany(mappedBy = "comment", fetch = FetchType.EAGER)
	private Set<Label> labels = new HashSet<Label>();

}