import { bindable, inject } from 'aurelia-framework';
import poll from "common/common-poll";
import {
    default as Clipboard
} from 'clipboard';
import {
    default as clipboard
} from 'clipboard-js';
import {
    default as Dropzone
} from 'dropzone';

import chatService from './chat-service';

export class ChatDirect {

    offset = 0;

    first = true; // 第一页
    last = true; // 最后一页

    originalHref = wurl();

    loginUser;
    users = [];
    channels = [];
    chatTo = null;

    /**
     * 构造函数
     */
    constructor() {

        Dropzone.autoDiscover = false;
        this.poll = poll;

        new Clipboard('.tms-chat-direct .tms-clipboard')
            .on('success', function(e) {
                toastr.success('复制到剪贴板成功!');
            }).on('error', function(e) {
                toastr.error('复制到剪贴板失败!');
            });

        this.initSubscribeEvent();
    }

    initSubscribeEvent() {

        this.subscribe = ea.subscribe(nsCons.EVENT_CHAT_MSG_SENDED, (payload) => {

            poll.reset();

            if (!this.first) { // 不是第一页
                if (this.isAt) {
                    this.listChatDirect(false);
                } else {
                    this.listChatChannel(false);
                }
            }
        });

        this.subscribe2 = ea.subscribe(nsCons.EVENT_CHAT_SIDEBAR_TOGGLE, (payload) => {

            this.isRightSidebarShow = payload.isShow;
            if (this.isRightSidebarShow) {
                let wid = $(this.contentRef).width() - 392;
                $(this.contentBodyRef).width(wid);
                $(this.contentBodyRef).children('.scroll-wrapper').width(wid);
            } else {
                $(this.contentBodyRef).css('width', '100%');
                $(this.contentBodyRef).children('.scroll-wrapper').css('width', '100%');
            }
        });

        this.subscribe3 = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_CREATED, (payload) => {
            this.channels.splice(0, 0, payload.channel);
        });

        this.subscribe4 = ea.subscribe(nsCons.EVENT_CHAT_SEARCH_GOTO_CHAT_ITEM, (payload) => {

            this.gotoChatItem(payload.chatItem);
        });

        this.subscribe5 = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_DELETED, (payload) => {

            if (!this.isAt && (payload.channel.name == this.chatTo)) {
                window.location = wurl('path') + `#/chat/@${this.loginUser.username}`;
            }

            this.channels = [...this.channels];

        });

        this.subscribe6 = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_JOINED, (payload) => {

            this.channels.splice(0, 0, payload.channel);

        });

        this.subscribe7 = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_LEAVED, (payload) => {

            if (!this.isAt && (payload.channel.name == this.chatTo)) {
                window.location = wurl('path') + `#/chat/@${this.loginUser.username}`;
            }

            this.channels = _.reject(this.channels, { id: payload.channel.id });

        });

        this.subscribe8 = ea.subscribe(nsCons.EVENT_CHAT_LAST_ITEM_RENDERED, (payload) => {

            if (payload.item.__scroll) {
                this.scrollToAfterImgLoaded(this.markId ? this.markId : 'b');
                delete payload.item.__scroll;
                this.markId = null;
            }

        });

        this.subscribe9 = ea.subscribe(nsCons.EVENT_SCROLLBAR_SCROLL_TO_BOTTOM, (payload) => {

            if (this.scrollbarRef == payload.element) {
                poll.reset();
            }

        });

        this.subscribe10 = ea.subscribe(nsCons.EVENT_CHAT_CONTENT_SCROLL_TO, (payload) => {

            this.scrollTo(payload.target);

        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {

        this.subscribe.dispose();
        this.subscribe2.dispose();
        this.subscribe3.dispose();
        this.subscribe4.dispose();
        this.subscribe5.dispose();
        this.subscribe6.dispose();
        this.subscribe7.dispose();
        this.subscribe8.dispose();
        this.subscribe9.dispose();
        this.subscribe10.dispose();

        clearInterval(this.timeagoTimer);
        poll.stop();
    }

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {

        this._reset();

        this.markId = params.id;
        this.routeConfig = routeConfig;

        if (this.chatId) {
            this.preChatId = this.chatId; // 记录切换前的沟通对象
        }
        this.chatId = nsCtx.chatId = params.username;

        localStorage && localStorage.setItem(nsCons.KEY_REMEMBER_LAST_CHAT_TO, this.chatId);

        this.isAt = nsCtx.isAt = _.startsWith(params.username, '@');
        this.chatTo = nsCtx.chatTo = utils.getChatName(params.username);

        if (this.markId) {
            history.replaceState(null, '', utils.removeUrlQuery('id'));
        }

        return Promise.all([chatService.loginUser(false).then((user) => {
                this.loginUser = user;
                nsCtx.loginUser = user;
                nsCtx.isSuper = utils.isSuperUser(this.loginUser);
                nsCtx.isAdmin = utils.isAdminUser(this.loginUser);
            }),
            chatService.listUsers(false).then((users) => {
                this.users = users;
                nsCtx.users = users;
                window.tmsUsers = users;
                if (this.isAt) {
                    this.channel = null;
                    this.user = _.find(this.users, {
                        username: this.chatTo
                    });

                    if (this.user) {
                        let name = this.user ? this.user.name : this.chatTo;
                        routeConfig.navModel.setTitle(`${name} | 私聊 | TMS`);

                        this.listChatDirect(true);
                    } else {
                        toastr.error(`聊天用户[${this.chatTo}]不存在或者没有权限访问!`);
                        if (this.preChatId) {
                            window.location = wurl('path') + `#/chat/${this.preChatId}`;
                        } else {
                            window.location = wurl('path') + `#/chat/@${this.loginUser.username}`;
                        }
                    }

                }
            }),
            chatService.listChannels(false).then((channels) => {
                this.channels = channels;
                nsCtx.channels = channels;
                if (!this.isAt) {
                    this.user = null;
                    this.channel = _.find(this.channels, {
                        name: this.chatTo
                    });

                    if (this.channel) {
                        routeConfig.navModel.setTitle(`${this.channel.title} | 频道 | TMS`);

                        this.listChatChannel(true);
                    } else {
                        toastr.error(`聊天频道[${this.chatTo}]不存在或者没有权限访问!`);
                        if (this.preChatId) {
                            window.location = wurl('path') + `#/chat/${this.preChatId}`;
                        } else {
                            window.location = wurl('path') + `#/chat/@${this.loginUser.username}`;
                        }
                    }
                }
            })
        ]);

    }

    _reset() {
        this.progressWidth = 0;
        this.chats = null;
        this.first = true; // 第一页
        this.last = true; // 最后一页
    }

    lastMoreHandler() { // 上面的老消息

        let start = _.first(this.chats).id;

        let url;
        let data;
        if (this.isAt) {
            url = `/admin/chat/direct/more`;
            data = {
                last: true,
                start: start,
                size: 20,
                chatTo: this.chatTo
            };
        } else {
            url = `/admin/chat/channel/more`;
            data = {
                last: true,
                start: start,
                size: 20,
                channelId: this.channel.id
            };
        }
        this.lastMoreP = $.get(url, data, (data) => {
            if (data.success) {
                this.chats = _.unionBy(_.reverse(data.data), this.chats);
                this.last = (data.msgs[0] - data.data.length <= 0);
                !this.last && (this.lastCnt = data.msgs[0] - data.data.length);
                this.scrollToAfterImgLoaded(start);
            } else {
                toastr.error(data.data, '获取更多消息失败!');
            }
        });
    }

    firstMoreHandler() { // 前面的新消息

        let start = _.last(this.chats).id;
        let url;
        let data;
        if (this.isAt) {
            url = `/admin/chat/direct/more`;
            data = {
                last: false,
                start: start,
                size: 20,
                chatTo: this.chatTo
            };
        } else {
            url = `/admin/chat/channel/more`;
            data = {
                last: false,
                start: start,
                size: 20,
                channelId: this.channel.id
            };
        }
        this.nextMoreP = $.get(url, data, (data) => {
            if (data.success) {
                this.chats = _.unionBy(this.chats, data.data);
                this.first = (data.msgs[0] - data.data.length <= 0);
                !this.first && (this.firstCnt = data.msgs[0] - data.data.length);
                this.scrollToAfterImgLoaded(start);
            } else {
                toastr.error(data.data, '获取更多消息失败!');
            }
        });
    }

    // 获取频道消息
    listChatChannel(isCareMarkId) {

        var data = {
            size: 20,
            channelId: this.channel.id
        };

        // 如果设定了获取消息界限
        if (this.markId && isCareMarkId) {
            data.id = this.markId;
        }

        $.get('/admin/chat/channel/listBy', data, (data) => {
            this.processChats(data);
        });
    }

    // 获取私聊消息
    listChatDirect(isCareMarkId) {

        var data = {
            size: 20,
            chatTo: this.chatTo
        };

        // 如果设定了获取消息界限
        if (this.markId && isCareMarkId) {
            data.id = this.markId;
        }
        $.get('/admin/chat/direct/list', data, (data) => {
            this.processChats(data);
        });
    }

    // 共同返回消息处理
    processChats(data) {
        if (data.success) {
            this.chats = _.reverse(data.data.content);
            let lastChat = _.last(this.chats);
            lastChat && (lastChat.__scroll = true); // 标记消息列表渲染完成需要执行消息滚动定位.
            this.last = data.data.last;
            this.first = data.data.first;
            !this.last && (this.lastCnt = data.data.totalElements - data.data.numberOfElements);
            !this.first && (this.firstCnt = data.data.size * data.data.number);
        }
    }

    _scrollTo(to) {
        if (to == 'b') {
            $(this.commentsRef).parent('.scroll-content').scrollTo('max');
        } else if (to == 't') {
            $(this.commentsRef).parent('.scroll-content').scrollTo(0);
        } else {
            if (_.some(this.chats, { id: +to })) {
                $(this.commentsRef).parent('.scroll-content').scrollTo(`.comment[data-id="${to}"]`, {
                    offset: this.offset
                });
                $(this.commentsRef).find(`.comment[data-id]`).removeClass('active');
                $(this.commentsRef).find(`.comment[data-id=${to}]`).addClass('active');
            } else {
                $(this.commentsRef).parent('.scroll-content').scrollTo('max');
                toastr.warning(`消息[${to}]不存在,可能已经被删除!`);
            }
        }
    }

    scrollToAfterImgLoaded(to) {
        _.defer(() => {
            new ImagesLoaded(this.commentsRef).always(() => {
                this._scrollTo(to);
            });

            this._scrollTo(to);
        });

    }

    doPoll() {
        poll.start((resetCb, stopCb) => {
            this._pollChats(resetCb, stopCb);
            this._poll(resetCb, stopCb);
        });
    }

    _poll(resetCb, stopCb) {

        let lastChat = _.last(this.chats);

        if (this.pollOnGoing || this.isAt || !this.channel || !lastChat) {
            return;
        }

        this.pollOnGoing = true;

        $.get('/admin/chat/channel/poll', {
            channelId: this.channel.id,
            lastChatChannelId: lastChat.id,
            isAt: true
        }, (data) => {
            if (data.success) {

                if (this.countAt && data.data.countAt > this.countAt) {
                    let cnt = data.data.countAt - this.countAt;
                    push.create('TMS沟通@消息通知', {
                        body: `你有${cnt}条新的@消息!`,
                        icon: {
                            x16: 'img/tms-x16.ico',
                            x32: 'img/tms-x32.png'
                        },
                        timeout: 5000
                    });
                }
                this.countAt = data.data.countAt;
                ea.publish(nsCons.EVENT_CHAT_POLL_UPDATE, {
                    countAt: data.data.countAt,
                    countMyRecentSchedule: data.data.countMyRecentSchedule
                });
            }
        }).always(() => {
            this.pollOnGoing = false;
        });
    }

    // 消息轮询处理
    _pollChats(resetCb, stopCb) {

        if (this.pollChatsOngoing || !this.chats || !this.first) {
            return;
        }

        let lastChat = _.last(this.chats);

        let url;
        let data;

        if (this.isAt) {
            url = `/admin/chat/direct/latest`;
            data = {
                id: lastChat ? lastChat.id : 0,
                chatTo: this.chatTo
            };
        } else {
            url = `/admin/chat/channel/latest`;
            data = {
                id: lastChat ? lastChat.id : 0,
                channelId: this.channel.id
            };
        }

        this.pollChatsOngoing = true;

        $.get(url, data, (data) => {
            if (data.success) {

                if (!this._checkPollResultOk(data)) {
                    return;
                }

                this._checkNeedNotify(data);

                this.chats = _.unionBy(this.chats, data.data, 'id');
                this.scrollToAfterImgLoaded('b');
            } else {
                toastr.error(data.data, '轮询获取消息失败!');
            }
        }).fail((xhr, sts) => {
            stopCb();
            utils.errorAutoTry(() => {
                resetCb();
            });
        }).always(() => {
            this.pollChatsOngoing = false;
        });
    }

    _checkNeedNotify(data) {

        if (data.data.length == 0) {
            return false;
        }

        let hasOwn = _.some(data.data, (item) => {
            return item.creator.username == this.loginUser.username;
        });

        if (!hasOwn) {
            push.create('TMS沟通频道消息通知', {
                body: `频道[${this.channel.title}]有新消息了!`,
                icon: {
                    x16: 'img/tms-x16.ico',
                    x32: 'img/tms-x32.png'
                },
                timeout: 5000
            });
        }
    }

    _checkPollResultOk(data) {

        if (data.data.length == 0) {
            return false;
        }

        let chat = _.first(data.data);
        return this.isAt ? _.has(chat, 'chatTo') : _.has(chat, 'channel');
    }

    /**
     * 当数据绑定引擎绑定到视图时被调用
     * @param  {[object]} ctx 视图绑定上下文环境对象
     */
    bind(ctx) {

        this.doPoll();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

        let tg = timeago();
        this.timeagoTimer = setInterval(() => {
            $(this.chatContainerRef).find('[data-timeago]').each((index, el) => {
                $(el).text(tg.format($(el).attr('data-timeago'), 'zh_CN'));
            });
        }, 5000);

        this.initHotkeys();
        this.initFocusedComment();

        $(this.scrollbarRef).on('mouseenter', '.em-chat-content-item', (event) => {
            event.preventDefault();
            let $item = $(event.currentTarget);
            this.$hoveredItem = $item;
            this.isShowHead = !utils.isElementInViewport($item.children('.em-user-avatar'));
            let $next = $item.next('.em-chat-content-item');
            if ($next.size() === 1) {
                this.isShowFoot = !utils.isElementInViewport($next.children('.em-user-avatar'));
            } else {
                this.isShowFoot = false;
            }
        }).on('mouseleave', (event) => {
            event.preventDefault();
            this.isShowHead = false;
            this.isShowFoot = false;
        });

        $(this.commentsRef).on('click', '.cbutton', function(event) {
            event.preventDefault();
            let $btn = $(this);
            $btn.addClass('cbutton--click');
            setTimeout(function() {
                $btn.removeClass('cbutton--click');
            }, 500);
        });

        $(this.chatContainerRef).on('click', 'code[data-code]', function(event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).attr('data-code')).then(
                    () => { toastr.success('复制到剪贴板成功!'); },
                    (err) => { toastr.error('复制到剪贴板失败!'); }
                );
            }
        });

        $(this.chatContainerRef).on('click', '.pre-code-wrapper', function(event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).find('i[data-clipboard-text]').attr('data-clipboard-text')).then(
                    () => { toastr.success('复制到剪贴板成功!'); },
                    (err) => { toastr.error('复制到剪贴板失败!'); }
                );
            }
        });

        $('.tms-comments-container[ref="scrollbarRef"]').scroll(_.throttle((event) => {
            try {
                let sHeight = $(event.currentTarget)[0].scrollHeight;
                let sTop = $(event.currentTarget)[0].scrollTop;

                let scale = sTop * 1.0 / (sHeight - $(event.currentTarget).outerHeight());
                this.progressWidth = $(event.currentTarget).outerWidth() * scale;
            } catch (err) { this.progressWidth = 0; }

        }, 10));

    }

    goHeadHandler() {
        this.scrollTo(this.$hoveredItem, 500, () => { this.isShowHead = false; });
    }

    goFootHandler() {
        this.scrollTo(this.$hoveredItem.next(), 500, () => { this.isShowFoot = false; });
    }

    initFocusedComment() {
        $(this.commentsRef).on('click', '.comment.item', (event) => {
            this.focusedComment = $(event.currentTarget);
        }).on('dblclick', '.comment.item', (event) => {
            if (event.ctrlKey) {
                let chatId = $(event.currentTarget).attr('data-id');
                let $t = $(event.currentTarget).find('.content > textarea');
                let item = _.find(this.chats, { id: Number.parseInt(chatId) });

                if (!item.openEdit && (item.creator.username != this.loginUser.username)) {
                    return;
                }

                $.get(`/admin/chat/${this.isAt ? 'direct' : 'channel'}/get`, {
                    id: item.id
                }, (data) => {
                    if (data.success) {
                        if (item.version != data.data.version) {
                            _.extend(item, data.data);
                        }
                        item.isEditing = true;
                        item.contentOld = item.content;
                        _.defer(() => {
                            $t.focus().select();
                            autosize.update($t.get(0));
                        });
                    } else {
                        toastr.error(data.data);
                    }

                });
            }
        });
    }

    getScrollTargetComment(isPrev) {
        if (isPrev) {
            if (this.focusedComment && this.focusedComment.size() === 1) {
                let $avatar = this.focusedComment.find('> a.em-user-avatar');
                if (utils.isElementInViewport($avatar)) {
                    let prev = this.focusedComment.prev('.comment.item');
                    (prev.size() === 1) && (this.focusedComment = prev);
                }
            } else {
                this.focusedComment = $(this.commentsRef).children('.comment.item:first');
            }
        } else {
            if (this.focusedComment && this.focusedComment.size() === 1) {
                let next = this.focusedComment.next('.comment.item');
                (next.size() === 1) && (this.focusedComment = next);
            } else {
                this.focusedComment = $(this.commentsRef).children('.comment.item:last');
            }
        }
        return this.focusedComment;
    }

    scrollTo(target, duration = 0, onAfter) {
        this.focusedComment = target;
        $(this.commentsRef).parent('.scroll-content').scrollTo(target, duration, {
            offset: this.offset,
            onAfter: onAfter
        });
    }

    initHotkeys() {
        $(document).bind('keydown', 'ctrl+u', (evt) => {
            evt.preventDefault();
            $(this.emChatInputRef.btnItemUploadRef).find('.content').click();
        }).bind('keydown', 'ctrl+/', (evt) => {
            evt.preventDefault();
            this.emChatInputRef.emHotkeysModal.show();
        }).bind('keydown', 'alt+up', (evt) => {
            evt.preventDefault();
            this.scrollTo(this.getScrollTargetComment(true));
        }).bind('keydown', 'alt+down', (evt) => {
            evt.preventDefault();
            this.scrollTo(this.getScrollTargetComment());
        }).bind('keydown', 't', (event) => {
            event.preventDefault();
            this.scrollTo($(this.commentsRef).children('.comment.item:first'));
        }).bind('keydown', 'b', (event) => {
            event.preventDefault();
            this.scrollTo($(this.commentsRef).children('.comment.item:last'));
        });

    }

    gotoChatItem(item) {

        let chat = _.find(this.chats, { id: item.id });
        if (chat) {
            this.scrollToAfterImgLoaded(item.id);
        } else {

            let chatTo;
            let chatId;

            if (item.chatTo) {
                chatTo = item.chatTo.username;
                chatId = `@${chatTo}`;
            } else if (item.channel) {
                chatTo = item.channel.name;
                chatId = `${chatTo}`;
            }

            if (this.chatTo == chatTo) { // 当前定位消息就在当前聊天对象里,只是没有获取显示出来
                this.activate({
                    id: item.id,
                    username: chatId
                }, this.routeConfig);
            } else { // 定位消息在非当前聊天对象中
                window.location = wurl('path') + `#/chat/${chatId}?id=${item.id}`;
            }
        }

    }

    refreshLatestHandler(event) {
        event.stopImmediatePropagation();
        this.markId = null;
        if (this.isAt) {
            this.listChatDirect(false);
        } else {
            this.listChatChannel(false);
        }
    }

}
