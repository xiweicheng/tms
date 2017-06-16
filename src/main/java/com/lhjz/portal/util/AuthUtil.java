package com.lhjz.portal.util;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;

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

}
