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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.ChatType;
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
@ToString(exclude = { "creator", "updater", "voters", "labels" })
public class Chat implements Serializable {

	private static final long serialVersionUID = -1213448577430547620L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 16777216)
	private String content;

	@Column(length = 16777216)
	private String voteZan;

	@Column(length = 16777216)
	private String voteCai;

	private Boolean openEdit;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	@ManyToOne
	@JoinColumn(name = "creator")
	private User creator;

	@ManyToOne
	@JoinColumn(name = "updater")
	private User updater;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private ChatType type = ChatType.Msg;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Version
	private long version;

	@ManyToMany(mappedBy = "voterChats")
	Set<User> voters = new HashSet<>();

	@Column
	private Boolean privated = Boolean.FALSE;

	@Column
	private String title;

	@OneToMany(mappedBy = "chat", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	Set<Label> labels = new HashSet<>();

}
