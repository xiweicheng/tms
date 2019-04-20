/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Data
@EqualsAndHashCode(of = { "id", "name" })
@ToString(exclude = { "translate", "chat" })
public class Label implements Serializable {

	/** serialVersionUID (long) */
	private static final long serialVersionUID = -9182071299487898824L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 2000)
	private String description;

	private String creator;

	private String updater;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.Normal;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Version
	private long version;

	@JsonIgnore
	@ManyToOne
	private Translate translate;

	@JsonIgnore
	@ManyToOne
	private Chat chat;

}
