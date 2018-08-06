/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.base;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Log;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Message;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author weichx
 * 
 * @date Apr 2, 2015 2:59:47 PM
 * 
 */
public abstract class BaseController {

	@Autowired
	protected LogRepository logRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	protected Environment env;

	protected Log log(Action action, Target target, String targetId,
			Object... vals) {

		return logWithProperties(action, target, targetId, null, vals);
	}

	protected Log log(Action action, Target target, Long targetId,
			Object... vals) {

		return logWithProperties(action, target, targetId, null, vals);
	}

	protected User getLoginUser() {
		return userRepository.findOne(WebUtil.getUsername());
	}

	protected User getUser(String username) {
		return userRepository.findOne(username);
	}

	protected Log logWithProperties(Action action, Target target,
			Long targetId, String properties, Object... vals) {

		return logWithProperties(action, target, String.valueOf(targetId),
				properties, vals);
	}

	protected Log logWithProperties(Action action, Target target,
			String targetId, String properties, Object... vals) {

		Log log = new Log();
		log.setAction(action);
		log.setTarget(target);
		log.setCreateDate(new Date());
		log.setProperties(properties);

		if (StringUtil.isNotEmpty(targetId)) {
			log.setTargetId(String.valueOf(targetId));
		}

		if (vals.length > 0) {
			log.setNewValue(vals[0] != null ? String.valueOf(vals[0]) : StringUtils.EMPTY);
		}

		if (vals.length > 1) {
			log.setOldValue(vals[1] != null ? String.valueOf(vals[1]) : StringUtils.EMPTY);
		}

		log.setCreator(getLoginUser());

		return logRepository.saveAndFlush(log);

	}
	
	protected boolean isSuper() {
		return WebUtil.getUserAuthorities().contains(SysConstant.ROLE_SUPER);
	}

	protected boolean isCreator(String creator) {
		return WebUtil.getUsername().equals(creator);
	}

	protected boolean isSuperOrCreator(String creator) {
		return isSuper() || isCreator(creator);
	}
	
	protected boolean isSuperOrCreator(User creator) {
		if (creator == null) {
			return false;
		}
		return isSuperOrCreator(creator.getUsername());
	}
	
	protected String getLoginUserName(User user) {
		return StringUtil.isEmpty(user.getName()) ? user.getUsername() : user.getName();
	}
	
	protected String getLoginUserName() {
		User user = getLoginUser();
		if (user == null) {
			return StringUtil.EMPTY;
		}
		return StringUtil.isEmpty(user.getName()) ? user.getUsername() : user.getName();
	}

	@SuppressWarnings("unchecked")
	@ExceptionHandler(Exception.class)
	public ModelAndView exceptionHandler(HttpServletRequest request,
			HttpServletResponse response, Exception ex) {

		if (Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains(
				request.getHeader("X-Requested-With"))) {

			return new ModelAndView(new MappingJackson2JsonView(),
					(Map<String, ?>) RespBody.failed(ex.getMessage())
							.addMsg(ex.toString()).asMap());
		}

		return new ModelAndView("admin/error", "error", Message.error(
				ex.getMessage()).detail(ex.toString()));
	}
}
