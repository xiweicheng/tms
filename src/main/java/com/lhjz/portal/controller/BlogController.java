/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.AsyncTask;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.BlogAuthority;
import com.lhjz.portal.entity.BlogFollower;
import com.lhjz.portal.entity.BlogHistory;
import com.lhjz.portal.entity.BlogNews;
import com.lhjz.portal.entity.BlogStow;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.entity.Dir;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Log;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.SpaceAuthority;
import com.lhjz.portal.entity.Tag;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.exception.BizException;
import com.lhjz.portal.model.BlogCommentPayload;
import com.lhjz.portal.model.BlogPayload;
import com.lhjz.portal.model.BlogPayload.Cmd;
import com.lhjz.portal.model.BlogSearchResult;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.PollBlog;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.model.ToastrPayload;
import com.lhjz.portal.pojo.BlogSortForm;
import com.lhjz.portal.pojo.BlogSortItem;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Editor;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.BlogAuthorityRepository;
import com.lhjz.portal.repository.BlogFollowerRepository;
import com.lhjz.portal.repository.BlogHistoryRepository;
import com.lhjz.portal.repository.BlogNewsRepository;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.BlogStowRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.DirRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.SpaceRepository;
import com.lhjz.portal.repository.TagRepository;
import com.lhjz.portal.service.BlogLockService;
import com.lhjz.portal.service.ChatChannelService;
import com.lhjz.portal.service.FileService;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.LuckySheetUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.ThreadUtil;
import com.lhjz.portal.util.ValidateUtil;
import com.lhjz.portal.util.WebUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xi
 * @date 2015年3月28日 下午1:19:05
 */
@Controller
@RequestMapping("admin/blog")
public class BlogController extends BaseController {

    public static final String BLOG_PATH = "#/blog/";
    public static final String TEMPLATES_MAIL_MAIL_DYNAMIC = "templates/mail/mail-dynamic";
    public static final String TITLE = "title";
    public static final String HREF = "href";
    public static final String DATE = "date";
    public static final String USER = "user";
    public static final String CONTENT = "content";
    public static final String BLOG_UPDATE = "/blog/update";
    public static final String FAIL_MSG_NO_AUTH_EDIT_BLOG = "您没有权限编辑该博文!";
    public static final String FOLLOWER = "follower";
    public static final String CID = "?cid=";
    public static final String ERR_NO_AUTH = "权限不足！";
    public static final String UTF_8 = "UTF-8";
    public static final String ERR_FILE_NOT_EXISTS = "文件不存在！";

    static Logger logger = LoggerFactory.getLogger(BlogController.class);

    @Value("${tms.blog.upload.path}")
    private String uploadPath;

    @Value("${tms.blog.md2pdf.path}")
    private String md2pdfPath;

    @Value("${tms.bin.node.path}")
    private String nodePath;

    @Value("${tms.chat.url.summary.off}")
    Boolean off; // 是否关闭

    @Autowired
    BlogRepository blogRepository;

    @Autowired
    BlogHistoryRepository blogHistoryRepository;

    @Autowired
    BlogAuthorityRepository blogAuthorityRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    ChatDirectRepository chatDirectRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    BlogStowRepository blogStowRepository;

    @Autowired
    BlogFollowerRepository blogFollowerRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    DirRepository dirRepository;

    @Autowired
    BlogNewsRepository blogNewsRepository;

    @Autowired
    MailSender mailSender;

    @Autowired
    ChatChannelService chatChannelService;

    @Autowired
    BlogLockService blogLockService;

    @Lazy
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    AsyncTask asyncTask;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    FileService fileService;

    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public RespBody create(@RequestParam("url") String url,
                           @RequestParam(value = "spaceId", required = false) Long spaceId,
                           @RequestParam(value = "dirId", required = false) Long dirId,
                           @RequestParam(value = "pid", required = false) Long pid,
                           @RequestParam(value = "privated", required = false) Boolean privated,
                           @RequestParam(value = "opened", required = false) Boolean opened,
                           @RequestParam(value = "editor", required = false) String editor,
                           @RequestParam(value = "uuid", required = false) String uuid,
                           @RequestParam(value = "usernames", required = false) String usernames, @RequestParam(TITLE) String title,
                           @RequestParam(CONTENT) String content, @RequestParam("contentHtml") String contentHtml) {

        if (StringUtil.isEmpty(title)) {
            return RespBody.failed("标题不能为空!");
        }

        if (StringUtil.isEmpty(content)) {
            return RespBody.failed("内容不能为空!");
        }

        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setUuid(uuid);

        if (StringUtils.isNotBlank(editor)) {
            blog.setEditor(Editor.valueOf(editor));
        } else {
            blog.setEditor(Editor.Markdown);
        }

        if (spaceId != null) {
            Space space = spaceRepository.findOne(spaceId);
            if (space == null) {
                return RespBody.failed("指定空间不存在!");
            }
            blog.setSpace(space);
        }

        if (dirId != null) {
            Dir dir = dirRepository.findOne(dirId);
            if (dir == null) {
                return RespBody.failed("指定分类不存在!");
            }
            blog.setDir(dir);
        }

        if (privated != null) {
            blog.setPrivated(privated);
        }

        if (opened != null) {
            blog.setOpened(opened);
        }

        if (pid != null) {
            blog.setPid(pid);
        }

        Blog blog2 = blogRepository.saveAndFlush(blog);

        if (pid != null) {
            blogRepository.updateHasChild(true, pid);
        }

        log(Action.Create, Target.Blog, blog2.getId(), blog2.getTitle());

        final String href = url + BLOG_PATH + blog2.getId();
        final String html = contentHtml;
        final User loginUser = getLoginUser();

        final Mail mail = Mail.instance();
        if (StringUtil.isNotEmpty(usernames)) {

            if (StringUtil.isNotEmpty(usernames)) {
                String[] usernameArr = usernames.split(",");
                Arrays.asList(usernameArr).stream().forEach(username ->
                        mail.addUsers(getUser(username))
                );

                wsSendToUsers(blog2, Cmd.At, WebUtil.getUsername(), usernameArr);
            }

            try {
                mailSender
                        .sendHtmlByQueue(String.format("TMS-博文频道@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                                TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC,
                                        MapUtil.objArr2Map(USER, loginUser, DATE, new Date(), HREF, href, TITLE,
                                                "下面的博文消息中有@到你", CONTENT, html)),
                                getLoginUserName(loginUser), mail.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return RespBody.succeed(blog2);
    }

    private void wsSendToUsers(Blog blog, Cmd cmd, String loginUsername, String... usernames) {
        try {

            ThreadUtil.exec(() -> {
                BlogPayload blogPayload = BlogPayload.builder().id(blog.getId()).version(blog.getVersion())
                        .title(blog.getTitle()).cmd(cmd).username(loginUsername).build();

                for (String username : usernames) {

                    BlogNews blogNews = blogNewsRepository.saveAndFlush(BlogNews.builder().bid(blog.getId())
                            .title(blog.getTitle()).to(username).cmd(cmd).username(loginUsername).build());
                    blogPayload.setNid(blogNews.getId());

                    messagingTemplate.convertAndSendToUser(username, BLOG_UPDATE, blogPayload);

                }

            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void wsSendToUsers(Blog blog, Comment comment, Cmd cmd, String loginUsername, String... usernames) {
        try {
            ThreadUtil.exec(() -> {
                BlogPayload blogPayload = BlogPayload.builder().id(blog.getId()).version(blog.getVersion())
                        .title(blog.getTitle()).cid(comment.getId()).cmd(cmd).username(loginUsername).build();

                for (String username : usernames) {

                    BlogNews blogNews = blogNewsRepository
                            .saveAndFlush(BlogNews.builder().bid(blog.getId()).cid(comment.getId())
                                    .title(blog.getTitle()).to(username).cmd(cmd).username(loginUsername).build());
                    blogPayload.setNid(blogNews.getId());

                    messagingTemplate.convertAndSendToUser(username, BLOG_UPDATE, blogPayload);
                }

            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void wsSend(Blog blog, Cmd cmd, String loginUsername) {
        try {
            ThreadUtil.exec(() ->
                    messagingTemplate.convertAndSend(BLOG_UPDATE,
                            BlogPayload.builder().id(blog.getId()).openEdit(blog.getOpenEdit()).version(blog.getVersion())
                                    .title(blog.getTitle()).cmd(cmd).username(loginUsername).build())
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "listMy", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMy(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

        List<Blog> blogs = blogRepository.findByPidIsNullAndStatusNot(Status.Deleted, sort).stream()
                .filter(this::hasAuth).peek(b -> {
                    b.setContent(null);
                    b.setBlogAuthorities(null);
                    b.setUpdater(null);

                    User creator = b.getCreator();
                    if (creator != null) {
                        User user2 = new User();
                        user2.setUsername(creator.getUsername());
                        user2.setAuthorities(null);
                        user2.setStatus(null);
                        b.setCreator(user2);
                    }

                    b.setCreateDate(null);
                    b.setOpenEdit(null);
                    b.setOpened(null);
                    b.setReadCnt(null);
                    b.setTags(null);
                    b.setType(null);

                    Dir dir = b.getDir();
                    if (dir != null) {
                        Dir dir2 = new Dir();
                        dir2.setId(dir.getId());
                        dir2.setOpened(null);
                        dir2.setPrivated(null);
                        dir2.setStatus(null);
                        b.setDir(dir2);
                    }

                    Space space = b.getSpace();
                    if (space != null) {
                        Space space2 = new Space();
                        space2.setId(space.getId());
                        space2.setDirs(null);
                        space2.setOpened(null);
                        space2.setPrivated(null);
                        space2.setSpaceAuthorities(null);
                        space2.setStatus(null);
                        space2.setType(null);
                        b.setSpace(space2);
                    }

                }).collect(Collectors.toList());

        return RespBody.succeed(blogs);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody update(@RequestParam("url") String url,
                           @RequestParam(value = "usernames", required = false) String usernames, @RequestParam("id") Long id,
                           @RequestParam("version") Long version, @RequestParam(TITLE) String title,
                           @RequestParam(CONTENT) String content, @RequestParam(value = "diff", required = false) String diff,
                           @RequestParam(value = "contentHtml", required = false) String contentHtml,
                           @RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld) {

        if (StringUtil.isEmpty(title)) {
            return RespBody.failed("更新标题不能为空!");
        }

        if (StringUtil.isEmpty(content)) {
            return RespBody.failed("更新内容不能为空!");
        }

        Blog blog = blogRepository.findOne(id);

        boolean isOpenEdit = Boolean.TRUE.equals(blog.getOpenEdit());

        if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
            return RespBody.failed(FAIL_MSG_NO_AUTH_EDIT_BLOG);
        }

        if (isOpenEdit && !hasAuth(blog)) {
            return RespBody.failed(FAIL_MSG_NO_AUTH_EDIT_BLOG);
        }

        if (!Long.valueOf(blog.getVersion()).equals(version)) {
            return RespBody.failed("该博文已经被其他人更新,请刷新博文重新编辑提交!");
        }

        boolean isUpdated = false;

        if (!content.equals(blog.getContent())) {
            logWithProperties(Action.Update, Target.Blog, blog.getId(), CONTENT, diff, blog.getTitle());
            isUpdated = true;
        }

        if (!title.equals(blog.getTitle())) {
            logWithProperties(Action.Update, Target.Blog, blog.getId(), TITLE, title, blog.getTitle());
            isUpdated = true;
        }

        if (isUpdated) {

            BlogHistory blogHistory = new BlogHistory();
            blogHistory.setBlog(blog);
            blogHistory.setTitle(blog.getTitle());
            blogHistory.setContent(blog.getContent());
            blogHistory.setBlogUpdater(blog.getUpdater());
            blogHistory.setBlogUpdateDate(blog.getUpdateDate());
            blogHistory.setEditor(blog.getEditor());

            blogHistoryRepository.saveAndFlush(blogHistory);

            blog.setTitle(title);
            blog.setContent(content);

            Blog blog2 = blogRepository.saveAndFlush(blog);

            wsSend(blog2, Cmd.U, WebUtil.getUsername());

            final User loginUser = getLoginUser();
            final String href = url + BLOG_PATH + blog2.getId();
            final String html;
            if (StringUtil.isNotEmpty(diff)) {
                html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
            } else {
                html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
            }

            final Mail mail = Mail.instance();

            List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog2, Status.Deleted);

            // 编辑非自己的博文，自动成为该博文的关注者
            if (!blog.getCreator().equals(loginUser)) {
                // 没有关注该博文
                BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreator(blog2, loginUser);

                if (blogFollower != null) {
                    if (blogFollower.getStatus().equals(Status.Deleted)) {
                        blogFollower.setStatus(Status.New);

                        blogFollowerRepository.saveAndFlush(blogFollower);

                        logWithProperties(Action.Update, Target.Blog, id, FOLLOWER, blog2.getTitle());
                    }
                } else {
                    BlogFollower blogFollower2 = new BlogFollower();
                    blogFollower2.setBlog(blog2);

                    blogFollowerRepository.saveAndFlush(blogFollower2);

                    logWithProperties(Action.Update, Target.Blog, id, FOLLOWER, blog.getTitle());
                }
            }

            mail.addUsers(followers.stream().map(BlogFollower::getCreator).collect(Collectors.toList()), loginUser);
            mail.addUsers(Arrays.asList(blog2.getCreator()), loginUser);

            // 博文更新通知优先级： @  > 我关注的 > 我的

            List<String> usernameArr = Arrays
                    .asList(StringUtil.isNotEmpty(usernames) ? usernames.split(",") : new String[0]);

            List<String> fs = followers.stream().map(f -> f.getCreator().getUsername())
                    .filter(f -> !usernameArr.contains(f)).collect(Collectors.toList());

            wsSendToUsers(blog2, Cmd.F, WebUtil.getUsername(), fs.toArray(new String[0]));

            String bCreator = blog.getCreator().getUsername();

            if (!blog.getCreator().equals(loginUser) && !usernameArr.contains(bCreator) && !fs.contains(bCreator)) {
                wsSendToUsers(blog, Cmd.OU, WebUtil.getUsername(), bCreator);
            }

            if (usernameArr.size() > 0) {
                usernameArr.stream().forEach(username ->
                        mail.addUsers(getUser(username))
                );

                wsSendToUsers(blog2, Cmd.At, WebUtil.getUsername(), usernameArr.toArray(new String[0]));
            }

            if (!mail.isEmpty()) {

                try {
                    mailSender.sendHtmlByQueue(
                            String.format("TMS-博文编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                            TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC,
                                    MapUtil.objArr2Map(USER, loginUser, DATE, new Date(), HREF, href, TITLE,
                                            "下面编辑的博文消息中有@到你", CONTENT, html)),
                            getLoginUserName(loginUser), mail.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return RespBody.succeed(blog2);
        } else {
            return RespBody.failed("修改博文无变更!");
        }

    }

    @RequestMapping(value = "editor/change", method = RequestMethod.POST)
    @ResponseBody
    public RespBody changeEditor(@RequestParam("id") Long id, @RequestParam("version") Long version,
                                 @RequestParam(CONTENT) String content, @RequestParam("editor") String editor) {

        if (StringUtil.isEmpty(content)) {
            return RespBody.failed("更新内容不能为空!");
        }

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed(FAIL_MSG_NO_AUTH_EDIT_BLOG);
        }

        if (blog.getVersion() != version.longValue()) {
            return RespBody.failed("该博文已经被其他人更新,请刷新博文重新编辑提交!");
        }

        BlogHistory blogHistory = new BlogHistory();
        blogHistory.setBlog(blog);
        blogHistory.setTitle(blog.getTitle());
        blogHistory.setContent(blog.getContent());
        blogHistory.setBlogUpdater(blog.getUpdater());
        blogHistory.setBlogUpdateDate(blog.getUpdateDate());
        blogHistory.setEditor(blog.getEditor());

        blogHistoryRepository.saveAndFlush(blogHistory);

        blog.setContent(content);
        blog.setEditor(Editor.valueOf(editor));

        Blog blog2 = blogRepository.saveAndFlush(blog);

        wsSend(blog2, Cmd.U, WebUtil.getUsername());

        return RespBody.succeed(blog2);

    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseBody
    public RespBody delete(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限删除该博文!");
        }

        fileService.removeFileByAtId(blog.getUuid());

        blog.setStatus(Status.Deleted);

        blogRepository.saveAndFlush(blog);

        wsSend(blog, Cmd.D, WebUtil.getUsername());

        log(Action.Delete, Target.Blog, id, blog.getTitle());

        // check看被删除博文的父级博文是否还存在子级博文
        Long pid = blog.getPid();
        if (pid != null) {
            long cnt = blogRepository.countByPid(pid);
            blogRepository.updateHasChild(cnt > 0, pid);
        }

        // 被删除博文的子级博文也都需要删除
        deleteChilds(blog);

        return RespBody.succeed(id);
    }

    // 级联删除子级博文
    private void deleteChilds(Blog blog) {
        List<Blog> childs = blogRepository.findByPidAndStatusNot(blog.getId(), Status.Deleted);
        childs.forEach(child -> {
            deleteChilds(child);
            blogRepository.updateStatus(Status.Deleted, child.getId());
        });

    }

    @RequestMapping(value = "get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody get(@RequestParam("id") Long id, @RequestParam(value = "inc", defaultValue = "true") Boolean inc) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null || Status.Deleted.equals(blog.getStatus())) {
            return RespBody.failed("博文不存在或者已经被删除!");
        }

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限查看该博文!");
        }

        if (Boolean.TRUE.equals(inc)) {
            Long readCnt = blog.getReadCnt();
            if (readCnt == null) {
                readCnt = 1L;
            } else {
                readCnt = readCnt + 1;
            }

            blogRepository.updateReadCnt(readCnt, id);

            blog.setReadCnt(readCnt);
        }

        return RespBody.succeed(blog);
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    @ResponseBody
    public RespBody search(@RequestParam("search") String search,
                           @RequestParam(value = "comment", defaultValue = "false") Boolean comment,
                           @RequestParam(value = "ellipsis", defaultValue = "60") Integer ellipsis,
                           @SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

        if (StringUtil.isEmpty(search)) {
            return RespBody.failed("检索条件不能为空!");
        }

        List<Blog> blogs = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();

        // 按标签检索
        if (search.toLowerCase().startsWith("tags:") || search.toLowerCase().startsWith("tag:")) {
            String[] arr = search.split(":", 2);
            if (StringUtil.isNotEmpty(arr[1].trim())) {
                String[] tags = arr[1].trim().split("\\s+");
                blogs = blogRepository.findByStatusNotAndTags_nameIn(Status.Deleted, Arrays.asList(tags), sort).stream()
                        .filter(this::hasAuth).peek(b -> {
                            b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
                            b.setBlogAuthorities(null);
                        }).collect(Collectors.toList());
            }

            if (Boolean.TRUE.equals(comment)) {
                return RespBody.succeed(new BlogSearchResult(blogs, comments));
            }

        } else if (search.toLowerCase().startsWith("from:")) {
            String[] arr = search.split(":", 2);
            if (StringUtil.isNotEmpty(arr[1].trim())) {
                String[] condis = arr[1].trim().split("\\s+");

                User user = getUser(condis[0]);

                if (user != null) {

                    if (condis.length == 1) {

                        blogs = blogRepository.findByCreatorAndStatusNot(user, Status.Deleted, sort).stream()
                                .filter(this::hasAuth).peek(b -> {
                                    b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
                                    b.setBlogAuthorities(null);
                                }).collect(Collectors.toList());
                    } else {
                        blogs = blogRepository
                                .findByCreatorAndStatusNotAndTitleContainingIgnoreCaseOrCreatorAndStatusNotAndContentContainingIgnoreCase(
                                        user, Status.Deleted, condis[1], user, Status.Deleted, condis[1], sort)
                                .stream().filter(this::hasAuth).peek(b -> {
                                    b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
                                    b.setBlogAuthorities(null);
                                }).collect(Collectors.toList());
                    }
                }
            }

            if (Boolean.TRUE.equals(comment)) {
                return RespBody.succeed(new BlogSearchResult(blogs, comments));
            }

        } else {
            blogs = blogRepository
                    .findByStatusNotAndTitleContainingIgnoreCaseOrStatusNotAndContentContainingIgnoreCase(
                            Status.Deleted, search, Status.Deleted, search, sort)
                    .stream().filter(this::hasAuth).peek(b -> {
                        b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
                        b.setBlogAuthorities(null);
                    }).collect(Collectors.toList());

            if (Boolean.TRUE.equals(comment)) {
                comments = commentRepository.findByTypeAndStatusNotAndContentContainingIgnoreCase(CommentType.Blog,
                        Status.Deleted, search, sort).stream().filter(c -> hasAuth(Long.valueOf(c.getTargetId())))
                        .peek(c ->
                                c.setContent(StringUtil.limitLength(c.getContent(), ellipsis))
                        ).collect(Collectors.toList());

                return RespBody.succeed(new BlogSearchResult(blogs, comments));
            }
        }

        return RespBody.succeed(blogs);
    }

    @RequestMapping(value = "openEdit", method = RequestMethod.POST)
    @ResponseBody
    public RespBody openEdit(@RequestParam("id") Long id, @RequestParam("open") Boolean open) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null) {
            return RespBody.failed("博文消息不存在,可能已经被删除!");
        }

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("没有权限修改博文开放编辑全选!");
        }

        blog.setOpenEdit(open);
        blogRepository.saveAndFlush(blog);

        if (Boolean.FALSE.equals(blog.getPrivated()) && Boolean.TRUE.equals(open)) {
            wsSend(blog, Cmd.Open, WebUtil.getUsername());
        }

        logWithProperties(Action.Update, Target.Blog, id, "openEdit", open, blog.getTitle());

        return RespBody.succeed();
    }

    private boolean isVoterExists(String voters) {
        boolean isExits = false;
        if (voters != null) {
            String loginUsername = WebUtil.getUsername();
            String[] voterArr = voters.split(",");

            for (String voter : voterArr) {
                if (voter.equals(loginUsername)) {
                    isExits = true;
                    break;
                }
            }
        }

        return isExits;
    }

    private String calcVoters(String voters) {

        List<String> list = Stream.of(voters.split(",")).filter(v -> !v.equals(WebUtil.getUsername()))
                .collect(Collectors.toList());

        return StringUtil.join(",", list);
    }

    @RequestMapping(value = "vote", method = RequestMethod.POST)
    @ResponseBody
    public RespBody vote(@RequestParam("id") Long id, @RequestParam("url") String url,
                         @RequestParam("contentHtml") String contentHtml,
                         @RequestParam(value = "type", required = false) String type) {

        Blog blog = blogRepository.findOne(id);
        if (blog == null) {
            return RespBody.failed("投票博文消息不存在!");
        }

        String loginUsername = WebUtil.getUsername();

        String title = "";
        final User loginUser = getLoginUser();

        if (VoteType.Zan.name().equalsIgnoreCase(type)) {
            String voteZan = blog.getVoteZan();
            if (isVoterExists(voteZan)) {
                return RespBody.failed("您已经投票[赞]过！");
            } else {

                String vz = voteZan == null ? loginUsername : voteZan + ',' + loginUsername;

                Integer voteZanCnt = blog.getVoteZanCnt();
                Integer vzc = voteZanCnt == null ? 1 : voteZanCnt + 1;

                blogRepository.updateVoteZan(vz, vzc, id);

                logWithProperties(Action.Update, Target.Blog, id, "voteZan", blog.getTitle());

                blog.setVoteZan(vz);
                blog.setVoteZanCnt(vzc);
                title = loginUser.getName() + "[" + loginUsername + "]赞了你的博文消息!";
            }

        } else {
            String voteZan = blog.getVoteZan();
            if (isVoterExists(voteZan)) {
                String vz = this.calcVoters(voteZan);
                Integer voteZanCnt = blog.getVoteZanCnt();
                Integer vzc = voteZanCnt == null ? 0 : voteZanCnt - 1;

                blogRepository.updateVoteZan(vz, vzc, id);

                blog.setVoteZan(vz);
                blog.setVoteZanCnt(vzc);
                return RespBody.succeed(blog);
            }
        }

        final String href = url + BLOG_PATH + id;
        final String titleHtml = title;
        final Mail mail = Mail.instance().addUsers(blog.getCreator());
        final String html = "<h3>投票博文消息内容:</h3><hr/>" + contentHtml;

        try {
            mailSender.sendHtmlByQueue(String.format("TMS-博文消息投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                    TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser, DATE,
                            new Date(), HREF, href, TITLE, titleHtml, CONTENT, html)),
                    getLoginUserName(loginUser), mail.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RespBody.succeed(blog);
    }

    @RequestMapping(value = "comment/vote", method = RequestMethod.POST)
    @ResponseBody
    public RespBody voteComment(@RequestParam("cid") Long cid, @RequestParam("url") String url,
                                @RequestParam("contentHtml") String contentHtml,
                                @RequestParam(value = "type", required = false) String type) {

        Comment comment = commentRepository.findOne(cid);
        if (comment == null) {
            return RespBody.failed("投票博文评论不存在!");
        }
        String loginUsername = WebUtil.getUsername();

        Comment comment2 = null;

        String title = "";
        final User loginUser = getLoginUser();

        if (VoteType.Zan.name().equalsIgnoreCase(type)) {
            String voteZan = comment.getVoteZan();
            if (isVoterExists(voteZan)) {
                return RespBody.failed("您已经投票[赞]过！");
            } else {
                comment.setVoteZan(voteZan == null ? loginUsername : voteZan + ',' + loginUsername);

                Integer voteZanCnt = comment.getVoteZanCnt();
                comment.setVoteZanCnt(voteZanCnt == null ? 1 : voteZanCnt + 1);

                comment2 = commentRepository.saveAndFlush(comment);
                title = loginUser.getName() + "[" + loginUsername + "]赞了你的博文评论!";

                logWithProperties(Action.Update, Target.Comment, cid, "voteZan", comment2.getTargetId(),
                        comment2.getContent());
            }

        } else {
            String voteZan = comment.getVoteZan();
            if (isVoterExists(voteZan)) {
                comment.setVoteZan(this.calcVoters(voteZan));
                Integer voteZanCnt = comment.getVoteZanCnt();
                comment.setVoteZanCnt(voteZanCnt == null ? 0 : voteZanCnt - 1);

                comment2 = commentRepository.saveAndFlush(comment);
                return RespBody.succeed(comment2);
            }
        }

        final String href = url + BLOG_PATH + comment.getTargetId() + CID + cid;
        final String titleHtml = title;
        final Mail mail = Mail.instance().addUsers(comment.getCreator());
        final String html = "<h3>投票博文评论内容:</h3><hr/>" + contentHtml;

        try {
            mailSender.sendHtmlByQueue(String.format("TMS-博文评论投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                    TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser, DATE,
                            new Date(), HREF, href, TITLE, titleHtml, CONTENT, html)),
                    getLoginUserName(loginUser), mail.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RespBody.succeed(comment2);
    }

    @RequestMapping(value = "share/to/search", method = RequestMethod.GET)
    @ResponseBody
    public RespBody searchShareTo(@RequestParam("search") String search) {

        if (StringUtil.isEmpty(search)) {
            return RespBody.failed("检索条件不能为空!");
        }

        Map<String, Object> map = new HashMap<>();

        List<User> users = userRepository.findTop6ByUsernameContainingIgnoreCaseAndEnabledTrue(search);
        List<Channel> channels = channelRepository.findTop6ByNameContainingIgnoreCaseAndStatusNot(search,
                Status.Deleted);
        channels.forEach(c -> c.setMembers(null));

        map.put("users", users);
        map.put("channels", channels);

        return RespBody.succeed(map);
    }

    @RequestMapping(value = "share", method = RequestMethod.POST)
    @ResponseBody
    public RespBody share(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
                          @RequestParam("html") String html, @RequestParam(value = "desc", required = false) String desc,
                          @RequestParam(value = "users", required = false) String users,
                          @RequestParam(value = "channels", required = false) String channels,
                          @RequestParam(value = "mails", required = false) String mails) {

        Blog blog = blogRepository.findOne(id);

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限分享该博文!");
        }

        final User loginUser = getLoginUser();

        final String href = basePath + BLOG_PATH + id;
        final String html2 = StringUtil.replace(
                "<h1 style=\"color: blue;\">分享博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
                blog.getTitle(), html);

        final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的博文有分享到你";
        final String shareDesc = StringUtil.isNotEmpty(desc) ? "> **分享说明：**" + desc : StringUtil.EMPTY;

        Mail mail = Mail.instance();
        if (StringUtil.isNotEmpty(users)) {
            Stream.of(users.split(",")).forEach(username -> {
                User user = getUser(username);
                if (user != null) {
                    mail.addUsers(user);

                    ChatDirect chatDirect = new ChatDirect();
                    chatDirect.setChatTo(user);
                    chatDirect.setContent(
                            StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的博文分享:  [{?2}]({?3})\n\n{?5}\n\n---\n\n{?4}",
                                    loginUser.getUsername(), blog.getTitle(), href, blog.getContent(), shareDesc));

                    chatDirectRepository.saveAndFlush(chatDirect);
                }
            });
        }
        if (StringUtil.isNotEmpty(channels)) {
            Stream.of(channels.split(",")).forEach(name -> {
                Channel channel = channelRepository.findOneByName(name);
                if (channel != null) {
                    channel.getMembers().forEach(mail::addUsers);

                    ChatChannel chatChannel = new ChatChannel();
                    chatChannel.setChannel(channel);
                    chatChannel.setContent(
                            StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的博文分享:  [{?2}]({?3})\n\n{?5}\n\n---\n\n{?4}",
                                    loginUser.getUsername(), blog.getTitle(), href, blog.getContent(), shareDesc));

                    chatChannelService.save(chatChannel);
                }
            });
        }

        if (StringUtil.isNotEmpty(mails)) {
            Stream.of(mails.split(",")).forEach(m -> {
                if (ValidateUtil.isEmail(m)) {
                    mail.add(m);
                }
            });
        }

        ThreadUtil.exec(() -> {

            try {
                Thread.sleep(3000);
                mailSender.sendHtml(String.format("TMS-博文分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                        TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser,
                                DATE, new Date(), HREF, href, TITLE, title, CONTENT, html2)),
                        getLoginUserName(loginUser), mail.get());
                logger.info("博文分享邮件发送成功！");
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                logger.error("博文分享邮件发送失败！");
            }

        });
        return RespBody.succeed();
    }

    @RequestMapping(value = "comment/share", method = RequestMethod.POST)
    @ResponseBody
    public RespBody shareComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
                                 @RequestParam(HREF) final String href, @RequestParam("html") String html,
                                 @RequestParam(value = "desc", required = false) String desc,
                                 @RequestParam(value = "users", required = false) String users,
                                 @RequestParam(value = "channels", required = false) String channels,
                                 @RequestParam(value = "mails", required = false) String mails) {

        Comment comment = commentRepository.findOne(id);

        final User loginUser = getLoginUser();

        final String html2 = StringUtil.replace(
                "<h1 style=\"color: blue;\">分享博文评论: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
                "博文评论链接", html);

        final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的博文评论有分享到你";

        Mail mail = Mail.instance();
        if (StringUtil.isNotEmpty(users)) {
            Stream.of(users.split(",")).forEach(username -> {
                User user = getUser(username);
                if (user != null) {
                    mail.addUsers(user);

                    ChatDirect chatDirect = new ChatDirect();
                    chatDirect.setChatTo(user);
                    chatDirect.setContent(
                            StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的博文评论分享:  [{?2}]({?3})\n\n---\n\n{?4}",
                                    loginUser.getUsername(), "博文评论链接", href, comment.getContent()));

                    chatDirectRepository.saveAndFlush(chatDirect);
                }
            });
        }
        if (StringUtil.isNotEmpty(channels)) {
            Stream.of(channels.split(",")).forEach(name -> {
                Channel channel = channelRepository.findOneByName(name);
                if (channel != null) {
                    channel.getMembers().forEach(mail::addUsers);

                    ChatChannel chatChannel = new ChatChannel();
                    chatChannel.setChannel(channel);
                    chatChannel.setContent(
                            StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的博文评论分享:  [{?2}]({?3})\n\n---\n\n{?4}",
                                    loginUser.getUsername(), "博文评论链接", href, comment.getContent()));

                    chatChannelService.save(chatChannel);
                }
            });
        }

        if (StringUtil.isNotEmpty(mails)) {
            Stream.of(mails.split(",")).forEach(m -> {
                if (ValidateUtil.isEmail(m)) {
                    mail.add(m);
                }
            });
        }

        ThreadUtil.exec(() -> {

            try {
                Thread.sleep(3000);
                mailSender.sendHtml(String.format("TMS-博文评论分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                        TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser,
                                DATE, new Date(), HREF, href, TITLE, title, CONTENT, html2)),
                        getLoginUserName(loginUser), mail.get());
                logger.info("博文评论分享邮件发送成功！");
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                logger.error("博文评论分享邮件发送失败！");
            }

        });
        return RespBody.succeed();
    }

    @RequestMapping(value = "comment/create", method = RequestMethod.POST)
    @ResponseBody
    public RespBody createComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
                                  @RequestParam(CONTENT) String content,
                                  @RequestParam(value = "contentHtml", required = false) String contentHtml,
                                  @RequestParam(value = "editor", required = false) String editor,
                                  @RequestParam(value = "uuid", required = false) String uuid,
                                  @RequestParam(value = "users", required = false) String users) {

        if (!hasAuth(id)) {
            return RespBody.failed("权限不足!");
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTargetId(String.valueOf(id));
        comment.setType(CommentType.Blog);
        comment.setUuid(uuid);

        if (StringUtils.isNotBlank(editor)) {
            comment.setEditor(Editor.valueOf(editor));
        } else {
            comment.setEditor(Editor.Markdown);
        }

        Comment comment2 = commentRepository.saveAndFlush(comment);

        if (Boolean.FALSE.equals(off)) {
            asyncTask.updateBlogComment(content, comment2.getId(), messagingTemplate, WebUtil.getUsername(), users);
        }

        logWithProperties(Action.Create, Target.Comment, comment2.getId(), editor, content, id);

        final User loginUser = getLoginUser();

        final String href = basePath + BLOG_PATH + id + CID + comment2.getId();

        Blog blog = blogRepository.findOne(id);

        Mail mail = Mail.instance();
        mail.addUsers(Arrays.asList(blog.getCreator()), loginUser);

        // 博文更新通知优先级： @  > 我关注的 > 我的

        List<String> atUsers = Arrays.asList(StringUtil.isNotEmpty(users) ? users.split(",") : new String[0]);

        if (atUsers.size() > 0) {
            atUsers.forEach(username -> {
                User user = getUser(username);
                mail.addUsers(user);
            });
            wsSendToUsers(blog, comment2, Cmd.CAt, WebUtil.getUsername(), atUsers.toArray(new String[0]));
        }

        List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
        mail.addUsers(followers.stream().map(BlogFollower::getCreator).collect(Collectors.toList()), loginUser);

        List<String> fs = followers.stream().map(f -> f.getCreator().getUsername()).filter(f -> !atUsers.contains(f))
                .collect(Collectors.toList());
        wsSendToUsers(blog, comment2, Cmd.FCC, WebUtil.getUsername(), fs.toArray(new String[0]));

        String bCreator = blog.getCreator().getUsername();
        if (!blog.getCreator().equals(loginUser) && !atUsers.contains(bCreator) && !fs.contains(bCreator)) {
            wsSendToUsers(blog, comment2, Cmd.CC, WebUtil.getUsername(), bCreator);
        }

        // auto follow blog
        boolean isFollower = followers.stream().anyMatch(f -> f.getCreator().equals(loginUser));
        if (!isFollower && !blog.getCreator().equals(loginUser)) {
            BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreator(blog, getLoginUser());

            if (blogFollower != null) {
                if (blogFollower.getStatus().equals(Status.Deleted)) {
                    blogFollower.setStatus(Status.New);

                    blogFollowerRepository.saveAndFlush(blogFollower);
                }
            } else {
                blogFollower = new BlogFollower();
                blogFollower.setBlog(blog);

                blogFollowerRepository.saveAndFlush(blogFollower);
            }
        }

        if (StringUtil.isEmpty(contentHtml)) {
            if (Editor.Html.equals(comment2.getEditor())) {
                contentHtml = comment.getContent();
            } else {
                contentHtml = "内容暂不支持查看，请点击链接查看！";
            }
        }

        final String html = StringUtil.replace(
                "<h1 style=\"color: blue;\">评论博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
                blog.getTitle(), contentHtml);

        try {
            mailSender.sendHtmlByQueue(String.format("TMS-博文评论_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                    TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser, DATE,
                            new Date(), HREF, href, TITLE, "下面博文评论涉及到你", CONTENT, html)),
                    getLoginUserName(loginUser), mail.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RespBody.succeed(comment2);
    }

    @RequestMapping(value = "comment/update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody updateComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
                                  @RequestParam("cid") Long cid, @RequestParam("version") Long version,
                                  @RequestParam(CONTENT) String content,
                                  @RequestParam(value = "contentHtml", required = false) String contentHtml,
                                  @RequestParam(value = "diff", required = false) String diff,
                                  @RequestParam(value = "users", required = false) String users) {

        Comment comment = commentRepository.findOne(cid);

        if (comment == null) {
            return RespBody.failed("修改博文评论不存在,可能已经被删除!");
        }

        if (!isSuperOrCreator(comment.getCreator().getUsername())) {
            return RespBody.failed("您没有权限编辑该博文评论!");
        }

        if (comment.getVersion() != version.longValue()) {
            return RespBody.failed("该博文评论已经被其他人更新,请刷新页面重新编辑提交!");
        }

        if (!hasAuth(Long.valueOf(comment.getTargetId()))) {
            return RespBody.failed("您没有权限编辑该博文评论!");
        }

        comment.setContent(content);

        logWithProperties(Action.Update, Target.Comment, cid, CONTENT,
                StringUtil.isEmpty(diff) ? StringUtil.EMPTY : diff, id);

        Comment comment2 = commentRepository.saveAndFlush(comment);

        final User loginUser = getLoginUser();

        final String href = basePath + BLOG_PATH + id + CID + comment2.getId();

        Blog blog = blogRepository.findOne(id);

        Mail mail = Mail.instance();
        mail.addUsers(Arrays.asList(blog.getCreator()), loginUser);

        // 博文更新通知优先级： @  > 我关注的 > 我的

        List<String> atUsers = Arrays.asList(StringUtil.isNotEmpty(users) ? users.split(",") : new String[0]);

        if (atUsers.size() > 0) {
            atUsers.forEach(username -> {
                User user = getUser(username);
                mail.addUsers(user);
            });
            wsSendToUsers(blog, comment2, Cmd.CAt, WebUtil.getUsername(), atUsers.toArray(new String[0]));
        }

        List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
        mail.addUsers(followers.stream().map(BlogFollower::getCreator).collect(Collectors.toList()), loginUser);

        List<String> fs = followers.stream().map(f -> f.getCreator().getUsername()).filter(f -> !atUsers.contains(f))
                .collect(Collectors.toList());
        wsSendToUsers(blog, comment2, Cmd.FCU, WebUtil.getUsername(), fs.toArray(new String[0]));

        String bCreator = blog.getCreator().getUsername();
        if (!blog.getCreator().equals(loginUser) && !atUsers.contains(bCreator) && !fs.contains(bCreator)) {
            wsSendToUsers(blog, comment2, Cmd.CU, WebUtil.getUsername(), bCreator);
        }

        if (StringUtil.isEmpty(contentHtml)) {
            if (Editor.Html.equals(comment2.getEditor())) {
                contentHtml = comment.getContent();
            } else {
                contentHtml = "内容暂不支持查看，请点击链接查看！";
            }
        }

        final String html = StringUtil.replace(
                "<h1 style=\"color: blue;\">评论博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
                blog.getTitle(), contentHtml);

        try {
            mailSender.sendHtmlByQueue(String.format("TMS-博文评论更新_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                    TemplateUtil.process(TEMPLATES_MAIL_MAIL_DYNAMIC, MapUtil.objArr2Map(USER, loginUser, DATE,
                            new Date(), HREF, href, TITLE, "下面更新博文评论涉及到你", CONTENT, html)),
                    getLoginUserName(loginUser), mail.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RespBody.succeed(comment2);
    }

    @RequestMapping(value = "comment/query", method = RequestMethod.GET)
    @ResponseBody
    public RespBody queryComment(@RequestParam("id") Long id,
                                 @PageableDefault(sort = {"id"}, direction = Direction.ASC) Pageable pageable) {

        if (!hasAuth(id)) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        Page<Comment> page = commentRepository.findByTargetIdAndStatusNot(String.valueOf(id), Status.Deleted, pageable);

        return RespBody.succeed(page);
    }

    @RequestMapping(value = "comment/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeComment(@RequestParam("cid") Long cid) {

        Comment comment = commentRepository.findOne(cid);
        if (comment != null) {

            if (!isSuperOrCreator(comment.getCreator().getUsername())) {
                return RespBody.failed("您没有权限删除该博文评论!");
            }

            fileService.removeFileByAtId(comment.getUuid());

            comment.setStatus(Status.Deleted);
            commentRepository.saveAndFlush(comment);

            log(Action.Delete, Target.Comment, cid, comment.getContent());
        }

        return RespBody.succeed(cid);
    }

    @RequestMapping(value = "comment/get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody getComment(@RequestParam("cid") Long cid) {

        Comment comment = commentRepository.findOne(cid);

        if (!hasAuth(Long.valueOf(comment.getTargetId()))) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        return RespBody.succeed(comment);
    }

    @RequestMapping(value = "space/update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody updateSpace(@RequestParam("id") Long id, @RequestParam(value = "sid", required = false) Long sid,
                                @RequestParam(value = "did", required = false) Long did,
                                @RequestParam(value = "pid", required = false) Long pid) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername()) && (blog.getSpace() == null
                || (!blog.getSpace().getCreator().getUsername().equals(WebUtil.getUsername())))) {
            return RespBody.failed("您没有权限修改该博文从属空间!");
        }

        Space space = sid != null ? spaceRepository.findOne(sid) : null;

        Dir dir = did != null ? dirRepository.findOne(did) : null;

        blogRepository.updateSpaceAndDirAndPid(space, dir, pid, id);

        updateSpaceAndDir(space, dir, id);

        Long pidOld = blog.getPid();
        if (pidOld != null) {
            long cnt = blogRepository.countByPid(pidOld);
            blogRepository.updateHasChild(cnt > 0, pidOld);
        }

        String val = StringUtil.EMPTY;

        if (space != null && dir != null) {
            val = space.getName() + " / " + dir.getName();
        } else if (space != null) {
            val = space.getName();
        } else if (dir != null) {
            val = dir.getName();
        }

        logWithProperties(Action.Update, Target.Blog, id, "space", val, blog.getTitle());

        em.detach(blog);

        return RespBody.succeed(blogRepository.findOne(id));
    }

    private void updateSpaceAndDir(Space space, Dir dir, Long id) {

        List<Blog> blogs = blogRepository.findByPidAndStatusNot(id, Status.Deleted);
        blogs.forEach(blog -> {
            blogRepository.updateSpaceAndDir(space, dir, blog.getId());
            updateSpaceAndDir(space, dir, blog.getId());
        });

    }

    @RequestMapping(value = "privated/update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody updatePrivated(@RequestParam("id") Long id, @RequestParam("privated") Boolean privated) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限修改该博文可见性!");
        }

        blogRepository.updatePrivatedAndOpened(privated, (Boolean.TRUE.equals(privated) ? Boolean.FALSE : blog.getOpened()), id);

        logWithProperties(Action.Update, Target.Blog, id, "privated", privated, blog.getTitle());

        em.detach(blog);

        return RespBody.succeed(blogRepository.findOne(id));
    }

    @RequestMapping(value = "file/permission/update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody updateFilePermission(@RequestParam("id") Long id, @RequestParam("readonly") Boolean readonly) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限修改该博文文件附件权限!");
        }

        blogRepository.updateFileReadonly(readonly, id);

        logWithProperties(Action.Update, Target.Blog, id, "fileReadonly", readonly, blog.getTitle());

        em.detach(blog);

        return RespBody.succeed(blogRepository.findOne(id));
    }

    @RequestMapping(value = "opened/update", method = RequestMethod.POST)
    @ResponseBody
    public RespBody updateOpened(@RequestParam("id") Long id, @RequestParam("opened") Boolean opened) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限修改该博文的可见性!");
        }

        blogRepository.updatePrivatedAndOpened((Boolean.TRUE.equals(opened) ? Boolean.FALSE : blog.getPrivated()), opened, id);

        logWithProperties(Action.Update, Target.Blog, id, "opened", opened, blog.getTitle());

        em.detach(blog);

        return RespBody.succeed(blogRepository.findOne(id));
    }

    @RequestMapping(value = "history/list", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listHistory(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限查看该博文历史列表!");
        }

        List<BlogHistory> blogHistories = blogHistoryRepository.findByBlogAndStatusNot(blog, Status.Deleted);

        return RespBody.succeed(blogHistories);
    }

    @RequestMapping(value = "history/get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody getHistory(@RequestParam("hid") Long hid) {

        BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
        Blog blog = blogHistory.getBlog();

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限查看该博文历史!");
        }

        return RespBody.succeed(blogHistory);
    }

    @RequestMapping(value = "history/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeHistory(@RequestParam("hid") Long hid) {

        BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
        Blog blog = blogHistory.getBlog();

        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限删除该博文历史!");
        }

        blogHistory.setStatus(Status.Deleted);

        blogHistoryRepository.saveAndFlush(blogHistory);

        return RespBody.succeed(hid);
    }

    @RequestMapping(value = "history/restore", method = RequestMethod.POST)
    @ResponseBody
    public RespBody restoreHistory(@RequestParam("hid") Long hid) {

        BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
        Blog blog = blogHistory.getBlog();

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限查看该博文历史!");
        }

        boolean isOpenEdit = Boolean.TRUE.equals(blog.getOpenEdit());
        if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
            return RespBody.failed("您没有权限还原该博文历史!");
        }

        if (isOpenEdit && !hasAuth(blog)) {
            return RespBody.failed("您没有权限还原该博文历史!");
        }

        BlogHistory blogHistory2 = new BlogHistory();
        blogHistory2.setBlog(blog);
        blogHistory2.setTitle(blog.getTitle());
        blogHistory2.setContent(blog.getContent());
        blogHistory2.setBlogUpdater(blog.getUpdater());
        blogHistory2.setBlogUpdateDate(blog.getUpdateDate());
        blogHistory2.setEditor(blog.getEditor());

        blogHistoryRepository.saveAndFlush(blogHistory2);

        blog.setTitle(blogHistory.getTitle());
        blog.setContent(blogHistory.getContent());
        if (blogHistory.getEditor() != null) {
            blog.setEditor(blogHistory.getEditor());
        }

        Blog blog2 = blogRepository.saveAndFlush(blog);

        return RespBody.succeed(blog2);
    }

    @RequestMapping(value = "download/{id}", method = RequestMethod.GET)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id,
                         @RequestParam(value = "type", defaultValue = "pdf") String type) {

        logger.debug("download blog start...");

        Blog blog = blogRepository.findOne(id);
        if (blog == null) {
            try {
                response.sendError(404, "下载博文不存在!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!hasAuth(blog)) {
            try {
                response.sendError(404, "您没有权限下载该博文!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
        String path = WebUtil.getRealPath(request);

        String blogUpdateDate = DateUtil.format(blog.getUpdateDate(), DateUtil.FORMAT9);

        String exportFilePath = null;
        String mdFilePath = null;
        String pdfFilePath = null;
        String md2htmlFilePath = null;
        File fileMd = null;
        File filePdf = null;
        File exportFile = null;

        boolean matchType = "mind".equalsIgnoreCase(type) || "table".equalsIgnoreCase(type) || "sheet".equalsIgnoreCase(type);

        if (matchType) {
            String exportFileName = blog.getId() + "_" + blogUpdateDate + "." + type;
            exportFilePath = path + uploadPath + exportFileName;

            exportFile = new File(exportFilePath);

            if (!exportFile.exists()) {
                try {

                    String content = blog.getContent();

                    FileUtils.writeStringToFile(exportFile, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if ("xlsx".equalsIgnoreCase(type)) {
            String exportFileName = blog.getId() + "_" + blogUpdateDate + "." + type;
            String exportDir = path + uploadPath;
            exportFilePath = exportDir + exportFileName;

            exportFile = new File(exportFilePath);

            if (!exportFile.exists()) {
                try {
                    String content = blog.getContent();
                    LuckySheetUtil.exportLuckySheetXlsxByPOI(exportDir, exportFileName, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            String mdFileName = blog.getId() + "_" + blogUpdateDate + ".md";
            String pdfFileName = blog.getId() + "_" + blogUpdateDate + ".pdf";
            String md2htmlFileName = blog.getId() + "_" + blogUpdateDate + ".html";

            mdFilePath = path + uploadPath + mdFileName;
            pdfFilePath = path + uploadPath + pdfFileName;
            md2htmlFilePath = path + uploadPath + md2htmlFileName;

            fileMd = new File(mdFilePath);

            if (!fileMd.exists()) {
                try {

                    String content = StringUtil.EMPTY;

                    if (Editor.Html.equals(blog.getEditor())) {
                        content = "<!DOCTYPE html><html><head><meta charset='utf-8' /><meta http-equiv='X-UA-Compatible' content='IE=edge' /><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no' /><meta content='tms,chat,wiki,translation,blog,markdown,group,team,teamwork,聊天,沟通,知识库,博文,国际化翻译,团队协作' name='Keywords'><meta content='TMS是免费开源的团队协作(团队沟通,博文知识库,国际化翻译i18n)web系统(响应式界面设计,移动端适配).' name='Description'></head><body><div class='markdown-body'><style>.markdown-body .tms-chat-msg-code-trigger{display: none;}.markdown-body{font-size:14px;line-height:1.6}.markdown-body>:first-child{margin-top:0!important}.markdown-body>:last-child{margin-bottom:0!important}.markdown-body a{word-break:break-all}.markdown-body a.absent{color:#C00}.markdown-body a.anchor{bottom:0;cursor:pointer;display:block;left:0;margin-left:-30px;padding-left:30px;position:absolute;top:0}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6{cursor:text;font-weight:700;margin:20px 0 10px;padding:0;position:relative;word-break:break-all;}.markdown-body h1 .mini-icon-link,.markdown-body h2 .mini-icon-link,.markdown-body h3 .mini-icon-link,.markdown-body h4 .mini-icon-link,.markdown-body h5 .mini-icon-link,.markdown-body h6 .mini-icon-link{color:#000;display:none}.markdown-body h1:hover a.anchor,.markdown-body h2:hover a.anchor,.markdown-body h3:hover a.anchor,.markdown-body h4:hover a.anchor,.markdown-body h5:hover a.anchor,.markdown-body h6:hover a.anchor{line-height:1;margin-left:-22px;padding-left:0;text-decoration:none;top:15%}.markdown-body h1:hover a.anchor .mini-icon-link,.markdown-body h2:hover a.anchor .mini-icon-link,.markdown-body h3:hover a.anchor .mini-icon-link,.markdown-body h4:hover a.anchor .mini-icon-link,.markdown-body h5:hover a.anchor .mini-icon-link,.markdown-body h6:hover a.anchor .mini-icon-link{display:inline-block}.markdown-body hr:after,.markdown-body hr:before{display:table;content:''}.markdown-body h1 code,.markdown-body h1 tt,.markdown-body h2 code,.markdown-body h2 tt,.markdown-body h3 code,.markdown-body h3 tt,.markdown-body h4 code,.markdown-body h4 tt,.markdown-body h5 code,.markdown-body h5 tt,.markdown-body h6 code,.markdown-body h6 tt{font-size:inherit}.markdown-body h1{color:#000;font-size:28px}.markdown-body h2{border-bottom:1px solid #CCC;color:#000;font-size:24px}.markdown-body h3{font-size:18px}.markdown-body h4{font-size:16px}.markdown-body h5{font-size:14px}.markdown-body h6{color:#777;font-size:14px}.markdown-body blockquote,.markdown-body dl,.markdown-body ol,.markdown-body p,.markdown-body pre,.markdown-body table,.markdown-body ul{margin:15px 0}.markdown-body hr{overflow:hidden;background:#e7e7e7;height:4px;padding:0;margin:16px 0;border:0;-moz-box-sizing:content-box;box-sizing:content-box}.markdown-body h1+p,.markdown-body h2+p,.markdown-body h3+p,.markdown-body h4+p,.markdown-body h5+p,.markdown-body h6+p,.markdown-body ol li>:first-child,.markdown-body ul li>:first-child{margin-top:0}.markdown-body hr:after{clear:both}.markdown-body a:first-child h1,.markdown-body a:first-child h2,.markdown-body a:first-child h3,.markdown-body a:first-child h4,.markdown-body a:first-child h5,.markdown-body a:first-child h6,.markdown-body>h1:first-child,.markdown-body>h1:first-child+h2,.markdown-body>h2:first-child,.markdown-body>h3:first-child,.markdown-body>h4:first-child,.markdown-body>h5:first-child,.markdown-body>h6:first-child{margin-top:0;padding-top:0}.markdown-body li p.first{display:inline-block}.markdown-body ol,.markdown-body ul{padding-left:30px}.markdown-body ol.no-list,.markdown-body ul.no-list{list-style-type:none;padding:0}.markdown-body ol ol,.markdown-body ol ul,.markdown-body ul ol,.markdown-body ul ul{margin-bottom:0}.markdown-body dl{padding:0}.markdown-body dl dt{font-size:14px;font-style:italic;font-weight:700;margin:15px 0 5px;padding:0}.markdown-body dl dt:first-child{padding:0}.markdown-body dl dt>:first-child{margin-top:0}.markdown-body dl dt>:last-child{margin-bottom:0}.markdown-body dl dd{margin:0 0 15px;padding:0 15px}.markdown-body blockquote>:first-child,.markdown-body dl dd>:first-child{margin-top:0}.markdown-body blockquote>:last-child,.markdown-body dl dd>:last-child{margin-bottom:0}.markdown-body blockquote{border-left:4px solid #DDD;color:#777;padding:0 15px}.markdown-body table{border-collapse:collapse}.markdown-body table th{font-weight:700}.markdown-body table td,.markdown-body table th{border:1px solid #CCC;padding:6px 13px}.markdown-body table tr{background-color:#FFF;border-top:1px solid #CCC}.markdown-body table tr:nth-child(2n){background-color:#F8F8F8}.markdown-body img{max-width:100%}.markdown-body span.frame{display:block;overflow:hidden}.markdown-body span.frame>span{border:1px solid #DDD;display:block;float:left;margin:13px 0 0;overflow:hidden;padding:7px;width:auto}.markdown-body span.frame span img{display:block;float:left}.markdown-body span.frame span span{clear:both;color:#333;display:block;padding:5px 0 0}.markdown-body span.align-center{clear:both;display:block;overflow:hidden}.markdown-body span.align-center>span{display:block;margin:13px auto 0;overflow:hidden;text-align:center}.markdown-body span.align-center span img{margin:0 auto;text-align:center}.markdown-body span.align-right{clear:both;display:block;overflow:hidden}.markdown-body span.align-right>span{display:block;margin:13px 0 0;overflow:hidden;text-align:right}.markdown-body span.align-right span img{margin:0;text-align:right}.markdown-body span.float-left{display:block;float:left;margin-right:13px;overflow:hidden}.markdown-body span.float-left span{margin:13px 0 0}.markdown-body span.float-right{display:block;float:right;margin-left:13px;overflow:hidden}.markdown-body span.float-right>span{display:block;margin:13px auto 0;overflow:hidden;text-align:right}.markdown-body code,.markdown-body tt{background-color:#F8F8F8;border:1px solid #EAEAEA;border-radius:3px;margin:0 2px;padding:0 5px;white-space:normal}.markdown-body pre>code{background:none;border:none;margin:0;padding:0;white-space:pre}.markdown-body .highlight pre,.markdown-body pre{background-color:#F8F8F8;border:1px solid #CCC;border-radius:3px;font-size:13px;line-height:19px;overflow:auto;padding:6px 10px}.markdown-body pre code,.markdown-body pre tt{background-color:transparent;border:none}.markdown-body .emoji{width:1.5em;height:1.5em;display:inline-block;margin-bottom:-.25em;background-size:contain;}</style>"
                                + blog.getContent() + "</div></body></html>";
                    } else {
                        content = blog.getContent();
                    }

                    FileUtils.writeStringToFile(fileMd, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            filePdf = new File(pdfFilePath);

            if (!filePdf.exists()) {

                try {

                    String pathNode = StringUtil.isNotEmpty(md2pdfPath) ? md2pdfPath
                            : new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();

                    String node = StringUtil.isNotEmpty(nodePath) ? nodePath : "node";
                    String nodeCmd = StringUtil.replace(node + " {?1} {?2} {?3}", pathNode, mdFilePath, pdfFilePath);
                    logger.info("Node CMD: {}", nodeCmd);
                    Process process = Runtime.getRuntime().exec(nodeCmd);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String s = null;
                    while ((s = bufferedReader.readLine()) != null) {
                        logger.info(s);
                    }
                    process.waitFor();
                    logger.info("Md2pdf done!");
                } catch (IOException | InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("application/x-msdownload;");
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        String dnFileName = null;
        String dnFileLength = null;
        File dnFile = null;
        if ("md".equalsIgnoreCase(type) || "html".equalsIgnoreCase(type)) { // download markdown or html
            if (fileMd == null) {
                throw new BizException(ERR_FILE_NOT_EXISTS);
            }
            dnFileName = blog.getTitle().trim() + "." + type;
            dnFileLength = String.valueOf(fileMd.length());
            dnFile = fileMd;
        } else if ("md2html".equalsIgnoreCase(type)) { // download markdown as html
            File md2fileHtml = new File(md2htmlFilePath);
            dnFileName = blog.getTitle().trim() + ".html";
            dnFileLength = String.valueOf(md2fileHtml.length());
            dnFile = md2fileHtml;
        } else {
            if (matchType || "xlsx".equalsIgnoreCase(type)) { // download mind or excel
                if (exportFile == null) {
                    throw new BizException(ERR_FILE_NOT_EXISTS);
                }
                dnFileName = blog.getTitle().trim() + "." + type;
                dnFileLength = String.valueOf(exportFile.length());
                dnFile = exportFile;
            } else { // download pdf
                if (filePdf == null) {
                    throw new BizException(ERR_FILE_NOT_EXISTS);
                }
                dnFileName = blog.getTitle().trim() + ".pdf";
                dnFileLength = String.valueOf(filePdf.length());
                dnFile = filePdf;
            }
        }
        // 2.设置文件头：最后一个参数是设置下载文件名
        response.setHeader("Content-Disposition", "attachment; fileName=" + StringUtil.encodingFileName(dnFileName));
        response.setHeader("Content-Length", dnFileLength);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dnFile));
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "comment/download/{id}", method = RequestMethod.GET)
    public void downloadComment(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id,
                                @RequestParam(value = "type", defaultValue = "pdf") String type) {

        logger.debug("download blog comment start...");

        Comment comment = commentRepository.findOne(id);
        if (comment == null) {
            try {
                response.sendError(404, "下载博文评论不存在!");
                return;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            }
        }

        Blog blog = blogRepository.findOne(Long.valueOf(comment.getTargetId()));

        if (!hasAuth(blog)) {
            try {
                response.sendError(404, "您没有权限下载该博文评论!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
        String path = WebUtil.getRealPath(request);

        String commentUpdateDate = DateUtil.format(comment.getUpdateDate(), DateUtil.FORMAT9);

        String exportFilePath = null;
        String mdFilePath = null;
        String pdfFilePath = null;
        String md2htmlFilePath = null;
        File fileMd = null;
        File filePdf = null;
        File exportFile = null;

        boolean matchType = "mind".equalsIgnoreCase(type) || "table".equalsIgnoreCase(type) || "sheet".equalsIgnoreCase(type);

        if (matchType) {
            String exportFileName = comment.getId() + "_" + commentUpdateDate + "." + type;
            exportFilePath = path + uploadPath + exportFileName;

            exportFile = new File(exportFilePath);

            if (!exportFile.exists()) {
                try {

                    String content = comment.getContent();

                    FileUtils.writeStringToFile(exportFile, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if ("xlsx".equalsIgnoreCase(type)) {
            String exportFileName = comment.getId() + "_" + commentUpdateDate + "." + type;
            String exportDir = path + uploadPath;
            exportFilePath = exportDir + exportFileName;

            exportFile = new File(exportFilePath);

            if (!exportFile.exists()) {
                try {
                    String content = blog.getContent();
                    LuckySheetUtil.exportLuckySheetXlsxByPOI(exportDir, exportFileName, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            String mdFileName = comment.getId() + "_" + commentUpdateDate + ".md";
            String pdfFileName = comment.getId() + "_" + commentUpdateDate + ".pdf";
            String md2htmlFileName = comment.getId() + "_" + commentUpdateDate + ".html";

            mdFilePath = path + uploadPath + mdFileName;
            pdfFilePath = path + uploadPath + pdfFileName;
            md2htmlFilePath = path + uploadPath + md2htmlFileName;

            fileMd = new File(mdFilePath);

            if (!fileMd.exists()) {
                try {

                    String content = StringUtil.EMPTY;

                    if (Editor.Html.equals(comment.getEditor())) {
                        content = "<div class='markdown-body'><style>.markdown-body .tms-chat-msg-code-trigger{display: none;}.markdown-body{font-size:14px;line-height:1.6}.markdown-body>:first-child{margin-top:0!important}.markdown-body>:last-child{margin-bottom:0!important}.markdown-body a{word-break:break-all}.markdown-body a.absent{color:#C00}.markdown-body a.anchor{bottom:0;cursor:pointer;display:block;left:0;margin-left:-30px;padding-left:30px;position:absolute;top:0}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6{cursor:text;font-weight:700;margin:20px 0 10px;padding:0;position:relative;word-break:break-all;}.markdown-body h1 .mini-icon-link,.markdown-body h2 .mini-icon-link,.markdown-body h3 .mini-icon-link,.markdown-body h4 .mini-icon-link,.markdown-body h5 .mini-icon-link,.markdown-body h6 .mini-icon-link{color:#000;display:none}.markdown-body h1:hover a.anchor,.markdown-body h2:hover a.anchor,.markdown-body h3:hover a.anchor,.markdown-body h4:hover a.anchor,.markdown-body h5:hover a.anchor,.markdown-body h6:hover a.anchor{line-height:1;margin-left:-22px;padding-left:0;text-decoration:none;top:15%}.markdown-body h1:hover a.anchor .mini-icon-link,.markdown-body h2:hover a.anchor .mini-icon-link,.markdown-body h3:hover a.anchor .mini-icon-link,.markdown-body h4:hover a.anchor .mini-icon-link,.markdown-body h5:hover a.anchor .mini-icon-link,.markdown-body h6:hover a.anchor .mini-icon-link{display:inline-block}.markdown-body hr:after,.markdown-body hr:before{display:table;content:''}.markdown-body h1 code,.markdown-body h1 tt,.markdown-body h2 code,.markdown-body h2 tt,.markdown-body h3 code,.markdown-body h3 tt,.markdown-body h4 code,.markdown-body h4 tt,.markdown-body h5 code,.markdown-body h5 tt,.markdown-body h6 code,.markdown-body h6 tt{font-size:inherit}.markdown-body h1{color:#000;font-size:28px}.markdown-body h2{border-bottom:1px solid #CCC;color:#000;font-size:24px}.markdown-body h3{font-size:18px}.markdown-body h4{font-size:16px}.markdown-body h5{font-size:14px}.markdown-body h6{color:#777;font-size:14px}.markdown-body blockquote,.markdown-body dl,.markdown-body ol,.markdown-body p,.markdown-body pre,.markdown-body table,.markdown-body ul{margin:15px 0}.markdown-body hr{overflow:hidden;background:#e7e7e7;height:4px;padding:0;margin:16px 0;border:0;-moz-box-sizing:content-box;box-sizing:content-box}.markdown-body h1+p,.markdown-body h2+p,.markdown-body h3+p,.markdown-body h4+p,.markdown-body h5+p,.markdown-body h6+p,.markdown-body ol li>:first-child,.markdown-body ul li>:first-child{margin-top:0}.markdown-body hr:after{clear:both}.markdown-body a:first-child h1,.markdown-body a:first-child h2,.markdown-body a:first-child h3,.markdown-body a:first-child h4,.markdown-body a:first-child h5,.markdown-body a:first-child h6,.markdown-body>h1:first-child,.markdown-body>h1:first-child+h2,.markdown-body>h2:first-child,.markdown-body>h3:first-child,.markdown-body>h4:first-child,.markdown-body>h5:first-child,.markdown-body>h6:first-child{margin-top:0;padding-top:0}.markdown-body li p.first{display:inline-block}.markdown-body ol,.markdown-body ul{padding-left:30px}.markdown-body ol.no-list,.markdown-body ul.no-list{list-style-type:none;padding:0}.markdown-body ol ol,.markdown-body ol ul,.markdown-body ul ol,.markdown-body ul ul{margin-bottom:0}.markdown-body dl{padding:0}.markdown-body dl dt{font-size:14px;font-style:italic;font-weight:700;margin:15px 0 5px;padding:0}.markdown-body dl dt:first-child{padding:0}.markdown-body dl dt>:first-child{margin-top:0}.markdown-body dl dt>:last-child{margin-bottom:0}.markdown-body dl dd{margin:0 0 15px;padding:0 15px}.markdown-body blockquote>:first-child,.markdown-body dl dd>:first-child{margin-top:0}.markdown-body blockquote>:last-child,.markdown-body dl dd>:last-child{margin-bottom:0}.markdown-body blockquote{border-left:4px solid #DDD;color:#777;padding:0 15px}.markdown-body table{border-collapse:collapse}.markdown-body table th{font-weight:700}.markdown-body table td,.markdown-body table th{border:1px solid #CCC;padding:6px 13px}.markdown-body table tr{background-color:#FFF;border-top:1px solid #CCC}.markdown-body table tr:nth-child(2n){background-color:#F8F8F8}.markdown-body img{max-width:100%}.markdown-body span.frame{display:block;overflow:hidden}.markdown-body span.frame>span{border:1px solid #DDD;display:block;float:left;margin:13px 0 0;overflow:hidden;padding:7px;width:auto}.markdown-body span.frame span img{display:block;float:left}.markdown-body span.frame span span{clear:both;color:#333;display:block;padding:5px 0 0}.markdown-body span.align-center{clear:both;display:block;overflow:hidden}.markdown-body span.align-center>span{display:block;margin:13px auto 0;overflow:hidden;text-align:center}.markdown-body span.align-center span img{margin:0 auto;text-align:center}.markdown-body span.align-right{clear:both;display:block;overflow:hidden}.markdown-body span.align-right>span{display:block;margin:13px 0 0;overflow:hidden;text-align:right}.markdown-body span.align-right span img{margin:0;text-align:right}.markdown-body span.float-left{display:block;float:left;margin-right:13px;overflow:hidden}.markdown-body span.float-left span{margin:13px 0 0}.markdown-body span.float-right{display:block;float:right;margin-left:13px;overflow:hidden}.markdown-body span.float-right>span{display:block;margin:13px auto 0;overflow:hidden;text-align:right}.markdown-body code,.markdown-body tt{background-color:#F8F8F8;border:1px solid #EAEAEA;border-radius:3px;margin:0 2px;padding:0 5px;white-space:normal}.markdown-body pre>code{background:none;border:none;margin:0;padding:0;white-space:pre}.markdown-body .highlight pre,.markdown-body pre{background-color:#F8F8F8;border:1px solid #CCC;border-radius:3px;font-size:13px;line-height:19px;overflow:auto;padding:6px 10px}.markdown-body pre code,.markdown-body pre tt{background-color:transparent;border:none}.markdown-body .emoji{width:1.5em;height:1.5em;display:inline-block;margin-bottom:-.25em;background-size:contain;}</style>"
                                + comment.getContent() + "</div>";
                    } else {
                        content = comment.getContent();
                    }

                    FileUtils.writeStringToFile(fileMd, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            filePdf = new File(pdfFilePath);

            if (!filePdf.exists()) {

                try {

                    String pathNode = StringUtil.isNotEmpty(md2pdfPath) ? md2pdfPath
                            : new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();

                    String node = StringUtil.isNotEmpty(nodePath) ? nodePath : "node";
                    String nodeCmd = StringUtil.replace(node + " {?1} {?2} {?3}", pathNode, mdFilePath, pdfFilePath);
                    logger.info("Node CMD: {}", nodeCmd);
                    Process process = Runtime.getRuntime().exec(nodeCmd);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String s = null;
                    while ((s = bufferedReader.readLine()) != null) {
                        logger.info(s);
                    }
                    process.waitFor();
                    logger.info("Md2pdf done!");
                } catch (IOException | InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("application/x-msdownload;");
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        String dnFileName = null;
        String dnFileLength = null;
        File dnFile = null;
        if ("md".equalsIgnoreCase(type) || "html".equalsIgnoreCase(type)) { // download markdown or html
            if (fileMd == null) {
                throw new BizException(ERR_FILE_NOT_EXISTS);
            }
            dnFileName = blog.getTitle().trim() + "_评论_" + comment.getId() + "." + type;
            dnFileLength = String.valueOf(fileMd.length());
            dnFile = fileMd;
        } else if ("md2html".equalsIgnoreCase(type)) { // download markdown as html
            File md2fileHtml = new File(md2htmlFilePath);
            dnFileName = blog.getTitle().trim() + "_评论_" + comment.getId() + ".html";
            dnFileLength = String.valueOf(md2fileHtml.length());
            dnFile = md2fileHtml;
        } else if (matchType || "xlsx".equalsIgnoreCase(type)) { // download mind or excel
            if (exportFile == null) {
                throw new BizException(ERR_FILE_NOT_EXISTS);
            }
            dnFileName = blog.getTitle().trim() + "_评论_" + comment.getId() + "." + type;
            dnFileLength = String.valueOf(exportFile.length());
            dnFile = exportFile;
        } else { // download pdf
            if (filePdf == null) {
                throw new BizException(ERR_FILE_NOT_EXISTS);
            }
            dnFileName = blog.getTitle().trim() + "_评论_" + comment.getId() + ".pdf";
            dnFileLength = String.valueOf(filePdf.length());
            dnFile = filePdf;
        }
        // 2.设置文件头：最后一个参数是设置下载文件名
        response.setHeader("Content-Disposition", "attachment; fileName=" + StringUtil.encodingFileName(dnFileName));
        response.setHeader("Content-Length", dnFileLength);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dnFile));
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("download/md2html/{id}")
    @ResponseBody
    public RespBody downloadHtmlFromMd(HttpServletRequest request, @PathVariable Long id,
                                       @RequestParam(value = CONTENT) String content) {

        logger.debug("download blog md2html start...");

        Blog blog = blogRepository.findOne(id);
        if (blog == null) {
            return RespBody.failed("下载博文不存在!");
        }

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限下载该博文!");
        }

        // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
        String path = WebUtil.getRealPath(request);

        String blogUpdateDate = DateUtil.format(blog.getUpdateDate(), DateUtil.FORMAT9);

        String md2htmlFileName = blog.getId() + "_" + blogUpdateDate + ".html";

        String md2htmlFilePath = path + uploadPath + md2htmlFileName;

        File md2fileHtml = new File(md2htmlFilePath);

        if (!md2fileHtml.exists()) {
            try {
                FileUtils.writeStringToFile(md2fileHtml, content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                return RespBody.failed(e.getMessage());
            }
        }

        return RespBody.succeed();
    }

    @PostMapping("comment/download/md2html/{id}")
    @ResponseBody
    public RespBody downloadCommentHtmlFromMd(HttpServletRequest request, @PathVariable Long id,
                                              @RequestParam(value = CONTENT) String content) {

        logger.debug("download blog comment md2html start...");

        Comment comment = commentRepository.findOne(id);

        if (comment == null) {
            return RespBody.failed("下载博文评论不存在!");
        }

        Blog blog = blogRepository.findOne(Long.valueOf(comment.getTargetId()));

        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限下载该博文评论!");
        }

        // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
        String path = WebUtil.getRealPath(request);

        String blogUpdateDate = DateUtil.format(comment.getUpdateDate(), DateUtil.FORMAT9);

        String md2htmlFileName = comment.getId() + "_" + blogUpdateDate + ".html";

        String md2htmlFilePath = path + uploadPath + md2htmlFileName;

        File md2fileHtml = new File(md2htmlFilePath);

        if (!md2fileHtml.exists()) {
            try {
                FileUtils.writeStringToFile(md2fileHtml, content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                return RespBody.failed(e.getMessage());
            }
        }

        return RespBody.succeed();
    }

    private boolean hasAuth(Blog b) {

        if (b == null) {
            return false;
        }

        if (b.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
            return false;
        }

        return hasAuthWithDeleted(b);
    }

    private boolean hasAuth(Long id) {

        if (id == null) {
            return false;
        }

        Blog b = blogRepository.findOne(id);

        if (b == null) {
            return false;
        }

        if (b.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
            return false;
        }

        return hasAuthWithDeleted(b);
    }

    private boolean hasSpaceAuth(Space s) {

        if (s == null) {
            return false;
        }

        if (isSuper()) { // 超级用户
            return true;
        }

        if (s.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
            return false;
        }

        if (Boolean.TRUE.equals(s.getOpened())) {
            return true;
        }

        User loginUser = new User(WebUtil.getUsername());

        // 过滤掉没有权限的
        if (s.getCreator().equals(loginUser)) { // 我创建的
            return true;
        }

        if (Boolean.FALSE.equals(s.getPrivated())) { // 非私有的
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

    private boolean hasAuthWithDeleted(Blog b) {

        if (b == null) {
            return false;
        }

        if (isSuper()) { // 超级用户
            return true;
        }

        User loginUser = new User(WebUtil.getUsername());

        // 过滤掉没有权限的
        if (b.getCreator().equals(loginUser)) { // 我创建的
            return true;
        }

        if (Boolean.TRUE.equals(b.getOpened())) {
            return true;
        }

        if (Boolean.FALSE.equals(b.getPrivated())) { // 非私有的
            if (b.getSpace() == null) {
                return true;
            } else {
                return hasSpaceAuth(b.getSpace());
            }
        }

        boolean exists = false;
        for (BlogAuthority ba : b.getBlogAuthorities()) {
            if (loginUser.equals(ba.getUser())) {
                exists = true;
                break;
            } else {
                Channel channel = ba.getChannel();
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

    @RequestMapping(value = "auth/get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody getAuth(@RequestParam("id") Long id) {
        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限查看该博文权限!");
        }
        return RespBody.succeed(blog.getBlogAuthorities());
    }

    @RequestMapping(value = "auth/add", method = RequestMethod.POST)
    @ResponseBody
    public RespBody addAuth(@RequestParam("id") Long id,
                            @RequestParam(value = "channels", required = false) String channels,
                            @RequestParam(value = "users", required = false) String users) {
        Blog blog = blogRepository.findOne(id);
        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限为该博文添加权限!");
        }

        List<BlogAuthority> blogAuthorities = new ArrayList<>();

        if (StringUtil.isNotEmpty(channels)) {
            Stream.of(channels.split(",")).forEach(c -> {
                Channel channel = channelRepository.findOne(Long.valueOf(c));

                if (channel != null) {
                    BlogAuthority blogAuthority = new BlogAuthority();
                    blogAuthority.setBlog(blog);
                    blogAuthority.setChannel(channel);
                    blogAuthorities.add(blogAuthority);
                }

            });
        }

        if (StringUtil.isNotEmpty(users)) {
            Stream.of(users.split(",")).forEach(u -> {
                User user = userRepository.findOne(u);
                if (user != null) {
                    BlogAuthority blogAuthority = new BlogAuthority();
                    blogAuthority.setBlog(blog);
                    blogAuthority.setUser(user);
                    blogAuthorities.add(blogAuthority);
                }

            });
        }

        List<BlogAuthority> list = blogAuthorityRepository.save(blogAuthorities);
        blogAuthorityRepository.flush();

        blog.getBlogAuthorities().addAll(list);

        return RespBody.succeed(blog);
    }

    @RequestMapping(value = "auth/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeAuth(@RequestParam("id") Long id,
                               @RequestParam(value = "channels", required = false) String channels,
                               @RequestParam(value = "users", required = false) String users) {
        Blog blog = blogRepository.findOne(id);
        if (!isSuperOrCreator(blog.getCreator().getUsername())) {
            return RespBody.failed("您没有权限为该博文移除权限!");
        }

        List<BlogAuthority> list = new ArrayList<>();
        Collection<Channel> channelC = new ArrayList<>();
        if (StringUtil.isNotEmpty(channels)) {
            Stream.of(channels.split(",")).forEach(c -> {
                Channel ch = new Channel();
                ch.setId(Long.valueOf(c));
                channelC.add(ch);

                BlogAuthority ba = new BlogAuthority();
                ba.setBlog(blog);
                ba.setChannel(ch);
                list.add(ba);
            });
        }
        Collection<User> userC = new ArrayList<>();
        if (StringUtil.isNotEmpty(users)) {
            Stream.of(users.split(",")).forEach(u -> {
                User user = new User();
                user.setUsername(u);
                userC.add(user);

                BlogAuthority ba = new BlogAuthority();
                ba.setBlog(blog);
                ba.setUser(user);
                list.add(ba);
            });
        }

        if (channelC.size() > 0 && userC.size() > 0) {
            blogAuthorityRepository.removeAuths(blog, channelC, userC);
        } else {
            if (channelC.size() > 0) {
                blogAuthorityRepository.removeChannelAuths(blog, channelC);
            } else if (userC.size() > 0) {
                blogAuthorityRepository.removeUserAuths(blog, userC);
            }
        }

        blogAuthorityRepository.flush();

        blog.getBlogAuthorities().removeAll(list);

        return RespBody.succeed(blog);
    }

    @RequestMapping(value = "stow/add", method = RequestMethod.POST)
    @ResponseBody
    public RespBody addStow(@RequestParam("id") Long id) {
        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限收藏该博文!");
        }

        User loginUser = getLoginUser();

        BlogStow blogStow3 = blogStowRepository.findOneByBlogAndCreator(blog, loginUser);

        if (blogStow3 != null) {
            if (!blogStow3.getStatus().equals(Status.Deleted)) {
                return RespBody.failed("您已经收藏过该博文!");
            } else {
                blogStow3.setStatus(Status.New);

                BlogStow blogStow = blogStowRepository.saveAndFlush(blogStow3);

                logWithProperties(Action.Update, Target.Blog, id, "stow", blog.getTitle());

                return RespBody.succeed(blogStow);
            }
        } else {
            BlogStow blogStow = new BlogStow();
            blogStow.setBlog(blog);

            BlogStow blogStow2 = blogStowRepository.saveAndFlush(blogStow);

            logWithProperties(Action.Update, Target.Blog, id, "stow", blog.getTitle());

            return RespBody.succeed(blogStow2);
        }

    }

    @RequestMapping(value = "stow/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeStow(@RequestParam("sid") Long sid) {

        BlogStow blogStow = blogStowRepository.findOne(sid);

        if (blogStow == null) {
            return RespBody.failed("该博文收藏记录不存在!");
        }

        if (!hasAuth(blogStow.getBlog())) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        blogStow.setStatus(Status.Deleted);

        blogStowRepository.saveAndFlush(blogStow);

        return RespBody.succeed(sid);
    }

    @RequestMapping(value = "stow/listMy", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMyStow() {

        List<BlogStow> blogStows = blogStowRepository.findByCreatorAndStatusNot(getLoginUser(), Status.Deleted);
        blogStows = blogStows.stream().filter(bs -> !bs.getBlog().getStatus().equals(Status.Deleted))
                .collect(Collectors.toList());
        blogStows.forEach(bs -> {
            bs.getBlog().setContent(null);
            bs.getBlog().setBlogAuthorities(null);
        });

        return RespBody.succeed(blogStows);
    }

    @RequestMapping(value = "stow/get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody getStow(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        BlogStow blogStow = blogStowRepository.findOneByBlogAndCreatorAndStatusNot(blog, getLoginUser(),
                Status.Deleted);

        return RespBody.succeed(blogStow);
    }

    @RequestMapping(value = "stow/list", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listStow(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        List<BlogStow> stows = blogStowRepository.findByBlogAndStatusNot(blog, Status.Deleted);
        stows.forEach(bs -> bs.setBlog(null));

        return RespBody.succeed(stows);
    }

    @RequestMapping(value = "follower/add", method = RequestMethod.POST)
    @ResponseBody
    public RespBody addFollower(@RequestParam("id") Long id) {
        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限关注该博文!");
        }

        User loginUser = getLoginUser();

        BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreator(blog, loginUser);

        if (blogFollower != null) {
            if (!blogFollower.getStatus().equals(Status.Deleted)) {
                return RespBody.failed("您已经关注过该博文!");
            } else {
                blogFollower.setStatus(Status.New);

                BlogFollower blogFollower2 = blogFollowerRepository.saveAndFlush(blogFollower);

                logWithProperties(Action.Update, Target.Blog, id, FOLLOWER, blog.getTitle());

                return RespBody.succeed(blogFollower2);
            }
        } else {
            BlogFollower blogFollower2 = new BlogFollower();
            blogFollower2.setBlog(blog);

            BlogFollower blogFollower3 = blogFollowerRepository.saveAndFlush(blogFollower2);

            logWithProperties(Action.Update, Target.Blog, id, FOLLOWER, blog.getTitle());

            return RespBody.succeed(blogFollower3);
        }

    }

    @RequestMapping(value = "follower/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeFollower(@RequestParam("fid") Long fid) {

        BlogFollower blogFollower = blogFollowerRepository.findOne(fid);

        if (blogFollower == null) {
            return RespBody.failed("该博文关注记录不存在!");
        }

        if (!hasAuth(blogFollower.getBlog())) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        blogFollower.setStatus(Status.Deleted);

        blogFollowerRepository.saveAndFlush(blogFollower);

        return RespBody.succeed(fid);
    }

    @RequestMapping(value = "follower/listMy", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listMyFollower() {

        List<BlogFollower> blogFollowers = blogFollowerRepository.findByCreatorAndStatusNot(getLoginUser(),
                Status.Deleted);
        blogFollowers.forEach(bf -> {
            bf.getBlog().setContent(null);
            bf.getBlog().setBlogAuthorities(null);
        });

        return RespBody.succeed(blogFollowers);
    }

    @RequestMapping(value = "follower/get", method = RequestMethod.GET)
    @ResponseBody
    public RespBody getFollower(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreatorAndStatusNot(blog, getLoginUser(),
                Status.Deleted);

        return RespBody.succeed(blogFollower);
    }

    @RequestMapping(value = "follower/list", method = RequestMethod.GET)
    @ResponseBody
    public RespBody listFollower(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
        followers.forEach(bf -> bf.setBlog(null));

        return RespBody.succeed(followers);
    }

    @RequestMapping(value = "poll", method = RequestMethod.GET)
    @ResponseBody
    public RespBody poll(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);
        if (!hasAuth(blog)) {
            return RespBody.failed("您没有权限操作该博文!");
        }

        return RespBody.succeed(PollBlog.builder().version(blog.getVersion()).build());
    }

    @RequestMapping(value = "log/my", method = RequestMethod.GET)
    @ResponseBody
    public RespBody myLog() {

        List<Log> logs = logRepository.findByTargetInAndCreateDateAfter(Arrays.asList(Target.Blog, Target.Comment),
                new DateTime().minusDays(7).toDate());

        if (logs.size() == 0) {
            return RespBody.succeed(getLogs(null));
        }

        Collections.reverse(logs);

        Log last = logs.get(logs.size() - 1);

        logs = logs.stream().filter(lg -> {

            String targetId = lg.getTargetId();
            if (Target.Blog.equals(lg.getTarget())) {
                Blog blog = blogRepository.findOne(Long.valueOf(targetId));
                return hasAuthWithDeleted(blog);
            } else if (Target.Comment.equals(lg.getTarget())) {
                Comment comment = commentRepository.findOne(Long.valueOf(targetId));
                Blog blog = blogRepository.findOne(Long.valueOf(comment.getTargetId()));
                return hasAuthWithDeleted(blog);
            }

            return false;
        }).limit(100).collect(Collectors.toList());

        if (logs.size() == 0) {
            logs = getLogs(last.getId());
        }

        return RespBody.succeed(logs);
    }

    private List<Log> getLogs(Long id) {

        List<Target> targets = Arrays.asList(Target.Blog, Target.Comment);
        List<Log> logs = null;
        if (id != null) {
            logs = logRepository.findTop50ByStatusNotAndTargetInAndIdLessThanOrderByIdDesc(Status.Deleted, targets, id);
        } else {
            logs = logRepository.findTop50ByStatusNotAndTargetInOrderByIdDesc(Status.Deleted, targets);
        }

        if (logs.size() == 0) {
            return new ArrayList<>();
        }

        Log last = logs.get(logs.size() - 1);

        logs = logs.stream().filter(lg -> {

            String targetId = lg.getTargetId();
            if (Target.Blog.equals(lg.getTarget())) {
                Blog blog = blogRepository.findOne(Long.valueOf(targetId));
                return hasAuthWithDeleted(blog);
            } else if (Target.Comment.equals(lg.getTarget())) {
                Comment comment = commentRepository.findOne(Long.valueOf(targetId));
                Blog blog = blogRepository.findOne(Long.valueOf(comment.getTargetId()));
                return hasAuthWithDeleted(blog);
            }

            return false;
        }).collect(Collectors.toList());

        if (logs.size() == 0) {
            return getLogs(last.getId());
        }

        return logs;
    }

    @RequestMapping(value = "log/my/more", method = RequestMethod.GET)
    @ResponseBody
    public RespBody myMoreLog(@RequestParam(value = "lastId", required = false) Long lastId) {

        return RespBody.succeed(getLogs(lastId));
    }

    @RequestMapping(value = "tag/add", method = RequestMethod.POST)
    @ResponseBody
    public RespBody addTag(@RequestParam("id") Long id, @RequestParam("tags") String tags) {

        Blog blog = blogRepository.findOne(id);
        boolean isOpenEdit = Boolean.TRUE.equals(blog.getOpenEdit());
        if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
            return RespBody.failed("您没有权限为该博文添加标签!");
        }

        User loginUser = getLoginUser();

        if (StringUtil.isNotEmpty(tags)) {
            Stream.of(tags.split(",")).forEach(t -> {
                Tag tag = tagRepository.findOneByNameAndCreator(t, loginUser);
                if (tag == null) {
                    Tag tag2 = new Tag();
                    tag2.setName(t);
                    tag2.getBlogs().add(blog);

                    tag = tagRepository.saveAndFlush(tag2);
                } else {
                    tag.getBlogs().add(blog);
                    tagRepository.saveAndFlush(tag);
                }

                blog.getTags().add(tag);

            });
        }

        return RespBody.succeed(blog);
    }

    @RequestMapping(value = "tag/remove", method = RequestMethod.POST)
    @ResponseBody
    public RespBody removeTag(@RequestParam("id") Long id, @RequestParam("tags") String tags) {

        Blog blog = blogRepository.findOne(id);
        boolean isOpenEdit = Boolean.TRUE.equals(blog.getOpenEdit());
        if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
            return RespBody.failed("您没有权限移除该博文的标签!");
        }

        User loginUser = getLoginUser();

        if (StringUtil.isNotEmpty(tags)) {
            Stream.of(tags.split(",")).forEach(t -> {
                Tag tag = tagRepository.findOneByNameAndCreator(t, loginUser);
                if (tag != null) {
                    tag.getBlogs().remove(blog);

                    tagRepository.saveAndFlush(tag);

                    blog.getTags().remove(tag);
                }

            });
        }

        return RespBody.succeed(blog);
    }

    @GetMapping("tag/my")
    @ResponseBody
    public RespBody myTag() {

        List<Tag> tags = tagRepository.findByCreator(getLoginUser());

        return RespBody.succeed(tags);
    }

    @PostMapping("dir/update")
    @ResponseBody
    public RespBody updateDir(@RequestParam("id") Long id, @RequestParam(value = "did", required = false) Long did) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null) {
            return RespBody.failed("对应博文不存在！");
        }

        if (!hasAuth(blog)) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        Dir dir = null;
        if (did != null) {
            dir = dirRepository.findOne(did);

            if (dir == null) {
                return RespBody.failed("对应分类不存在！");
            }
        }

        blog.setDir(dir);

        blogRepository.saveAndFlush(blog);

        return RespBody.succeed(blog);
    }

    @GetMapping("news/list")
    @ResponseBody
    public RespBody listNews(@PageableDefault(sort = {"id"}, direction = Direction.DESC) Pageable pageable) {

        Page<BlogNews> news = blogNewsRepository.findByToAndUsernameNotAndStatusNot(WebUtil.getUsername(),
                WebUtil.getUsername(), Status.Deleted, pageable);

        return RespBody.succeed(news);
    }

    @PostMapping("news/delete")
    @ResponseBody
    public RespBody deleteNews(@RequestParam("id") Long id) {

        BlogNews news = blogNewsRepository.findOne(id);

        if (!isSuperOrCreator(news.getTo())) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        news.setStatus(Status.Deleted);

        blogNewsRepository.saveAndFlush(news);

        messagingTemplate.convertAndSendToUser(news.getTo(), "/blog/toastr",
                ToastrPayload.builder().id(String.valueOf(news.getId())).build());

        return RespBody.succeed(id);

    }

    @PostMapping("share/create")
    @ResponseBody
    public RespBody createShare(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator())) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        String shareId = UUID.randomUUID().toString();

        int cnt = blogRepository.updateShareId(shareId, id);

        if (cnt == 1) {

            blog.setShareId(shareId);

            return RespBody.succeed(blog);
        }

        return RespBody.failed();

    }

    @PostMapping("share/remove")
    @ResponseBody
    public RespBody removeShare(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator())) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        int cnt = blogRepository.updateShareId(null, id);

        if (cnt == 1) {

            blog.setShareId(null);

            return RespBody.succeed(blog);
        }

        return RespBody.failed();

    }

    @PostMapping("tpl/update")
    @ResponseBody
    public RespBody updateTpl(@RequestParam("id") Long id, @RequestParam("tpl") Integer tpl,
                              @RequestParam(value = "desc", required = false) String desc) {

        Blog blog = blogRepository.findOne(id);

        if (!isSuperOrCreator(blog.getCreator())) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        // 模板：1：privated 2：opened (0或者null)：非模板
        int cnt = blogRepository.updateTpl(tpl, desc, id);

        if (cnt == 1) {

            blog.setTpl(tpl);

            return RespBody.succeed(blog);
        }

        return RespBody.failed();

    }

    @GetMapping("tpl/list")
    @ResponseBody
    public RespBody listTpl() {

        List<Blog> blogs = blogRepository.queryTpl(WebUtil.getUsername());

        return RespBody.succeed(blogs);

    }

    @GetMapping("tpl/hotCnt/inc")
    @ResponseBody
    public RespBody incTplHotCnt(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null || Status.Deleted.equals(blog.getStatus())) {
            return RespBody.failed("博文不存在或者已经被删除!");
        }

        Long hotCnt = blog.getTplHotCnt();
        if (hotCnt == null) {
            hotCnt = 1L;
        } else {
            hotCnt = hotCnt + 1;
        }

        blogRepository.updateTplHotCnt(hotCnt, id);

        return RespBody.succeed();
    }

    @PostMapping("history/repair")
    @Secured({"ROLE_ADMIN"})
    @ResponseBody
    public RespBody repairHistory() {

        List<BlogHistory> blogHistories = blogHistoryRepository.findAll();

        blogHistories.forEach(item -> {
            if (item.getEditor() == null) {
                Blog blog = blogRepository.findOne(item.getBlog().getId());
                if (blog != null) {
                    item.setEditor(blog.getEditor() != null ? blog.getEditor() : Editor.Markdown);
                    blogHistoryRepository.saveAndFlush(item);
                }
            }
        });

        return RespBody.succeed();
    }

    private void wsSend(Comment comment, com.lhjz.portal.model.BlogCommentPayload.Cmd cmd, String loginUsername) {
        try {
            ThreadUtil.exec(() ->
                    messagingTemplate.convertAndSend("/blog/comment/update",
                            BlogCommentPayload.builder().id(comment.getId()).version(comment.getVersion())
                                    .bid(comment.getTargetId()).cmd(cmd).username(loginUsername).build())
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @PostMapping("comment/label/toggle")
    @ResponseBody
    public RespBody toggelCommentLabel(@RequestParam("cid") Long cid, @RequestParam("name") String name) {

        Comment comment = commentRepository.findOne(cid);

        if (!hasAuth(Long.valueOf(comment.getTargetId()))) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        Optional<Label> tagOpt = comment.getLabels().stream().filter(tag ->
                // 创建者是自己 & name相等
                tag.getCreator().equals(WebUtil.getUsername()) && tag.getName().equals(name)
        ).findFirst();

        if (tagOpt.isPresent()) { // 移除
            labelRepository.delete(tagOpt.get());

            comment.getLabels().remove(tagOpt.get());

            wsSend(comment, com.lhjz.portal.model.BlogCommentPayload.Cmd.D, WebUtil.getUsername());

        } else { // 添加
            Label tag = new Label();
            tag.setName(name);
            tag.setDescription(name);
            tag.setCreator(WebUtil.getUsername());
            tag.setCreateDate(new Date());
            tag.setComment(comment);

            Label tag2 = labelRepository.saveAndFlush(tag);

            comment.getLabels().add(tag2);

            wsSend(comment, com.lhjz.portal.model.BlogCommentPayload.Cmd.C, WebUtil.getUsername());
        }

        return RespBody.succeed(comment);
    }

    @PostMapping("sort")
    @ResponseBody
    public RespBody sort(BlogSortForm blogSortForm) {

        if (blogSortForm == null || StringUtil.isEmpty(blogSortForm.getItems())) {
            return RespBody.failed("参数错误！");
        }

        BlogSortItem[] sortItems = JsonUtil.json2Object(blogSortForm.getItems(), BlogSortItem[].class);

        Stream.of(sortItems).forEach(item -> {
            Blog blog = blogRepository.findOne(item.getId());
            if (blog.getSpace() == null || AuthUtil.hasSpaceAuth(blog.getSpace())
                    && item.getSort() != null && !item.getSort().equals(blog.getSort())) {
                blog.setSort(item.getSort());
                blogRepository.saveAndFlush(blog);
            }
        });

        return RespBody.succeed();
    }

    @PostMapping("dir/sort")
    @ResponseBody
    public RespBody sortDir(BlogSortForm blogSortForm) {

        if (blogSortForm == null || StringUtil.isEmpty(blogSortForm.getItems())) {
            return RespBody.failed("参数错误！");
        }

        BlogSortItem[] sortItems = JsonUtil.json2Object(blogSortForm.getItems(), BlogSortItem[].class);

        Stream.of(sortItems).forEach(item -> {
            Dir dir = dirRepository.findOne(item.getId());
            if (AuthUtil.hasSpaceAuth(dir.getSpace())
                    && item.getSort() != null && !item.getSort().equals(dir.getSort())) {
                dir.setSort(item.getSort());
                dirRepository.saveAndFlush(dir);
            }
        });

        return RespBody.succeed();
    }

    @PostMapping("space/sort")
    @ResponseBody
    public RespBody sortSpace(BlogSortForm blogSortForm) {

        if (blogSortForm == null || StringUtil.isEmpty(blogSortForm.getItems())) {
            return RespBody.failed("参数错误！");
        }

        BlogSortItem[] sortItems = JsonUtil.json2Object(blogSortForm.getItems(), BlogSortItem[].class);

        Stream.of(sortItems).forEach(item -> {
            Space space = spaceRepository.findOne(item.getId());
            if (isSuperOrCreator(space.getCreator())
                    && item.getSort() != null && !item.getSort().equals(space.getSort())) {
                space.setSort(item.getSort());
                spaceRepository.saveAndFlush(space);
            }
        });

        return RespBody.succeed();
    }

    @GetMapping("check/lock")
    @ResponseBody
    public RespBody checkLock(@RequestParam("id") Long id) {

        return RespBody.succeed(blogLockService.isRealLock(id));
    }

    @GetMapping("list/by/pid")
    @ResponseBody
    public RespBody listByPid(@RequestParam("pid") Long pid,
                              @SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

        Blog blog = blogRepository.findOne(pid);

        if (!hasAuth(blog)) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        List<Blog> blogs = blogRepository.findByPidAndStatusNot(pid, Status.Deleted, sort).stream()
                .filter(this::hasAuth).peek(b -> {
                    b.setContent(null);
                    b.setBlogAuthorities(null);
                    b.setUpdater(null);

                    User creator = b.getCreator();
                    if (creator != null) {
                        User user2 = new User();
                        user2.setUsername(creator.getUsername());
                        user2.setAuthorities(null);
                        user2.setStatus(null);
                        b.setCreator(user2);
                    }

                    b.setCreateDate(null);
                    b.setOpenEdit(null);
                    b.setOpened(null);
                    b.setReadCnt(null);
                    b.setTags(null);
                    b.setType(null);

                    Dir dir = b.getDir();
                    if (dir != null) {
                        Dir dir2 = new Dir();
                        dir2.setId(dir.getId());
                        dir2.setOpened(null);
                        dir2.setPrivated(null);
                        dir2.setStatus(null);
                        b.setDir(dir2);
                    }

                    Space space = b.getSpace();
                    if (space != null) {
                        Space space2 = new Space();
                        space2.setId(space.getId());
                        space2.setDirs(null);
                        space2.setOpened(null);
                        space2.setPrivated(null);
                        space2.setSpaceAuthorities(null);
                        space2.setStatus(null);
                        space2.setType(null);
                        b.setSpace(space2);
                    }

                }).collect(Collectors.toList());

        return RespBody.succeed(blogs);
    }

    @PostMapping("hasChild/update")
    @ResponseBody
    public RespBody updateHasChild(@RequestParam("id") Long id) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null) {
            return RespBody.failed("对应博文不存在！");
        }

        if (!hasAuth(blog)) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        long cnt = blogRepository.countByPid(id);
        blogRepository.updateHasChild(cnt > 0, id);

        return RespBody.succeed(cnt > 0);

    }

    @PostMapping("pid/update")
    @ResponseBody
    public RespBody updatePid(@RequestParam("id") Long id, @RequestParam(value = "pid", required = false) Long pid) {

        Blog blog = blogRepository.findOne(id);

        if (blog == null) {
            return RespBody.failed("对应博文不存在！");
        }

        if (!hasAuth(blog)) {
            return RespBody.failed(ERR_NO_AUTH);
        }

        if (pid != null) {
            Blog blogP = blogRepository.findOne(pid);
            if (blogP == null) {
                return RespBody.failed("对应博文不存在！");
            }

            if (!hasAuth(blogP)) {
                return RespBody.failed(ERR_NO_AUTH);
            }
        }

        Long pidO = blog.getPid();

        int row = blogRepository.updatePid(pid, id);

        if (row == 1) {

            if (pidO != null) { // 原来父博文检查看是否还存在子级博文
                long cnt = blogRepository.countByPid(pidO);
                blogRepository.updateHasChild(cnt > 0, pidO);
            }

            if (pid != null) { // 新的父博文设置存在子级博文
                blogRepository.updateHasChild(true, pid);
            }

            return RespBody.succeed();
        }

        return RespBody.failed();
    }
}
