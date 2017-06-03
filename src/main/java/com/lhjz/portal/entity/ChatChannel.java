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
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString(exclude = {"chatLabels"})
@EqualsAndHashCode(of = "id")
public class ChatChannel implements Serializable {

	private static final long serialVersionUID = 2823425313949085614L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

	@Column(length = 16777216)
	private String content;

	private Boolean openEdit;

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

	@Column(length = 16777216)
	private String voteZan;

	@Column(length = 16777216)
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	@Version
	private long version;

	@OneToMany(mappedBy = "chatChannel", cascade = { CascadeType.REMOVE })
	List<ChatLabel> chatLabels = new ArrayList<>();

}
