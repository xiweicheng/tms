/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.entity.Language;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.LanguageForm;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.LanguageRepository;
import com.lhjz.portal.repository.ProjectRepository;
import com.lhjz.portal.repository.TranslateItemRepository;
import com.lhjz.portal.repository.TranslateRepository;
import com.lhjz.portal.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/language")
public class LanguageController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(LanguageController.class);

	@Autowired
	TranslateRepository translateRepository;

	@Autowired
	TranslateItemRepository translateItemRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	LanguageRepository languageRepository;

	@Autowired
	LabelRepository labelRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody create(@Valid LanguageForm languageForm,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream()
					.map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		if (languageRepository.findOneByName(languageForm.getName()) != null) {
			return RespBody.failed("添加语言已经存在!");
		}

		Language language = new Language();
		language.setCreateDate(new Date());
		language.setCreator(WebUtil.getUsername());
		language.setDescription(languageForm.getDesc());
		language.setName(languageForm.getName());
		language.setStatus(Status.New);

		Language language2 = languageRepository.saveAndFlush(language);

		log(Action.Create, Target.Language, language2.getId());

		return RespBody.succeed(language2);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody update(@RequestParam("id") Long id,
			@Valid LanguageForm languageForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream()
					.map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		Language language3 = languageRepository
				.findOneByName(languageForm.getName());

		Language language = languageRepository.findOne(id);
		language.setDescription(languageForm.getDesc());

		String msg = null;
		if (language3 != null) {
			msg = "修改语言名称已经存在!不能修改该属性,可以修改语言其它属性!";
		} else {
			language.setName(languageForm.getName());
		}

		language.setStatus(Status.Updated);

		Language language2 = languageRepository.saveAndFlush(language);

		log(Action.Update, Target.Language, language2.getId());

		return RespBody.succeed(language2).addMsg(msg);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody delete(@RequestParam("id") Long id) {

		if (WebUtil.isRememberMeAuthenticated()) {
			return RespBody.failed("因为当前是通过[记住我]登录,为了安全需要,请退出重新登录再尝试删除语言!");
		}

		Language language = languageRepository.findOne(id);

		if (language == null) {
			return RespBody.failed("删除语言不存在！");
		}

		Set<Project> projects = language.getProjects();
		if (projects.size() > 0) {
			return RespBody.failed("该语言已经绑定到项目,无法删除！建议解除项目语言绑定后再尝试!");
		}

		Set<TranslateItem> translateItems = language.getTranslateItems();
		if (translateItems.size() > 0) {
			return RespBody.failed("该语言已经绑定到翻译条目,无法删除！建议解除翻译条目语言绑定后再尝试!");
		}

		languageRepository.delete(language);

		log(Action.Delete, Target.Language, language.getId(), language);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody get(@RequestParam("id") Long id) {

		Language language = languageRepository.findOne(id);

		if (language == null) {
			return RespBody.failed("获取语言不存在！");
		}

		log(Action.Read, Target.Language, id);

		return RespBody.succeed(language);
	}

}
