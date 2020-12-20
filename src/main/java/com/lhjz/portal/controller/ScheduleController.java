/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Schedule;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.model.SchedulePayload;
import com.lhjz.portal.model.SchedulePayload.Cmd;
import com.lhjz.portal.pojo.Enum.SchedulePriority;
import com.lhjz.portal.pojo.Enum.ScheduleType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.ScheduleForm;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ScheduleRepository;
import com.lhjz.portal.repository.UserRepository;
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
@RequestMapping("admin/schedule")
public class ScheduleController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	@Autowired
	ScheduleRepository scheduleRepository;

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender mailSender;

	@Lazy
	@Autowired
	SimpMessagingTemplate messagingTemplate;

	private void wsSend(String username, Schedule schedule, Cmd cmd) {
		try {
			messagingTemplate.convertAndSendToUser(username, "/channel/schedule", SchedulePayload.builder().cmd(cmd)
					.id(schedule.getId()).title(schedule.getTitle()).creator(WebUtil.getUsername()).build());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("basePath") final String basePath, @Valid ScheduleForm scheduleForm,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		Schedule schedule = new Schedule();
		schedule.setTitle(scheduleForm.getTitle());
		schedule.setDescription(scheduleForm.getDescription());
		schedule.setPlace(scheduleForm.getPlace());

		if (scheduleForm.getChannelId() != null) {
			schedule.setChannel(channelRepository.findOne(scheduleForm.getChannelId()));
		}

		schedule.setStartDate(scheduleForm.getStartDate());
		schedule.setEndDate(scheduleForm.getEndDate());

		if (StringUtil.isNotEmpty(scheduleForm.getPriority())) {
			schedule.setPriority(SchedulePriority.valueOf(scheduleForm.getPriority()));
		}

		schedule.setPrivated(scheduleForm.getPrivated());
		schedule.setRemind(scheduleForm.getRemind());

		if (StringUtil.isNotEmpty(scheduleForm.getType())) {
			schedule.setType(ScheduleType.valueOf(scheduleForm.getType()));
		}

		Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);

		final Mail mail = Mail.instance();
		String[] actors = scheduleForm.getActors().split(",");
		List<String> names = new ArrayList<>();
		Stream.of(actors).forEach((actor) -> {
			User user = userRepository.findOne(actor);
			user.getActSchedules().add(schedule2);

			User user2 = userRepository.saveAndFlush(user);
			schedule2.getActors().add(user2);

			// socket推送通知
			wsSend(actor, schedule2, Cmd.C);

			mail.addUsers(user);
			names.add(StringUtil.isNotEmpty(user2.getName()) ? user2.getName() : user2.getUsername());
		});

		final User loginUser = getLoginUser();
		final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule.getStartDate(), DateUtil.FORMAT1) + " - "
				+ DateUtil.format(schedule.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
				+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>" + StringUtil.nl2br(schedule.getTitle())
				+ "</p>";

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-日程创建消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic",
									MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath, "title",
											"下面创建的日程的参与者中有你", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(schedule2);
	}

	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy() {

		User loginUser = getLoginUser();
		Set<Schedule> schedules = loginUser.getActSchedules().stream().filter((item) -> {
			return !item.getStatus().equals(Status.Deleted);
		}).collect(Collectors.toSet());

		return RespBody.succeed(schedules);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		Schedule schedule = scheduleRepository.findOne(id);

		return RespBody.succeed(schedule);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "priority", required = false) String priority,
			@RequestParam(value = "privated", required = false) Boolean privated) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		boolean updated = false;

		if (StringUtil.isNotEmpty(title) && !title.equals(schedule.getTitle())) {
			schedule.setTitle(title);
			updated = true;
		}

		if (desc != null && !desc.equals(schedule.getDescription())) {
			schedule.setDescription(desc);
			updated = true;
		}

		if (place != null && !place.equals(schedule.getPlace())) {
			schedule.setPlace(place);
			updated = true;
		}

		if (StringUtil.isNotEmpty(priority) && !priority.equals(schedule.getPriority().name())) {
			schedule.setPriority(SchedulePriority.valueOf(priority));
			updated = true;
		}

		if (privated != null && !privated.equals(schedule.getPrivated())) {
			schedule.setPrivated(privated);
			updated = true;
		}

		if (updated) {
			Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);
			final Mail mail = Mail.instance();
			List<String> names = new ArrayList<>();
			schedule2.getActors().stream().forEach((actor) -> {
				mail.addUsers(actor);
				names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());

				// socket推送通知
				wsSend(actor.getUsername(), schedule2, Cmd.U);
			});

			final User loginUser = getLoginUser();
			final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule2.getStartDate(), DateUtil.FORMAT1) + " - "
					+ DateUtil.format(schedule2.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
					+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>"
					+ StringUtil.nl2br(schedule2.getTitle()) + "</p>";

			try {
				mailSender
						.sendHtmlByQueue(String.format("TMS-日程更新消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath,
												"title", "下面你参与的日程有更新", "content", html)),
								getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return RespBody.succeed(schedule2);
		}
		return RespBody.failed("无变更内容!");
	}

	@RequestMapping(value = "update2", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update2(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "endDate", required = false) Date endDate) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		boolean updated = false;

		if (StringUtil.isNotEmpty(title) && !title.equals(schedule.getTitle())) {
			schedule.setTitle(title);
			updated = true;
		}

		if (!isDateEql(schedule.getStartDate(), startDate)) {
			schedule.setStartDate(startDate);
			updated = true;
		}

		if (!isDateEql(schedule.getEndDate(), endDate)) {
			schedule.setEndDate(endDate);
			updated = true;
		}

		if (updated) {
			Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);
			final Mail mail = Mail.instance();
			List<String> names = new ArrayList<>();
			schedule2.getActors().stream().forEach((actor) -> {
				mail.addUsers(actor);
				names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());

				// socket推送通知
				wsSend(actor.getUsername(), schedule2, Cmd.U);
			});

			final User loginUser = getLoginUser();
			final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule2.getStartDate(), DateUtil.FORMAT1) + " - "
					+ DateUtil.format(schedule2.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
					+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>"
					+ StringUtil.nl2br(schedule2.getTitle()) + "</p>";

			try {
				mailSender
						.sendHtmlByQueue(String.format("TMS-日程更新消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath,
												"title", "下面你参与的日程有更新", "content", html)),
								getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return RespBody.succeed(schedule2);
		}

		return RespBody.failed("无变更内容!");
	}

	private boolean isDateEql(Date d1, Date d2) {

		if (d1 == null) {
			return d1 == d2;
		} else {
			return d1.equals(d2);
		}
	}

	@RequestMapping(value = "updateStartEndDate", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateStartEndDate(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "endDate", required = false) Date endDate) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		boolean updated = false;

		if (!isDateEql(schedule.getStartDate(), startDate)) {
			schedule.setStartDate(startDate);
			updated = true;
		}

		if (!isDateEql(schedule.getEndDate(), endDate)) {
			schedule.setEndDate(endDate);
			updated = true;
		}

		if (updated) {
			Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);
			final Mail mail = Mail.instance();
			List<String> names = new ArrayList<>();
			schedule2.getActors().stream().forEach((actor) -> {
				mail.addUsers(actor);
				names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());

				// socket推送通知
				wsSend(actor.getUsername(), schedule2, Cmd.U);
			});

			final User loginUser = getLoginUser();
			final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule2.getStartDate(), DateUtil.FORMAT1) + " - "
					+ DateUtil.format(schedule2.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
					+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>"
					+ StringUtil.nl2br(schedule2.getTitle()) + "</p>";

			try {
				mailSender
						.sendHtmlByQueue(String.format("TMS-日程更新消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath,
												"title", "下面你参与的日程有更新", "content", html)),
								getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return RespBody.succeed(schedule2);
		}
		return RespBody.failed("无变更内容!");
	}

	@RequestMapping(value = "updateRemind", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateRemind(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam(value = "remind", required = false) Long remind) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		schedule.setRemind(remind);

		Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);

		schedule2.getActors().stream().forEach((actor) -> {

			// socket推送通知
			wsSend(actor.getUsername(), schedule2, Cmd.U);
		});

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule2);
	}

	@RequestMapping(value = "addActors", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addActors(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam("actors") String actors) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		List<User> users = new ArrayList<>();
		schedule.getActors().stream().forEach((actor) -> {
			users.add(actor);
		});

		final Mail mail = Mail.instance();
		Stream.of(actors.split(",")).forEach((actor) -> {
			User user = userRepository.findOne(actor);

			user.getActSchedules().add(schedule);

			User user2 = userRepository.saveAndFlush(user);

			schedule.getActors().add(user2);
			mail.addUsers(user);

			// socket推送通知
			wsSend(actor, schedule, Cmd.C);

		});

		users.stream().forEach((actor) -> {
			// socket推送通知
			wsSend(actor.getUsername(), schedule, Cmd.U);
		});

		List<String> names = new ArrayList<>();
		schedule.getActors().stream().forEach((actor) -> {
			names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());
		});

		final User loginUser = getLoginUser();
		final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule.getStartDate(), DateUtil.FORMAT1) + " - "
				+ DateUtil.format(schedule.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
				+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>" + StringUtil.nl2br(schedule.getTitle())
				+ "</p>";

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-日程添加参与者消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic",
									MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath, "title",
											"下面日程的参与者中加入了你", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(schedule);
	}

	@RequestMapping(value = "removeActors", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeActors(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id,
			@RequestParam("actors") String actors) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		final Mail mail = Mail.instance();
		Stream.of(actors.split(",")).forEach((actor) -> {
			User user = userRepository.findOne(actor);

			user.getActSchedules().remove(schedule);

			User user2 = userRepository.saveAndFlush(user);

			schedule.getActors().remove(user2);
			mail.addUsers(user);

			// socket推送通知
			wsSend(actor, schedule, Cmd.D);
		});

		List<String> names = new ArrayList<>();
		schedule.getActors().stream().forEach((actor) -> {
			names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());

			// socket推送通知
			wsSend(actor.getUsername(), schedule, Cmd.U);
		});

		final User loginUser = getLoginUser();
		final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule.getStartDate(), DateUtil.FORMAT1) + " - "
				+ DateUtil.format(schedule.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
				+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>" + StringUtil.nl2br(schedule.getTitle())
				+ "</p>";

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-日程移除参与者消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic",
									MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", basePath, "title",
											"下面日程将你从参与者中移除", "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(schedule);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("basePath") final String basePath, @RequestParam("id") Long id) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该计划!");
		}

		schedule.setStatus(Status.Deleted);

		scheduleRepository.saveAndFlush(schedule);

		final Mail mail = Mail.instance();
		List<String> names = new ArrayList<>();
		schedule.getActors().stream().forEach((actor) -> {
			mail.addUsers(actor);
			names.add(StringUtil.isNotEmpty(actor.getName()) ? actor.getName() : actor.getUsername());

			// socket推送通知
			wsSend(actor.getUsername(), schedule, Cmd.D);
		});

		final User loginUser = getLoginUser();
		final String html = "<p><b>起止时间:</b> " + DateUtil.format(schedule.getStartDate(), DateUtil.FORMAT1) + " - "
				+ DateUtil.format(schedule.getEndDate(), DateUtil.FORMAT1) + "<hr/>" + "<b>参与者:</b> "
				+ StringUtil.join(",", names) + "<hr/>" + "<b>日程内容:</b> <br/>" + StringUtil.nl2br(schedule.getTitle())
				+ "</p>";

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-日程删除消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser, "date",
							new Date(), "href", basePath, "title", "下面日程被取消并且删除", "content", html)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(id);
	}

}
