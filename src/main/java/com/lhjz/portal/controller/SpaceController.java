/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.SpaceAuthority;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.SpaceAuthorityRepository;
import com.lhjz.portal.repository.SpaceRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/space")
public class SpaceController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(SpaceController.class);

	@Autowired
	SpaceRepository spaceRepository;
	
	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	SpaceAuthorityRepository spaceAuthorityRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "opened", required = false) Boolean opened,
			@RequestParam(value = "privated", required = false) Boolean privated) {

		if (StringUtil.isEmpty(name)) {
			return RespBody.failed("名称不能为空!");
		}

		Space space3 = spaceRepository.findOneByNameAndStatusNot(name, Status.Deleted);

		if (space3 != null) {
			return RespBody.failed("同名空间已经存在!");
		}

		Space space = new Space();

		space.setName(name);
		if (desc != null) {
			space.setDescription(desc);
		}
		if (privated != null) {
			space.setPrivated(privated);
		}
		if (opened != null) {
			space.setOpened(opened);
		}

		Space space2 = spaceRepository.saveAndFlush(space);

		return RespBody.succeed(space2);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		Space space = spaceRepository.findOne(id);

		if (!hasAuth(space)) {
			return RespBody.failed("没有权限查看该空间!");
		}

		return RespBody.succeed(space);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "opened", required = false) Boolean opened,
			@RequestParam(value = "privated", required = false) Boolean privated) {

		Space space = spaceRepository.findOne(id);

		if (!isSuperOrCreator(space.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该空间!");
		}

		if (StringUtil.isNotEmpty(name)) {
			space.setName(name);
		}
		if (desc != null) {
			space.setDescription(desc);
		}
		if (privated != null) {
			space.setPrivated(privated);
			if(privated) {
				space.setOpened(false);
			}
		}
		if (opened != null) {
			space.setOpened(opened);
			if(opened) {
				space.setPrivated(false);
			}
		}

		Space space2 = spaceRepository.saveAndFlush(space);

		return RespBody.succeed(space2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Space space = spaceRepository.findOne(id);

		if (!isSuperOrCreator(space.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该空间!");
		}
		
		boolean exist = space.getBlogs().stream().anyMatch(s -> !Status.Deleted.equals(s.getStatus()));
		if (exist) {
			return RespBody.failed("该空间下存在博文,不能删除,请移除博文后再试!");
		}

		space.setStatus(Status.Deleted);

		spaceRepository.saveAndFlush(space);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody list() {

		if (!isSuper()) {
			return RespBody.failed("没有权限获取全部空间列表!");
		}

		List<Space> spaces = spaceRepository.findAll().stream().filter(s -> !s.getStatus().equals(Status.Deleted))
				.collect(Collectors.toList());

		return RespBody.succeed(spaces);
	}
	
	private boolean hasAuth(Space s) {
		
		if (s == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		if (s.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}
		
		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (s.getCreator().equals(loginUser)) { // 我创建的
			return true;
		}
		
		if(Boolean.TRUE.equals(s.getOpened())) {
			return true;
		}

		if (!s.getPrivated()) { // 非私有的
			return true;
		}

		boolean exists = false;
		for (SpaceAuthority sa : s.getSpaceAuthorities()) {
			if (loginUser.equals(sa.getUser())) {
				exists = true;
				break;
			} else {
				Channel channel = sa.getChannel();
				if (channel != null) {
					Set<User> members = channel.getMembers();
					if (members.contains(loginUser)) {
						exists = true;
						break;
					}
				}
			}
		}

		return exists;
	}
	
	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy() {

		List<Space> spaces = spaceRepository.findAll().stream().filter(s -> hasAuth(s)).collect(Collectors.toList());

		return RespBody.succeed(spaces);
	}
	
	@RequestMapping(value = "auth/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getAuth(@RequestParam("id") Long id) {
		Space space = spaceRepository.findOne(id);
		if (!hasAuth(space)) {
			return RespBody.failed("没有权限查看该空间权限!");
		}
		return RespBody.succeed(space.getSpaceAuthorities());
	}
	
	@RequestMapping(value = "auth/add", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addAuth(@RequestParam("id") Long id,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "users", required = false) String users) {
		Space space = spaceRepository.findOne(id);
		if (!isSuperOrCreator(space.getCreator().getUsername())) {
			return RespBody.failed("您没有权限为该空间添加权限!");
		}

		List<SpaceAuthority> spaceAuthorities = new ArrayList<>();

		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(c -> {
				Channel channel = channelRepository.findOne(Long.valueOf(c));

				if (channel != null) {
					SpaceAuthority spaceAuthority = new SpaceAuthority();
					spaceAuthority.setSpace(space);
					spaceAuthority.setChannel(channel);
					spaceAuthorities.add(spaceAuthority);
				}

			});
		}

		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(u -> {
				User user = userRepository.findOne(u);
				if (user != null) {
					SpaceAuthority spaceAuthority = new SpaceAuthority();
					spaceAuthority.setSpace(space);
					spaceAuthority.setUser(user);
					spaceAuthorities.add(spaceAuthority);
				}

			});
		}

		List<SpaceAuthority> list = spaceAuthorityRepository.save(spaceAuthorities);
		spaceAuthorityRepository.flush();
		
		space.getSpaceAuthorities().addAll(list);

		return RespBody.succeed(space);
	}
	
	@RequestMapping(value = "auth/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeAuth(@RequestParam("id") Long id,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "users", required = false) String users) {
		Space space = spaceRepository.findOne(id);
		if (!isSuperOrCreator(space.getCreator().getUsername())) {
			return RespBody.failed("您没有权限为该空间移除权限!");
		}

		List<SpaceAuthority> list = new ArrayList<>();
		
		Collection<Channel> channelC = new ArrayList<>();
		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(c -> {
				Channel ch = new Channel();
				ch.setId(Long.valueOf(c));
				channelC.add(ch);
				
				SpaceAuthority sa = new SpaceAuthority();
				sa.setSpace(space);
				sa.setChannel(ch);
				list.add(sa);
			});
		}
		Collection<User> userC = new ArrayList<>();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(u -> {
				User user = new User();
				user.setUsername(u);
				userC.add(user);
				
				SpaceAuthority sa = new SpaceAuthority();
				sa.setSpace(space);
				sa.setUser(user);
				list.add(sa);
			});
		}
		
		if (channelC.size() > 0 && userC.size() > 0) {
			spaceAuthorityRepository.removeAuths(space, channelC, userC);
		} else {
			if (channelC.size() > 0) {
				spaceAuthorityRepository.removeChannelAuths(space, channelC);
			} else if (userC.size() > 0) {
				spaceAuthorityRepository.removeUserAuths(space, userC);
			}
		}
		
		spaceAuthorityRepository.flush();
		
		space.getSpaceAuthorities().removeAll(list);

		return RespBody.succeed(space);
	}

}
