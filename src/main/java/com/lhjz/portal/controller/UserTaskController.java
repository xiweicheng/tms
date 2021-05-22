/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.AsyncTask;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatLabel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.DirectPayload;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatLabelType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.ChatLabelRepository;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author xi
 * @date 2015年3月28日 下午1:19:05
 */
@RestController
@RequestMapping("admin/user/task")
@Slf4j
public class UserTaskController extends BaseController {

    @Autowired
    ChatLabelRepository chatLabelRepository;

    @Autowired
    MailSender mailSender;

    @Autowired
    ChatDirectRepository chatDirectRepository;

    @Autowired
    IChatMsg chatMsg;

    @Lazy
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    AsyncTask asyncTask;

    @GetMapping("listBy")
    public RespBody listBy(@PageableDefault(sort = {"id"}, direction = Direction.DESC) Pageable pageable,
                           @RequestParam("label") String label, @AuthenticationPrincipal UserDetails userDetails) {

        User loginUser = getLoginUser();

        List<ChatDirect> chatDirects = chatDirectRepository.queryByUserAndLabel(loginUser.getUsername(), label, pageable.getOffset(),
                pageable.getPageSize());
        long count = chatDirectRepository.countByUserAndLabel(loginUser.getUsername(), label);

        Page<ChatDirect> page = new PageImpl<>(chatDirects, pageable, count);

        return RespBody.succeed(page);
    }

    @PostMapping("status/update")
    public RespBody updateStatus(@RequestParam("from") String from, @RequestParam("to") String to,
                                 @RequestParam("id") Long id, @RequestParam(value = "all", defaultValue = "false") Boolean all) {

        ChatDirect chatDirect = chatDirectRepository.findOne(id);

        if (!AuthUtil.hasChannelAuth(chatDirect)) {
            return RespBody.failed("权限不足！");
        }

        User loginUser = getLoginUser();

        ChatLabel chatLabelFrom = chatLabelRepository.findOneByNameAndChatDirectAndStatusNot(from, chatDirect,
                Status.Deleted);

        if (chatLabelFrom != null) {
            Set<User> voters = chatLabelFrom.getVoters();

            boolean updated = false;

            if (Boolean.TRUE.equals(all)) {
                for (User user : voters) {
                    user.getVoterChatLabels().remove(chatLabelFrom);
                }
                userRepository.save(voters);
                userRepository.flush();

                voters.clear();

                updated = true;
            } else {
                if (voters.contains(loginUser)) {
                    loginUser.getVoterChatLabels().remove(chatLabelFrom);
                    voters.remove(loginUser);

                    userRepository.saveAndFlush(loginUser);

                    updated = true;
                }
            }

            if (updated) {

                if (voters.size() == 0) {
                    chatLabelFrom.setStatus(Status.Deleted);
                    chatLabelFrom = chatLabelRepository.saveAndFlush(chatLabelFrom);
                }

                logWithProperties(Action.Vote, Target.ChatLabel, chatLabelFrom.getId(), "name", from);

                chatDirect.setUpdateDate(new Date());
                chatDirectRepository.saveAndFlush(chatDirect);

                wsSend(chatDirect);
            }

        }

        ChatLabel chatLabelTo = chatLabelRepository.findOneByNameAndChatDirectAndStatusNot(to, chatDirect,
                Status.Deleted);

        if (chatLabelTo == null) {
            chatLabelTo = new ChatLabel();
            chatLabelTo.setName(to);
            chatLabelTo.setDescription(to);
            chatLabelTo.setChatDirect(chatDirect);
            chatLabelTo.setType(ChatLabelType.Tag);

            ChatLabel chatLabel2 = chatLabelRepository.saveAndFlush(chatLabelTo);

            chatLabel2.getVoters().add(loginUser);

            loginUser.getVoterChatLabels().add(chatLabel2);

            userRepository.saveAndFlush(loginUser);

            logWithProperties(Action.Create, Target.ChatLabel, chatLabel2.getId(), "name", to);

            chatDirect.setUpdateDate(new Date());
            chatDirectRepository.saveAndFlush(chatDirect);

            wsSend(chatDirect);

            userRepository.saveAndFlush(loginUser);

        } else {

            Set<User> voters = chatLabelTo.getVoters();
            if (!voters.contains(loginUser)) {
                loginUser.getVoterChatLabels().add(chatLabelTo);
                voters.add(loginUser);

                logWithProperties(Action.UnVote, Target.ChatLabel, chatLabelTo.getId(), "name", to);

                chatDirect.setUpdateDate(new Date());
                chatDirectRepository.saveAndFlush(chatDirect);

                wsSend(chatDirect);

                userRepository.saveAndFlush(loginUser);
            }
        }

        return RespBody.succeed(id);
    }

    private void wsSend(ChatDirect chatDirect) {

        asyncTask.updateChatDirect(chatDirect, DirectPayload.Cmd.U, messagingTemplate, WebUtil.getUsername());

    }

    @PostMapping("label/remove")
    public RespBody removeLabel(@RequestParam("id") Long id) {

        ChatLabel chatLabel = chatLabelRepository.findOne(id);

        User loginUser = getLoginUser();

        if (chatLabel != null) {
            Set<User> voters = chatLabel.getVoters();
            if (voters.contains(loginUser)) {
                loginUser.getVoterChatLabels().remove(chatLabel);
                voters.remove(loginUser);

                if (voters.size() == 0) {
                    chatLabel.setStatus(Status.Deleted);
                    chatLabel = chatLabelRepository.saveAndFlush(chatLabel);
                }

                logWithProperties(Action.Vote, Target.ChatLabel, chatLabel.getId(), "name", chatLabel.getName());

                chatLabel.getChatDirect().setUpdateDate(new Date());
                chatDirectRepository.saveAndFlush(chatLabel.getChatDirect());

                wsSend(chatLabel.getChatDirect());

                userRepository.saveAndFlush(loginUser);
            }
        }

        return RespBody.succeed(id);
    }

    @PostMapping("remove")
    public RespBody remove(@RequestParam("id") Long id, @RequestParam("label") String label) {

        ChatDirect chatDirect = chatDirectRepository.findOne(id);

        if (!isSuperOrCreator(chatDirect.getCreator())) {
            return RespBody.failed("权限不足！");
        }

        final ChatLabel chatLabel = chatLabelRepository.findOneByNameAndChatDirectAndStatusNot(label, chatDirect,
                Status.Deleted);

        if (chatLabel != null) {

            Set<User> voters = chatLabel.getVoters();

            voters.forEach(voter ->
                    voter.getVoterChatLabels().remove(chatLabel)
            );

            logWithProperties(Action.Vote, Target.ChatLabel, chatLabel.getId(), "name", chatLabel.getName());

            userRepository.save(voters);
            userRepository.flush();

            chatLabel.setStatus(Status.Deleted);
            chatLabelRepository.saveAndFlush(chatLabel);

            chatDirect.setUpdateDate(new Date());
            chatDirectRepository.saveAndFlush(chatDirect);

            wsSend(chatDirect);
        }

        return RespBody.succeed(id);
    }

}
