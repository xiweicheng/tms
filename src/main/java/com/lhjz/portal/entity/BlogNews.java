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
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.BlogPayload.Cmd;
import com.lhjz.portal.pojo.Enum.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogNews implements Serializable {

	private static final long serialVersionUID = 8846432423302544925L;

	@Id
	@GeneratedValue
	private Long id;

	// 博文id
	private Long bid;

	// 博文评论id
	private Long cid;

	// 消息属主
	@Column(name = "_to")
	private String to;
	
	private String username;

	@Column
	private String title;

	@Column
	private String content;

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

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private Cmd cmd;

	@Version
	private long version;

}
