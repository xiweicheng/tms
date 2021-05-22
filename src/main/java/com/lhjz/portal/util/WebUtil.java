/**
 * WebUtil.java
 */
package com.lhjz.portal.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author XiWeiCheng
 */
public final class WebUtil {

    public static final String UNKNOWN = "unknown";
    private static Logger logger = LoggerFactory.getLogger(WebUtil.class);

    private WebUtil() {

    }

    /**
     * 返回servlet context的绝对路径
     *
     * @param request
     * @return
     * @author xiweicheng
     * @creation 2014年3月29日 下午4:25:29
     * @modification 2014年3月29日 下午4:25:29
     */
    public static String getRealPath(HttpServletRequest request) {

        String realPath = request.getSession().getServletContext()
                .getRealPath("/").replace("\\", "/");

        if (!realPath.endsWith("/")) {
            return realPath + "/";
        }

        logger.debug("servlet context real path: {}", realPath);

        return realPath;
    }

    /**
     * 获取验证登录的用户名
     *
     * @return
     */
    public static String getUsername() {

        try {
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        } catch (Exception e) {
            logger.warn("获取登录用户名错误，将返回空字符串。 错误信息 ：{}", e.getMessage());
            return StringUtil.EMPTY;
        }
    }

    /**
     * 设置登录的用户名
     *
     * @return
     */
    public static void setUsername(String username) {

        try {
            AbstractAuthenticationToken authenticationToken = new AbstractAuthenticationToken(null) {

                private static final long serialVersionUID = 1033003540219681089L;

                @Override
                public Object getPrincipal() {
                    return username;
                }

                @Override
                public Object getCredentials() {
                    return null;
                }
            };
            authenticationToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 判断用户是否登录.
     *
     * @return
     */
    public static boolean isLogin() {
        String username = getUsername();
        return StringUtil.isNotEmpty(username) && !"anonymousUser".equals(username);
    }

    /**
     * 获取验证登录的用户信息
     *
     * @return
     */
    public static UserDetails getUserDetails() {

        try {
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal);
            } else {
                logger.warn("获取登录用户信息类型错误。 用户信息 ：{}", principal);
                return null;
            }
        } catch (Exception e) {
            logger.warn("获取登录用户信息错误。 错误信息 ：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取验证登录的用户权限
     *
     * @return
     */
    public static List<String> getUserAuthorities() {

        List<String> aus = new ArrayList<>();

        try {
            UserDetails userDetails = WebUtil.getUserDetails();
            if (userDetails != null) {
                Collection<? extends GrantedAuthority> authorities = userDetails
                        .getAuthorities();
                authorities.forEach(au ->
                        aus.add(au.getAuthority())
                );
            }

        } catch (Exception e) {
            logger.warn("获取登录用户权限信息错误。 错误信息 ：{}", e.getMessage());
        }

        return aus;
    }

    /**
     * 检测用户是不是通过 remember me cookie 登录的.
     *
     * @return
     */
    public static boolean isRememberMeAuthenticated() {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            return false;
        }

        return RememberMeAuthenticationToken.class
                .isAssignableFrom(authentication.getClass());
    }

    public static String getIpAddr(final HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个路由时，取第一个非unknown的ip
        final String[] arr = ip.split(",");
        for (final String str : arr) {
            if (!UNKNOWN.equalsIgnoreCase(str)) {
                ip = str;
                break;
            }
        }

        return ip;
    }

}
