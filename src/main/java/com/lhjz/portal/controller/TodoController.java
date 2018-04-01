/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Todo;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TodoPriority;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TodoRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/todo")
@Slf4j
public class TodoController extends BaseController {

	@Autowired
	TodoRepository todoRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("title") String title,
			@RequestParam(value = "content", required = false) String content) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题不能为空!");
		}

		Todo todo = Todo.builder().title(title).content(content).build();

		log.debug("创建TODO: {}", todo);

		Todo todo2 = todoRepository.saveAndFlush(todo);

		return RespBody.succeed(todo2);
	}

	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		List<Todo> todos = todoRepository.findByStatusNotAndCreator(Status.Deleted, getLoginUser(), sort);

		return RespBody.succeed(todos);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "sortIndex", required = false) Long sortIndex,
			@RequestParam(value = "priority", required = false) TodoPriority priority,
			@RequestParam(value = "status", required = false) Status status,
			@RequestParam(value = "content", required = false) String content) {

		Todo todo = todoRepository.findOne(id);

		if (!isSuperOrCreator(todo.getCreator())) {
			return RespBody.failed("权限不足！");
		}

		boolean needUpdate = false;

		if (StringUtil.isNotEmpty(title)) {
			todo.setTitle(title);
			needUpdate = true;
		}

		if (StringUtil.isNotEmpty(content)) {
			todo.setContent(content);
			needUpdate = true;
		}

		if (sortIndex != null) {
			todo.setSortIndex(sortIndex);
			needUpdate = true;
		}

		if (priority != null) {
			todo.setPriority(priority);
			needUpdate = true;
		}

		if (status != null) {
			todo.setStatus(status);
			needUpdate = true;
		}

		if (!needUpdate) {
			return RespBody.failed("没有更新内容！");
		}

		Todo todo2 = todoRepository.saveAndFlush(todo);

		return RespBody.succeed(todo2);

	}

	@RequestMapping(value = "delete/{id}", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@PathVariable("id") Long id) {

		Todo todo = todoRepository.findOne(id);

		if (!isSuperOrCreator(todo.getCreator())) {
			return RespBody.failed("权限不足！");
		}

		todo.setStatus(Status.Deleted);

		todoRepository.saveAndFlush(todo);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get/{id}", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@PathVariable("id") Long id) {

		Todo todo = todoRepository.findOne(id);

		if (!isSuperOrCreator(todo.getCreator())) {
			return RespBody.failed("权限不足！");
		}

		return RespBody.succeed(todo);
	}

}
