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

/**
 * 认证工具类
 * 提供各种权限验证的方法，用于判断用户是否有特定操作的权限
 */
public class AuthUtil {

    // 私有构造方法，防止实例化工具类
    private AuthUtil() {
    }

    /**
     * 判断当前用户是否为超级用户
     *
     * @return 如果是超级用户返回true，否则返回false
     */
    public static boolean isSuper() {
        return WebUtil.getUserAuthorities().contains(SysConstant.ROLE_SUPER);
    }

    /**
     * 判断当前用户是否为指定内容的创建者
     *
     * @param creator 创建者用户名
     * @return 如果当前用户是创建者返回true，否则返回false
     */
    public static boolean isCreator(String creator) {
        return WebUtil.getUsername().equals(creator);
    }

    /**
     * 判断当前用户是否为超级用户或内容创建者
     *
     * @param creator 创建者用户名
     * @return 如果是超级用户或创建者返回true，否则返回false
     */
    public static boolean isSuperOrCreator(String creator) {
        return isSuper() || isCreator(creator);
    }

    /**
     * 判断当前用户是否为超级用户或指定用户的创建者
     *
     * @param creator 用户对象
     * @return 如果是超级用户或创建者返回true，否则返回false
     */
    public static boolean isSuperOrCreator(User creator) {
        if (creator == null) {
            return false;
        }
        return isSuperOrCreator(creator.getUsername());
    }

    /**
     * 判断用户是否有聊天回复的频道权限
     *
     * @param cr 聊天回复对象
     * @return 如果有权限返回true，否则返回false
     */
    public static boolean hasChannelAuth(ChatReply cr) {

        if (cr == null) {
            return false;
        }

        return hasChannelAuth(cr.getChatChannel());
    }

    /**
     * 判断用户是否有聊天频道的权限
     *
     * @param cc 聊天频道对象
     * @return 如果有权限返回true，否则返回false
     */
    public static boolean hasChannelAuth(ChatChannel cc) {

        if (cc == null) {
            return false;
        }

        if (isSuperOrCreator(cc.getCreator().getUsername())) {
            return true;
        }

        return hasChannelAuth(cc.getChannel());
    }

    /**
     * 判断用户是否有频道的权限
     *
     * @param c 频道对象
     * @return 如果有权限返回true，否则返回false
     */
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

    /**
     * 判断用户是否为频道成员
     *
     * @param c 频道对象
     * @return 如果是成员返回true，否则返回false
     */
    public static boolean isChannelMember(Channel c) {

        if (c == null) {
            return false;
        }

        User loginUser = new User(WebUtil.getUsername());
        return c.getMembers().contains(loginUser);
    }

    /**
     * 判断用户是否有工作空间的权限
     *
     * @param s 工作空间对象
     * @return 如果有权限返回true，否则返回false
     */
    public static boolean hasSpaceAuth(Space s) {

        if (s == null) {
            return false;
        }

        if (isSuper()) { // 超级用户拥有所有权限
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

    /**
     * 检查当前用户是否有频道的授权权限
     *
     * @param chatDirect 聊天频道对象，包含频道相关信息
     * @return 如果有权限返回true，否则返回false
     */
    public static boolean hasChannelAuth(ChatDirect chatDirect) {

        // 如果聊天频道对象为空，直接返回false
        if (chatDirect == null) {
            return false;
        }

        // 如果当前用户是超级用户或频道创建者，返回true
        if (isSuperOrCreator(chatDirect.getCreator().getUsername())) {
            return true;
        }

        // 获取当前登录用户对象
        User loginUser = new User(WebUtil.getUsername());

        // 检查当前用户是否是频道的目标用户
        return chatDirect.getChatTo().equals(loginUser);
    }

}
