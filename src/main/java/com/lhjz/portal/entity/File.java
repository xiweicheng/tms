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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.ToType;

import lombok.Data;
import lombok.ToString;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:03:20
 * 
 */
@Data
@ToString(exclude = { "fileTranslates" })
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "uuid" }) }, indexes = { @Index(columnList = "name"),
		@Index(columnList = "uuidName"), @Index(columnList = "uuid"), @Index(columnList = "toId"),
		@Index(columnList = "username") })
public class File implements Serializable {

	/** serialVersionUID (long) */
	private static final long serialVersionUID = 4730479799042412659L;

	@Id
	@GeneratedValue
	private Long id;

	private String uuid;

	@NotBlank
	private String name;

	@NotBlank
	private String uuidName;

	@NotBlank
	private String username;

	@NotBlank
	private String path;

	@Enumerated(EnumType.STRING)
	private ToType toType;

	private String toId;

	// 上传所在频道消息，博文评论等的ID
	private String atId;

	@Enumerated(EnumType.STRING)
	private FileType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "file_translate", joinColumns = { @JoinColumn(name = "file_id") }, inverseJoinColumns = {
			@JoinColumn(name = "translate_id") })
	private Set<Translate> fileTranslates = new HashSet<Translate>();

	@Version
	private long version;

}
