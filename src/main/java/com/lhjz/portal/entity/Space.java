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
import javax.persistence.Lob;
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
import com.lhjz.portal.pojo.Enum.SpaceType;
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
@EqualsAndHashCode(of = { "id" })
@ToString(exclude = { "blogs", "spaceAuthorities", "dirs", "channel" })
public class Space implements Serializable {

	private static final long serialVersionUID = 1036120023938526638L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String name;

	@Lob
	@Column
	private String description;

	@Column
	private Boolean privated = Boolean.FALSE;

	@Column
	private Boolean opened = Boolean.FALSE;

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
	private SpaceType type = SpaceType.Own;

	@JsonIgnore
	@OneToMany(mappedBy = "space")
	private Set<Blog> blogs = new HashSet<Blog>();

	@OneToMany(mappedBy = "space", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<SpaceAuthority> spaceAuthorities = new HashSet<>();

	@OneToMany(mappedBy = "space", fetch = FetchType.EAGER)
	private Set<Dir> dirs = new HashSet<Dir>();

	@ManyToOne
	@JoinColumn(name = "channel")
	private Channel channel;

	@Column
	private Long sort;

	@Version
	private long version;

}
