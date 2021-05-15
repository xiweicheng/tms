/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import com.google.common.collect.Lists;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Todo;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TodoPriority;
import com.lhjz.portal.pojo.TodoSortForm;
import com.lhjz.portal.pojo.TodoSortItem;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TodoRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author xi
 * @date 2015年3月28日 下午1:19:05
 */
@Controller
@RequestMapping("admin/todo")
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
                           @RequestParam(value = "sortIndex", required = false) Long sortIndex,
                           @RequestParam(value = "content", required = false) String content) {

        if (StringUtil.isEmpty(title)) {
            return RespBody.failed("标题不能为空!");
        }

        Todo todo = Todo.builder().title(title).content(content).sortIndex(sortIndex).build();

        Todo todo2 = todoRepository.saveAndFlush(todo);

        return RespBody.succeed(todo2);
    }

    @RequestMapping(value = "listMy", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMy(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

        List<Todo> todos = todoRepository.findByStatusNotAndCreator(Status.Deleted, getLoginUser(), sort);

        return RespBody.succeed(todos);
    }

    @RequestMapping(value = "listMy/done", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMyDone(@PageableDefault(sort = {"id"}, direction = Direction.DESC) Pageable pageable,
                               @RequestParam(value = "search", required = false) String search) {

        Page<Todo> page;

        if (StringUtils.isBlank(search)) {
            page = todoRepository.findByStatusAndCreator(Status.Done, getLoginUser(), pageable);
        } else {
            page = todoRepository.findByStatusAndCreatorAndTitleContainingIgnoreCaseOrStatusAndCreatorAndContentContainingIgnoreCase(
                    Status.Done, getLoginUser(), search, Status.Done, getLoginUser(), search, pageable);
        }

        return RespBody.succeed(page);
    }

    @RequestMapping(value = "listMy/undone", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMyUnDone(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

        List<Todo> todos = todoRepository.findByStatusInAndCreator(Lists.newArrayList(Status.New, Status.Doing),
                getLoginUser(), sort);

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

    @PostMapping("sort")
    @ResponseBody
    public RespBody sort(TodoSortForm todoSortForm) {

        if (todoSortForm == null || StringUtil.isEmpty(todoSortForm.getItems())) {
            return RespBody.failed("参数错误！");
        }

        TodoSortItem[] sortItems = JsonUtil.json2Object(todoSortForm.getItems(), TodoSortItem[].class);

        Stream.of(sortItems).forEach(item -> {
            Todo todo = todoRepository.findOne(item.getId());
            if (item.getSort() != null & !item.getSort().equals(todo.getSortIndex())) {
                todo.setSortIndex(item.getSort());
                todoRepository.saveAndFlush(todo);
            }
        });

        return RespBody.succeed();
    }

}
