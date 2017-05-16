/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.TranslateForm;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
public class Mail {

	static Logger logger = LoggerFactory.getLogger(Mail.class);

	private Set<String> set = new HashSet<String>();

	private Map<String, String> mapHref = new HashMap<String, String>();

	private Map<String, String> map = new HashMap<String, String>();

	private Mail() {
	}

	public static Mail instance() {
		return new Mail();
	}

	public Mail parseTranslateUpdated(TranslateForm translateForm,
			Translate translate) {
		List<String> labels = translate.getLabels().stream().map((l) -> {
			return l.getName();
		}).collect(Collectors.toList());

		Collections.sort(labels);
		String oldLabels = StringUtil.join(",", labels);

		List<String> tags = new ArrayList<>();
		if (StringUtil.isNotEmpty(translateForm.getTags())) {
			tags = Arrays.asList(translateForm.getTags().split(","));
		}
		Collections.sort(tags);
		String newTags = StringUtil.join(",", tags);

		if (!translate.getKey().equals(translateForm.getKey())) {
			this.put("翻译名称", translate.getKey() + SysConstant.CHANGE_TO
					+ translateForm.getKey());
		}

		if (!oldLabels.equals(newTags)) {
			this.put("翻译标签", oldLabels + SysConstant.CHANGE_TO + newTags);

		}

		return this;
	}

	public Mail parseTranslateForm(TranslateForm translateForm) {

		this.put("翻译名称", translateForm.getKey());
		this.put("翻译标签", translateForm.getTags());

		return this;
	}

	public Mail parseTranslate(Translate translate) {

		map.put("翻译名称", translate.getKey());

		List<String> list = new ArrayList<String>();
		for (Label label : translate.getLabels()) {
			list.add(label.getName());
		}
		this.put("翻译标签", StringUtil.join(",", list));

		Set<TranslateItem> translateItems = translate.getTranslateItems();
		for (TranslateItem translateItem : translateItems) {
			String name = translateItem.getLanguage().getDescription() + "["
					+ translateItem.getLanguage().getName() + "]";
			this.put(name, translateItem.getContent());
		}

		return this;
	}

	public Mail put(String name, Object value) {

		map.put(name, String.valueOf(value));

		return this;
	}

	public String body() {

		List<String> list = new ArrayList<String>();
		for (String name : map.keySet()) {
			list.add(name + ": " + map.get(name));
		}

		return StringUtil.join("<br/>", list);
	}

	/**
	 * 添加发邮件用户的邮件地址
	 * 
	 * @param objs
	 * @return
	 */
	public Mail add(String... mails) {

		if (mails != null) {
			for (String mail : mails) {
				this.set.add(mail);
			}
		}

		return this;
	}

	/**
	 * 添加发邮件用户
	 * 
	 * @param users
	 * @return
	 */
	public Mail addUsers(User... users) {

		if (users != null) {
			for (User user : users) {
				if (user.isEnabled()) {
					this.add(user.getMails());
				}
			}
		}

		return this;
	}
	
	/**
	 * 添加发邮件用户
	 * 
	 * @param users
	 * @return
	 */
	public Mail addUsers(Collection<User> users) {
		
		if (users != null) {
			for (User user : users) {
				if (user.isEnabled()) {
					this.add(user.getMails());
				}
			}
		}
		
		return this;
	}

	public Mail addWatchers(Translate translate) {

		// 项目关注者
		this.addWatchers(translate.getProject());

		// 翻译关注者
		Set<User> watchers2 = translate.getWatchers();
		Set<String> watcherMails2 = watchers2.stream().map((user) -> {
			return user.isEnabled() ? user.getMails() : StringUtil.EMPTY;
		}).collect(Collectors.toSet());

		this.addAll(watcherMails2);

		return this;
	}

	public Mail addWatchers(Project project) {

		// 项目关注者
		Set<User> watchers = project.getWatchers();
		Set<String> watcherMails = watchers.stream().map((user) -> {
			return user.isEnabled() ? user.getMails() : StringUtil.EMPTY;
		}).collect(Collectors.toSet());

		this.addAll(watcherMails);

		return this;
	}

	public Mail addAll(Collection<String> mails) {

		if (mails != null) {
			for (String mail : mails) {
				this.add(mail);
			}
		}

		return this;
	}

	public Mail removeUsers(User... users) {

		for (User user : users) {
			this.remove(user.getMails());
		}

		return this;
	}

	public Mail remove(String... mails) {

		for (String mail : mails) {
			this.set.remove(mail);
		}

		return this;
	}

	public String[] get() {

		this.remove(StringUtil.EMPTY);

		String[] mails = this.set.toArray(new String[0]);

		logger.info("发送邮件对象: {}", StringUtil.join(",", mails));

		return mails;
	}

	public boolean isEmpty() {
		this.remove(StringUtil.EMPTY);
		return this.set.size() == 0;
	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId,
			List<Translate> translates) {

		for (Translate translate2 : translates) {
			this.addHref(name, baseURL, translateAction, projectId, translate2);
		}

	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId, String search) {

		String href = baseURL + translateAction + "?projectId=" + projectId
				+ "&search=" + search;
		this.mapHref.put(name,
				StringUtil.replaceByKV(
						"<a target=\"_blank\" href=\"{href}\">{text}</a>",
						"href", href, "text", href));

	}

	public void addHref(String name, String baseURL, String translateAction,
			Long projectId,
			Translate translate) {

		String href = baseURL + translateAction + "?projectId=" + projectId
				+ "&id=" + translate.getId();
		this.mapHref.put(name, StringUtil.replaceByKV(
				"<a target=\"_blank\" href=\"{href}\">{text}</a>",
				"href", href, "text", href));
	}

	public String hrefs() {

		StringBuilder sb = new StringBuilder();
		for (String name : mapHref.keySet()) {
			sb.append("<b>").append(name).append(": </b>")
					.append(mapHref.get(name)).append("<br/>");
		}

		return sb.toString();
	}

}
