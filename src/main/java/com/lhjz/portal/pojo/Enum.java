package com.lhjz.portal.pojo;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * @author xi
 * 
 * @date 2015年4月25日 上午11:25:00
 * 
 */
public class Enum {

	public static enum Status {
		Updated, Unknow, Normal, Deleted, Bultin, New, Opening, Analyzing, Accepted, Processing, Resolved, Closed, Ignored, Failed, Checked, Readed, Disabled, Doing, Done;
	}

	public static enum Action {
		Create, Read, Update, Delete, Upload, Visit, Import, Export, Vote, UnVote;
	}

	public static enum OnlineStatus {
		Online, Busy, Left, Offline;
	}

	public static enum FileType {
		Image, Attachment;
	}

	public static enum ChatType {
		Wiki, Msg;
	}

	public static enum ChatMsgType {
		Content, Label, Reply;
	}

	public static enum ChatReplyType {
		ChatChannel;
	}

	public static enum BlogType {
		Team, Own;
	}

	public static enum TagType {
		Blog;
	}

	public static enum SpaceType {
		Team, Own;
	}

	public static enum ChannelType {
		Common;
	}

	public static enum CommentType {
		Reply, Blog;
	}

	public static enum Prop {
		Key, Content, Name, Id, Watchers, Labels, TranslateItem, Title;
	}

	public static enum Target {
		Translate, Label, TranslateItem, Language, Chat, ChatChannel, ChatLabel, ChatReply, Blog, Comment, ChatDirect, Project, Article, File, Feedback, Diagnose, Settings, Page, User, Authority, Config, Feature, Case, Product, Env, Health, Job, JobApply, Import;
	}

	public static enum Page {
		Unknow, Index, About, Feature, Case, Product, Env, Health, Job, Diagnose, Contact;
	}

	public static enum Module {
		Unknow, BigImg, HotNews, MoreNews, Branch, Expert, More, Introduction;
	}

	public static enum Key {
		Unknow, Contact, PageEnable;
	}

	public static enum Role {
		ROLE_USER, ROLE_ADMIN, ROLE_SUPER;
	}

	public static enum VoteType {
		Zan, Cai;
	}

	public static enum SettingType {
		Mail;
	}

	public static enum ToType {
		Channel, User, Blog, Feedback;
	}
	
	public static enum Editor {
		Markdown, Html;
	}

	public static enum LinkType {
		Channel, User, App;
	}

	public static enum ScheduleType {
		Task, Meeting;
	}

	public static enum ChatLabelType {
		Emoji, Tag;
	}

	public static enum SchedulePriority {
		High, Middle, Low;
	}

	public static enum TodoPriority {
		Default, ZyJj, ZyBjj, BzyJi, BzyBjj;
	}

	public static enum ScheduleKnowStatus {
		Yes, No;
	}
	
	public static enum JenkinsStatus {

		SUCCESS("成功"), FAILURE("失败");

		private String name;

		private JenkinsStatus(String name) {
			this.name = name;
		}

		@JsonValue
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return String.valueOf(name);
		}
	}

	public static enum Code {
		Created(200), Readed(201), Updated(202), Deleted(203);

		private Integer value;

		private Code(Integer value) {
			this.value = value;
		}

		@JsonValue
		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

	}

}
