import { bindable, containerless, bindingMode } from 'aurelia-framework';

@containerless
export class EmChatContentItem {

    @bindable({ defaultBindingMode: bindingMode.twoWay }) chats;
    @bindable loginUser;
    @bindable isAt;
    @bindable channel;
    @bindable markId;
    @bindable chatTo;
    members = [];
    basePath = utils.getBasePath();

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_MEMBER_ADD_OR_REMOVE, (payload) => {
            this.members = [nsCtx.memberAll, ...payload.members];
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $('.tms-chat-direct').on('click', '.markdown-body .at-user', (event) => {
            event.preventDefault();
            ea.publish(nsCons.EVENT_CHAT_MSG_INSERT, {
                content: `{~${$(event.currentTarget).attr('data-value')}} `
            });
        });

        // 消息popup
        $('.tms-chat-direct').on('mouseenter', '.markdown-body a[href*="#/chat/"]:not(.pp-not)', (event) => {
            event.preventDefault();
            var $a = $(event.currentTarget);
            ea.publish(nsCons.EVENT_CHAT_MSG_POPUP_SHOW, {
                id: utils.urlQuery('id', $a.attr('href')),
                target: event.currentTarget
            });
        });

        // wiki dir
        $('.tms-chat-direct').on('mouseenter', '.tms-content-body .em-chat-content-item', (event) => {
            event.preventDefault();
            var $c = $(event.currentTarget);

            ea.publish(nsCons.EVENT_CHAT_MSG_WIKI_DIR, {
                dir: utils.dir($c.find('> .content > .markdown-body'))
            });
        });

        $('.tms-chat-direct').on('click', '.panel-wiki-dir .wiki-dir-item', (event) => {
            event.preventDefault();
            ea.publish(nsCons.EVENT_CHAT_CONTENT_SCROLL_TO, { target: $('#' + $(event.currentTarget).attr('data-id')) });
        });

        // 用户信息popup
        $('.tms-chat-direct').on('mouseenter', 'span[data-value].at-user:not(.pp-not),a[data-value].author:not(.pp-not)', (event) => {
            event.preventDefault();
            var $a = $(event.currentTarget);
            ea.publish(nsCons.EVENT_CHAT_MEMBER_POPUP_SHOW, {
                channel: this.channel,
                username: $a.attr('data-value'),
                target: event.currentTarget
            });
        });

        this.initHotkeys();
    }

    channelChanged() {

        if (this.channel) {
            this.members = [nsCtx.memberAll, ...this.channel.members];
        } else {
            this.members = [];
        }
    }

    deleteHandler(item) {

        this.emConfirmModal.show({
            onapprove: () => {

                let url;

                if (this.isAt) {
                    url = `/admin/chat/direct/delete`;
                } else {
                    url = `/admin/chat/channel/delete`;
                }

                $.post(url, {
                    id: item.id
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        this.chats = _.reject(this.chats, {
                            id: item.id
                        });
                        toastr.success('删除消息成功!');
                    } else {
                        toastr.error(data.data, '删除消息失败!');
                    }
                });
            }
        });

    }

    initHotkeys() {
        $(document).bind('keydown', 'e', (evt) => {
            evt.preventDefault();
            let chat = _.findLast(this.chats, c => c.creator.username == this.loginUser.username);
            if (chat) {
                this.editHandler(chat, $(`.em-chat-content-item[data-id="${chat.id}"]`).find('> .content > textarea'));
            }
        });
    }

    editHandler(item, editTxtRef) {

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
                    $(editTxtRef).focus().select();
                    autosize.update(editTxtRef);
                });
            } else {
                toastr.error(data.data);
            }

        });
    }

    editOkHandler(evt, item, txtRef) {
        this.editSave(item, txtRef);
        item.isEditing = false;
    }

    editCancelHandler(evt, item, txtRef) {
        item.content = item.contentOld;
        $(txtRef).val(item.content);
        item.isEditing = false;
    }

    editSave(item, txtRef) {

        this.sending = true;

        item.content = $(txtRef).val();

        var html = utils.md2html(item.content);
        var htmlOld = utils.md2html(item.contentOld);

        let url;
        let data;

        if (this.isAt) {
            url = `/admin/chat/direct/update`;
            data = {
                baseUrl: utils.getBaseUrl(),
                path: wurl('path'),
                id: item.id,
                content: item.content,
                diff: utils.diffS(item.contentOld, item.content),
                // contentHtml: html,
                // contentHtmlOld: htmlOld
            };
        } else {
            url = `/admin/chat/channel/update`;
            data = {
                url: utils.getUrl(),
                id: item.id,
                version: item.version,
                usernames: utils.parseUsernames(item.content, this.members).join(','),
                content: item.content,
                diff: utils.diffS(item.contentOld, item.content),
                // contentHtml: html,
                // contentHtmlOld: htmlOld
            };
        }

        $.post(url, data, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('更新消息成功!');
                item.isEditing = false;
                item.version = data.data.version;
            } else {
                toastr.error(data.data, '更新消息失败!');
            }
        }).always(() => {
            this.sending = false;
        });
    }

    eidtKeydownHandler(evt, item, txtRef) {

        if (this.sending) {
            return false;
        }

        if (evt.ctrlKey && evt.keyCode === 13) {

            this.editSave(item, txtRef);

            return false;
        } else if (evt.ctrlKey && evt.keyCode === 85) {
            $(txtRef).next('.tms-edit-actions').find('.upload').click();
            return false;
        } else if (evt.keyCode === 27) {
            this.editCancelHandler(evt, item, txtRef);
        }

        return true;
    }

    notifyRendered(last, item) {
        if (last) {
            _.defer(() => {
                ea.publish(nsCons.EVENT_CHAT_LAST_ITEM_RENDERED, {
                    item: item
                });
            });
        }
    }

    stowHandler(item) {
        $.post('/admin/chat/channel/stow', {
            id: item.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('收藏消息成功!');
            } else {
                toastr.error(data.data, '收藏消息失败!');
            }
        });
    }

    openEditHandler(item) {
        $.post('/admin/chat/channel/openEdit', {
            id: item.id,
            open: !item.openEdit
        }, (data, textStatus, xhr) => {
            if (data.success) {
                item.openEdit = !item.openEdit;
                toastr.success(`${item.openEdit ? '开启' : '关闭'}协作编辑成功!`);
            } else {
                toastr.success(`${!item.openEdit ? '开启' : '关闭'}协作编辑失败!`);
            }
        });
    }

    replyHandler(item) {
        ea.publish(nsCons.EVENT_CHAT_MSG_INSERT, {
            content: `[[回复#${item.id}](${utils.getUrl()}?id=${item.id}){~${item.creator.username}}]\n\n`
        });

        // 标记@自己的该消息为已读
        $.post('/admin/chat/channel/markAsReadedByChat', {
            chatId: item.id
        });
    }

    creatorNameHandler(item) {
        ea.publish(nsCons.EVENT_CHAT_MSG_INSERT, {
            content: `{~${item.creator.username}} `
        });
    }

    refreshHandler(item) {
        $.get('/admin/chat/channel/get', {
            id: item.id
        }, (data) => {
            if (item.version != data.data.version) {
                _.extend(item, data.data);
                toastr.success('刷新同步成功!');
            } else {
                toastr.info('消息内容暂无变更!');
            }
        });
    }

    likeHandler(item, isLike) {

        if ((isLike && item.isZanVoted) || (!isLike && item.isCaiVoted)) {
            return;
        }

        $.post('/admin/chat/channel/vote', {
            id: item.id,
            url: utils.getUrl(),
            contentHtml: utils.md2html(item.content),
            type: isLike ? 'Zan' : 'Cai'
        }, (data, textStatus, xhr) => {
            if (data.success) {
                _.extend(item, data.data);
                if (isLike) {
                    item.isZanVoted = true;
                } else {
                    item.isCaiVoted = true;
                }
            } else {
                toastr.error(data.data);
            }
        });
    }
}
