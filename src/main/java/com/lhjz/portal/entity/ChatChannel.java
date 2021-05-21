/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.ChatType;
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
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "uuid" }) }, indexes = { @Index(columnList = "uuid") })
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString(exclude = { "chatLabels", "chatReplies" })
@EqualsAndHashCode(of = "id")
public class ChatChannel implements Serializable {

	private static final long serialVersionUID = 2823425313949085614L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

	@Lob
	@Column
	private String content;

	@Column(length = 1000)
	private String ua;
	
	@Column(length = 100)
	private String uuid;

	private Boolean openEdit;

	// 是否是公告消息
	private Boolean notice;

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
	private ChatType type = ChatType.Msg;

	@Lob
	@Column
	private String voteZan;

	@Lob
	@Column
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;
	
	@Version
	private long version;

	@OneToMany(mappedBy = "chatChannel", cascade = { CascadeType.REMOVE })
	List<ChatLabel> chatLabels = new ArrayList<>();

	@JsonIgnore
	@OneToOne(mappedBy = "chatChannel", cascade = { CascadeType.REMOVE })
	ChatPin chatPin;

	@OneToMany(mappedBy = "chatChannel", cascade = { CascadeType.REMOVE })
	List<ChatReply> chatReplies = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "chatChannel", cascade = { CascadeType.REMOVE })
	List<ChatChannelFollower> chatChannelFollowers = new ArrayList<>();

}
