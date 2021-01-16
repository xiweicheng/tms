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
import javax.persistence.ManyToMany;
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
import com.lhjz.portal.pojo.Enum.BlogType;
import com.lhjz.portal.pojo.Enum.Editor;
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
@ToString(exclude = { "blogAuthorities", "tags" })
@EqualsAndHashCode(of = { "id", "content" })
public class Blog implements Serializable {

	private static final long serialVersionUID = -2895818776405578846L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String title;
	
	@Column(length = 100)
	private String uuid;

	@Lob
	@Column
	private String content;

	@Enumerated(EnumType.STRING)
	@Column
	private Editor editor;

	@Column
	private Boolean openEdit = Boolean.FALSE;

	@Column
	private Boolean privated = Boolean.FALSE;

	@Column
	private Boolean opened = Boolean.FALSE;

	@Column
	private Integer tpl; // 模板：1：privated 2：opened 其他：非模板

	@Column(length = 2000)
	private String tplDesc;

	@ManyToOne
	@JoinColumn(name = "dir")
	private Dir dir;

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
	private BlogType type = BlogType.Own;

	@Lob
	@Column
	private String voteZan;

	@Lob
	@Column
	private String voteCai;

	private Integer voteZanCnt;

	private Integer voteCaiCnt;

	private Long readCnt;

	private Long tplHotCnt; // 模板热度统计

	private String shareId;

	private Long sort; // 排序位置

	@ManyToOne
	@JoinColumn(name = "locker")
	private User locker; // 编辑中的加锁用户

	@Temporal(TemporalType.TIMESTAMP)
	private Date lockDate; // 编辑中的加锁时间

	@ManyToOne
	@JoinColumn(name = "space")
	private Space space;

	@OneToMany(mappedBy = "blog", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<BlogAuthority> blogAuthorities = new HashSet<>();

	@Version
	private long version;

	@ManyToMany(mappedBy = "blogs", fetch = FetchType.EAGER)
	private Set<Tag> tags = new HashSet<Tag>();

}
