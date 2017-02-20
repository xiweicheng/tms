/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Schedule;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.SchedulePriority;
import com.lhjz.portal.pojo.Enum.ScheduleType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.ScheduleForm;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ScheduleRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;

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
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@Valid ScheduleForm scheduleForm, BindingResult bindingResult) {

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

		String[] actors = scheduleForm.getActors().split(",");
		Stream.of(actors).forEach((actor) -> {
			User user = userRepository.findOne(actor);
			user.getActSchedules().add(schedule2);

			User user2 = userRepository.saveAndFlush(user);
			schedule2.getActors().add(user2);
		});

		// TODO 向参与者邮件提醒

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
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "endDate", required = false) Date endDate,
			@RequestParam(value = "priority", required = false) String priority,
			@RequestParam(value = "remind", required = false) Long remind,
			@RequestParam(value = "privated", required = false) Boolean privated) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		if (StringUtil.isNotEmpty(title)) {
			schedule.setTitle(title);
		}

		if (desc != null) {
			schedule.setDescription(desc);
		}

		if (place != null) {
			schedule.setPlace(place);
		}

		if (StringUtil.isNotEmpty(priority)) {
			schedule.setPriority(SchedulePriority.valueOf(priority));
		}

		if (privated != null) {
			schedule.setPrivated(privated);
		}

		Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule2);
	}

	@RequestMapping(value = "updateStartEndDate", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateStartEndDate(@RequestParam("id") Long id,
			@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "endDate", required = false) Date endDate) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		schedule.setStartDate(startDate);
		schedule.setEndDate(endDate);

		Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule2);
	}

	@RequestMapping(value = "updateRemind", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateRemind(@RequestParam("id") Long id,
			@RequestParam(value = "remind", required = false) Long remind) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		schedule.setRemind(remind);

		Schedule schedule2 = scheduleRepository.saveAndFlush(schedule);

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule2);
	}

	@RequestMapping(value = "addActors", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addActors(@RequestParam("id") Long id, @RequestParam("actors") String actors) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		Stream.of(actors.split(",")).forEach((actor) -> {
			User user = userRepository.findOne(actor);

			user.getActSchedules().add(schedule);

			User user2 = userRepository.saveAndFlush(user);

			schedule.getActors().add(user2);
		});

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule);
	}

	@RequestMapping(value = "removeActors", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeActors(@RequestParam("id") Long id, @RequestParam("actors") String actors) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该计划!");
		}

		Stream.of(actors.split(",")).forEach((actor) -> {
			User user = userRepository.findOne(actor);

			user.getActSchedules().remove(schedule);

			User user2 = userRepository.saveAndFlush(user);

			schedule.getActors().remove(user2);
		});

		// TODO 向参与者邮件提醒

		return RespBody.succeed(schedule);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Schedule schedule = scheduleRepository.findOne(id);

		if (!isSuperOrCreator(schedule.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该计划!");
		}

		schedule.setStatus(Status.Deleted);

		scheduleRepository.saveAndFlush(schedule);

		// TODO 向参与者邮件提醒

		return RespBody.succeed(id);
	}

}
