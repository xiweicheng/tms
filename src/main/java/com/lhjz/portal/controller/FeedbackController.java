/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Feedback;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.FeedbackForm;
import com.lhjz.portal.repository.FeedbackRepository;
import com.lhjz.portal.util.CommonUtil;
import com.lhjz.portal.util.DateUtil;
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
@RequestMapping("admin/feedback")
public class FeedbackController extends BaseController {

	static final Logger logger = LoggerFactory
			.getLogger(FeedbackController.class);

	@Autowired
	FeedbackRepository feedbackRepository;

	@Autowired
	MailSender2 mailSender;

	@Value("${lhjz.mail.to.addresses}")
	private String toAddrArr;

	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	public RespBody save(@Valid FeedbackForm feedbackForm,
			BindingResult bindingResult, @RequestParam("baseURL") String baseURL) {

		logger.debug("Enter method: {}", "save");

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream()
					.map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		List<Feedback> feedbacks = feedbackRepository.findByContentAndUsername(
				feedbackForm.getContent(), WebUtil.getUsername());

		if (feedbacks.size() > 0) {
			return RespBody.failed("您已经反馈过该内容！");
		}

		Feedback feedback = new Feedback();
		feedback.setContent(feedbackForm.getContent());
		feedback.setCreateDate(new Date());
		feedback.setMail(feedbackForm.getMail());
		feedback.setName(feedbackForm.getName());
		feedback.setPhone(feedbackForm.getPhone());
		feedback.setUsername(WebUtil.getUsername());
		feedback.setUrl(feedbackForm.getUrl());

		final Feedback feedback2 = feedbackRepository.saveAndFlush(feedback);

		log(Action.Create, Target.Feedback, feedback2.getId());

		final User loginUser = getLoginUser();

		final String href = baseURL;

		feedback2.setContent(CommonUtil.replaceLinebreak(feedback2.getContent()));

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-用户反馈_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/feedback",
							MapUtil.objArr2Map("feedback", feedback2, "user", loginUser, "href", href)),
					StringUtil.split(toAddrArr, ","));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed("反馈提交成功，谢谢！");
	}
}
