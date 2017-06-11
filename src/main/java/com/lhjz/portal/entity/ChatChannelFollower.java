/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.Data;

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
public class ChatChannelFollower implements Serializable {

	private static final long serialVersionUID = -4693843343986879072L;

	@Id
	@GeneratedValue
	private Long id;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "chat_channel_id")
	private ChatChannel chatChannel;

	@ManyToOne
	@JoinColumn(name = "creator")
	@CreatedBy
	private User creator;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Version
	private long version;

}