package com.lhjz.portal.util;

import java.util.Set;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.SpaceAuthority;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

public class AuthUtil {

	private AuthUtil() {
	}

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

	public static boolean hasChannelAuth(ChatReply cr) {

		if (cr == null) {
			return false;
		}

		return hasChannelAuth(cr.getChatChannel());
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

		if (Boolean.FALSE.equals(c.getPrivated())) {
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

	public static boolean hasSpaceAuth(Space s) {

		if (s == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		if (s.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}

		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (s.getCreator().equals(loginUser)) { // 我创建的
			return true;
		}

		if (Boolean.TRUE.equals(s.getOpened())) {
			return true;
		}

		if (Boolean.FALSE.equals(s.getPrivated())) { // 非私有的
			return true;
		}

		boolean exists = false;
		for (SpaceAuthority sa : s.getSpaceAuthorities()) {
			if (loginUser.equals(sa.getUser())) {
				exists = true;
				break;
			} else {
				Channel channel = sa.getChannel();
				if (channel != null) {
					Set<User> members = channel.getMembers();
					if (members.contains(loginUser)) {
						exists = true;
						break;
					}
				}
			}
		}

		return exists;
	}

	public static boolean hasChannelAuth(ChatDirect chatDirect) {

		if (chatDirect == null) {
			return false;
		}

		if (isSuperOrCreator(chatDirect.getCreator().getUsername())) {
			return true;
		}

		User loginUser = new User(WebUtil.getUsername());

		return chatDirect.getChatTo().equals(loginUser);
	}

}
