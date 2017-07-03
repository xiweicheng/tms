package com.lhjz.portal.util;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

public class AuthUtil {

	public static boolean isSuper() {
		return WebUtil.getUserAuthorities().contains(SysConstant.ROLE_SUPER);
	}

	public static boolean isCreator(String creator) {
		return WebUtil.getUsername().equals(creator);
	}

	public static boolean isSuperOrCreator(String creator) {
		return isSuper() || isCreator(creator);
	}

	public static boolean isSuperOrCreator(User creator) {
		if (creator == null) {
			return false;
		}
		return isSuperOrCreator(creator.getUsername());
	}

	public static boolean hasChannelAuth(ChatChannel cc) {

		if (cc == null) {
			return false;
		}

		if (isSuperOrCreator(cc.getCreator().getUsername())) {
			return true;
		}

		return hasChannelAuth(cc.getChannel());
	}

	public static boolean hasChannelAuth(Channel c) {

		if (c == null) {
			return false;
		}

		if (!c.getPrivated()) {
			return true;
		}

		User loginUser = new User(WebUtil.getUsername());
		return c.getMembers().contains(loginUser);
	}
	
	public static boolean isChannelMember(Channel c) {

		if (c == null) {
			return false;
		}

		User loginUser = new User(WebUtil.getUsername());
		return c.getMembers().contains(loginUser);
	}
	
	public static boolean hasTProjectAuth(TProject project) {
		if (project == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		if (project.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}

		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (project.getCreator().equals(loginUser)) { // creator
			return true;
		}
		// 过滤掉没有权限的
		if (project.getLeader().equals(loginUser)) { // leader
			return true;
		}

		return project.getMembers().contains(loginUser);
	}
	
	public static boolean isTProjectLeader(TProject project) {
		return new User(WebUtil.getUsername()).equals(project.getLeader());
	}

}
