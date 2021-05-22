package com.lhjz.portal.pojo;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author xi
 * @date 2015年4月25日 上午11:25:00
 */
public class Enum {

    public enum Status {
        Updated, Unknow, Normal, Deleted, Bultin, New, Opening, Analyzing, Accepted, Processing, Resolved, Closed, Ignored, Failed, Checked, Readed, Disabled, Doing, Done;
    }

    public enum Action {
        Create, Read, Update, Delete, Upload, Visit, Import, Export, Vote, UnVote;
    }

    public enum OnlineStatus {
        Online, Busy, Left, Offline;
    }

    public enum FileType {
        Image, Attachment;
    }

    public enum ChatType {
        Wiki, Msg;
    }

    public enum ChatMsgType {
        Content, Label, Reply, OpenEdit;
    }

    public enum ChatReplyType {
        ChatChannel;
    }

    public enum BlogType {
        Team, Own;
    }

    public enum TagType {
        Blog;
    }

    public enum SpaceType {
        Team, Own;
    }

    public enum ChannelType {
        Common;
    }

    public enum CommentType {
        Reply, Blog;
    }

    public enum Prop {
        Key, Content, Name, Id, Watchers, Labels, TranslateItem, Title;
    }

    public enum Target {
        Translate, Label, TranslateItem, Language, Chat, ChatChannel, ChatLabel, ChatReply, Blog, Comment, ChatDirect, Project, Article, File, Feedback, Diagnose, Settings, Page, User, Authority, Config, Feature, Case, Product, Env, Health, Job, JobApply, Import;
    }

    public enum Page {
        Unknow, Index, About, Feature, Case, Product, Env, Health, Job, Diagnose, Contact;
    }

    public enum Module {
        Unknow, BigImg, HotNews, MoreNews, Branch, Expert, More, Introduction;
    }

    public enum Key {
        Unknow, Contact, PageEnable;
    }

    public enum Role {
        ROLE_USER, ROLE_ADMIN, ROLE_SUPER;
    }

    public enum VoteType {
        Zan, Cai;
    }

    public enum SettingType {
        Mail, Menus;
    }

    public enum ToType {
        Channel, User, Blog, Feedback;
    }

    public enum Editor {
        Markdown, Html, Mind, Excel;
    }

    public enum LinkType {
        Channel, User, App;
    }

    public enum ScheduleType {
        Task, Meeting;
    }

    public enum ChatLabelType {
        Emoji, Tag;
    }

    public enum SchedulePriority {
        High, Middle, Low;
    }

    public enum TodoPriority {
        Default, ZyJj, ZyBjj, BzyJi, BzyBjj;
    }

    public enum ScheduleKnowStatus {
        Yes, No;
    }

    public enum JenkinsStatus {

        SUCCESS("成功"), FAILURE("失败");

        private String name;

        private JenkinsStatus(String name) {
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    public enum GitAction {

        // open：新建合并请求  close：关闭合并请求  accept：接受合并请求 push：推送

        OPEN("新建"), CLOSE("关闭"), ACCEPT("接受"), PUSH("推送");

        private String name;

        private GitAction(String name) {
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    public enum Code {
        Created(200), Readed(201), Updated(202), Deleted(203);

        private Integer value;

        private Code(Integer value) {
            this.value = value;
        }

        @JsonValue
        public Integer getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }

}
