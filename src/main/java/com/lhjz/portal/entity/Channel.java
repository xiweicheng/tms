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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.ChannelType;
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
@ToString(exclude = "members")
@EqualsAndHashCode(of = { "id" })
public class Channel implements Serializable {

	private static final long serialVersionUID = 1864577736341309316L;

	@Id
	@GeneratedValue
	private Long id;

	@Pattern(regexp = "^[a-z][a-z0-9_\\-]{2,49}$", message = "频道名称必须是3到50位小写字母数字_-组合,并且以字母开头!")
	@Column(unique = true, nullable = false)
	private String name;

	@Column
	private String title;

	@Column(length = 2000)
	private String description;

	@Column
	private Boolean privated = Boolean.FALSE;

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

	@ManyToOne
	@JoinColumn(name = "owner")
	private User owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.New;

	@Enumerated(EnumType.STRING)
	@Column
	private ChannelType type = ChannelType.Common;

	@Version
	private long version;

	@ManyToMany(mappedBy = "joinChannels")
	Set<User> members = new HashSet<User>();
	
	@ManyToMany(mappedBy = "subscribeChannels")
	Set<User> subscriber = new HashSet<User>();

}
