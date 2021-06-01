/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Language;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.entity.TranslateItemHistory;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.LanguageRepository;
import com.lhjz.portal.repository.ProjectRepository;
import com.lhjz.portal.repository.TranslateItemHistoryRepository;
import com.lhjz.portal.repository.TranslateItemRepository;
import com.lhjz.portal.repository.TranslateRepository;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 *
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/import")
public class ImportController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ImportController.class);

	@Autowired
	TranslateRepository translateRepository;

	@Autowired
	TranslateItemRepository translateItemRepository;

	@Autowired
	TranslateItemHistoryRepository translateItemHistoryRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	LanguageRepository languageRepository;

	@Autowired
	LabelRepository labelRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	MailSender mailSender;

	String translateAction = "admin/translate";

	private void joinKV(JsonObject jsonObject, String key,
			Map<String, String> kvMaps) {
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String k = StringUtil.isEmpty(key) ? entry.getKey()
					: StringUtil.join2(".", key, entry.getKey());
			JsonElement jsonE = entry.getValue();
			if (jsonE.isJsonPrimitive()) {
				kvMaps.put(k, jsonE.getAsString());
			} else if (jsonE.isJsonObject()) {
				joinKV((JsonObject) jsonE, k, kvMaps);
			}
		}
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN", "ROLE_USER" })
	public RespBody save(@RequestParam("projectId") Long projectId,
			@RequestParam("languageId") Long languageId,
			@RequestParam("type") Long type,
			@RequestParam("baseURL") String baseURL,
			@RequestParam(value = "labels", required = false) String labels,
			@RequestParam(value = "observers", required = false) String observers,
			@RequestParam("content") String content) {

		final Project project = projectRepository.findOne(projectId);
		Language language2 = languageRepository.findOne(languageId);

		Map<String, String> kvMaps = new HashMap<>();
		if (type == 1) {// JSON
			joinKV((JsonObject) JsonUtil.toJsonElement(content), "", kvMaps);
		} else { // Property
			Properties properties = new Properties();
			try {
				properties.load(new StringReader(content));
			} catch (IOException e) {
				e.printStackTrace();
				return RespBody.failed(e.getMessage());
			}
			Set<Object> keySet = properties.keySet();
			for (Object k : keySet) {
				kvMaps.put(String.valueOf(k), properties
						.getProperty(String.valueOf(k), StringUtil.EMPTY));
			}
		}

		List<TranslateItem> translateItems2 = new ArrayList<TranslateItem>();
		List<Translate> translates2 = new ArrayList<Translate>();
		Set<Translate> translates3 = new HashSet<Translate>();

		for (String key : kvMaps.keySet()) {
			List<Translate> translates = translateRepository
					.findByKeyAndProject(key, project);
			// 翻译存在
			if (translates.size() > 0) { // 已经存在,做更新处理
				Translate translate2 = translates.get(0);
				Set<TranslateItem> translateItems = translate2
						.getTranslateItems();
				Language language = null;

				// 查找是否翻译语言已经存在,是否需要更新
				for (TranslateItem translateItem : translateItems) {
					// 存在导入语言翻译
					if (translateItem.getLanguage().getId()
							.equals(languageId)) {
						language = translateItem.getLanguage();

						String oldContent = translateItem.getContent();
						// 翻译内容变动(如果更新翻译内容为空,则不进行更新)
						if (StringUtil.isNotEmpty(kvMaps.get(key))
								&& !kvMaps.get(key).equals(oldContent)) {

							TranslateItemHistory translateItemHistory = new TranslateItemHistory();
							translateItemHistory.setCreateDate(new Date());
							translateItemHistory.setCreator(WebUtil
									.getUsername());
							translateItemHistory.setItemContent(oldContent);
							translateItemHistory
									.setItemCreateDate(translateItem
											.getUpdateDate() != null ? translateItem
											.getUpdateDate() : translateItem
											.getCreateDate());
							translateItemHistory.setItemCreator(translateItem
									.getUpdater() != null ? translateItem
									.getUpdater() : translateItem.getCreator());
							translateItemHistory
									.setTranslateItem(translateItem);

							translateItemHistoryRepository
									.saveAndFlush(translateItemHistory);

							translateItem.setContent(kvMaps.get(key));
							translateItem.setUpdateDate(new Date());
							translateItem.setUpdater(WebUtil.getUsername());
							translateItem.setStatus(Status.Updated);

							translateItems2.add(translateItem);

							log(Action.Update, Target.Translate,
									translate2.getId(), kvMaps.get(key),
									oldContent);

							translates3.add(translate2);
						}
					}
				}

				// 翻译语言不存在
				if (language == null) {
					TranslateItem translateItem = new TranslateItem();
					translateItem.setContent(kvMaps.get(key));
					translateItem.setCreateDate(new Date());
					translateItem.setCreator(WebUtil.getUsername());
					translateItem.setLanguage(language2);
					translateItem.setStatus(Status.New);
					translateItem.setTranslate(translate2);

					translate2.getTranslateItems().add(translateItem);

					translateItems2.add(translateItem);

					log(Action.Update, Target.Translate, translate2.getId(),
							kvMaps.get(key));

					translates3.add(translate2);
				}

			} else { // 不存在, 做新建处理
				Translate translate = new Translate();
				translate.setCreateDate(new Date());
				translate.setCreator(WebUtil.getUsername());
				translate.setDescription(kvMaps.get(key));
				translate.setKey(key);
				translate.setProject(project);
				translate.setStatus(Status.New);
				Set<TranslateItem> translateItems = translate
						.getTranslateItems();

				Set<Language> languages = project.getLanguages();

				for (Language language : languages) {
					TranslateItem item = new TranslateItem();
					if (language.getId().equals(languageId)) {
						item.setContent(kvMaps.get(key));
					} else {
						item.setContent(StringUtil.EMPTY);
					}
					item.setCreateDate(new Date());
					item.setCreator(WebUtil.getUsername());
					item.setLanguage(language);
					item.setStatus(Status.New);
					item.setTranslate(translate);

					translateItems.add(item);
				}

				translates2.add(translate);
			}
		}

		translateItemRepository.save(translateItems2);
		translateItemRepository.flush();

		// 更新翻译标签
		String updateLabel = "U"
				+ DateUtil.format(new Date(), DateUtil.FORMAT8);
		List<Label> lblUpdated = new ArrayList<>();
		// 更新翻译search设置属性
		for (Translate translate : translates3) {

			translate.setUpdateDate(new Date());
			translate.setUpdater(WebUtil.getUsername());

			Label label = new Label();
			label.setCreateDate(new Date());
			label.setCreator(WebUtil.getUsername());
			label.setName(updateLabel);
			label.setStatus(Status.New);
			label.setTranslate(translate);

			lblUpdated.add(label);

			translate.getLabels().add(label);

			translate.setSearch(translate.toString());
		}

		Mail mail2 = Mail.instance();

		if (lblUpdated.size() > 0) {
			labelRepository.save(lblUpdated);
			labelRepository.flush();

			mail2.addHref("更新", baseURL, translateAction, projectId,
					updateLabel);
		}

		translateRepository.save(translates3);
		translateRepository.flush();

		// 只有新建的会打标签
		String[] lbls = new String[0];
		if (StringUtil.isNotEmpty(labels)) {
			lbls = labels.split(",");
		}

		List<Translate> translates = translateRepository.save(translates2);
		translateRepository.flush();

		// 新建的翻译打标签
		String newLabel = "N" + DateUtil.format(new Date(), DateUtil.FORMAT8);
		List<Label> lblNew = new ArrayList<>();
		// 新建的翻译设置search属性
		for (Translate translate : translates) {

			Label label = new Label();
			label.setCreateDate(new Date());
			label.setCreator(WebUtil.getUsername());
			label.setName(newLabel);
			label.setStatus(Status.New);
			label.setTranslate(translate);

			lblNew.add(label);

			translate.getLabels().add(label);

			Set<Label> labels2 = new HashSet<>();
			for (String lbl : lbls) {
				Label label2 = new Label();
				label2.setCreateDate(new Date());
				label2.setCreator(WebUtil.getUsername());
				label2.setName(lbl);
				label2.setStatus(Status.New);
				label2.setTranslate(translate);

				labels2.add(label2);
			}
			labelRepository.save(labels2);
			labelRepository.flush();

			translate.getLabels().addAll(labels2);

			translate.setSearch(translate.toString());

			log(Action.Create, Target.Translate, translate.getId());
		}

		translateRepository.save(translates2);
		translateRepository.flush();

		if (lblNew.size() > 0) {
			labelRepository.save(lblNew);
			labelRepository.flush();

			mail2.addHref("新增", baseURL, translateAction, projectId, newLabel);
		}

		final Mail mail = Mail.instance();

		final User loginUser = getLoginUser();

		final String href = baseURL + translateAction + "?projectId="
				+ projectId;

		final String msg = "<h3>" + "新增/更新/合计: " + lblNew.size() + " / "
				+ lblUpdated.size() + " / " + kvMaps.size() + "</h3><hr/>"
				+ mail2.hrefs();

		if (StringUtil.isNotEmpty(observers)) {
			Stream.of(observers.split(",")).forEach((observer) -> {
				mail.addUsers(getUser(observer)).addUsers(getLoginUser());
			});
		}

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-翻译导入_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/translate-import", MapUtil.objArr2Map("user", loginUser,
							"project", project, "importDate", new Date(), "href", href, "body", msg)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(msg);
	}

	@RequestMapping(value = "export", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN", "ROLE_USER" })
	public RespBody export(@RequestParam("projectId") Long projectId,
			@RequestParam("languageId") Long languageId,
			@RequestParam("type") Long type) {

		Project project = projectRepository.findOne(projectId);
		Set<Translate> translates = project.getTranslates();

		Map<String, String> map = new HashMap<String, String>();

		for (Translate translate : translates) {
			Set<TranslateItem> translateItems = translate.getTranslateItems();
			for (TranslateItem translateItem : translateItems) {
				if (translateItem.getLanguage().getId().equals(languageId)) {
					map.put(translate.getKey(), translateItem.getContent());
				}
			}
		}

		if (type == 1) {
			JsonObject root = new JsonObject();
			for (String k : map.keySet()) {
				String[] keys = k.split("\\.");
				JsonObject lastE = root;
				for (int i = 0; i < keys.length; i++) {
					if (i < keys.length - 1) {
						if (!lastE.has(keys[i])) {
							lastE.add(keys[i], new JsonObject());
						}

						lastE = (JsonObject) lastE.get(keys[i]);
					} else {
						lastE.addProperty(keys[i], map.get(k));
					}
				}
			}

			String data = JsonUtil.toJson(root);

			return RespBody.succeed(data).addMsg(map.size());
		} else {
			List<String> list = new ArrayList<String>();
			for (String k : map.keySet()) {
				String v = map.get(k);
				v = StringUtil.join("\\\n",
						(StringUtil.isNotEmpty(v) ? v : StringUtil.EMPTY)
								.split("\n"));
				list.add(k + "=" + v);
			}

			Collections.sort(list);

			String data = StringUtil.join("\r\n", list);

			return RespBody.succeed(data).addMsg(map.size());
		}
	}
}
