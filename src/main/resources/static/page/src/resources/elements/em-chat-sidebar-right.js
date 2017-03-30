import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatSidebarRight {

    last = true;
    @bindable loginUser;
    @bindable isAt;
    @bindable channel;
    forAction = ''; // search | stow | at
    forShow = '';

    basePath = utils.getBasePath();

    headerMapping = {
        [nsCons.ACTION_TYPE_SEARCH]: '无符合检索结果',
        [nsCons.ACTION_TYPE_AT]: '暂无@消息',
        [nsCons.ACTION_TYPE_STOW]: '暂无收藏消息',
    };

    /**
     * 构造函数
     */
    constructor() {

        this.subscribe = ea.subscribe(nsCons.EVENT_CHAT_SEARCH_RESULT, (payload) => {
            this._mappingActionShow(payload.action);
            let result = payload.result;
            this.search = payload.search;
            this.page = result;
            this.chats = result.content;
            this.last = result.last;
            this.moreCnt = result.totalElements - (result.number + 1) * result.size;
        });

        this.subscribe2 = ea.subscribe(nsCons.EVENT_CHAT_SHOW_AT, (payload) => {
            this._mappingActionShow(payload.action);
            let result = payload.result;
            this.page = result;
            this.chats = _.map(result.content, (item) => {
                let chatChannel = item.chatChannel;
                chatChannel.chatAt = item;
                return chatChannel;
            });
            this.last = result.last;
            this.moreCnt = result.totalElements - (result.number + 1) * result.size;
        });

        this.subscribe1 = ea.subscribe(nsCons.EVENT_CHAT_SHOW_STOW, (payload) => {
            this._mappingActionShow(payload.action);
            this.chats = payload.result;
            this.last = true;
        });

        this.subscribe3 = ea.subscribe(nsCons.EVENT_CHAT_SHOW_DIR, (payload) => {
            this._mappingActionShow(payload.action);
            $(this.dirRef).empty().append(payload.result);
        });

        this.subscribe4 = ea.subscribe(nsCons.EVENT_CHAT_SHOW_ATTACH, (payload) => {
            this._mappingActionShow(payload.action);
        });

        this.subscribe5 = ea.subscribe(nsCons.EVENT_CHAT_SHOW_SCHEDULE, (payload) => {
            this._mappingActionShow(payload.action);
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {

        this.subscribe.dispose();
        this.subscribe1.dispose();
        this.subscribe2.dispose();
        this.subscribe3.dispose();
        this.subscribe4.dispose();
        this.subscribe5.dispose();
    }

    _mappingActionShow(forAction) {
        this.forAction = forAction;
        if (_.includes([nsCons.ACTION_TYPE_SEARCH, nsCons.ACTION_TYPE_STOW, nsCons.ACTION_TYPE_AT], this.forAction)) {
            this.forShow = 'chat-msg';
        } else if (_.includes([nsCons.ACTION_TYPE_DIR], this.forAction)) {
            this.forShow = 'wiki-dir';
        } else if (_.includes([nsCons.ACTION_TYPE_ATTACH], this.forAction)) {
            this.forShow = 'chat-attach';
            this.chatAttachVm.fetch();
        } else if (_.includes([nsCons.ACTION_TYPE_SCHEDULE], this.forAction)) {
            this.forShow = 'chat-schedule';
            this.chatScheduleVm.show();
        }
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        this.initHotkeys();
    }

    initHotkeys() {
        $(document).bind('keydown', 'o', (event) => {
            event.preventDefault();
            let item = _.find(this.chats, { isHover: true });
            item && (item.isOpen = !item.isOpen);
        });

    }

    searchItemMouseleaveHandler(item) {
        item.isOpen = false;
        item.isHover = false;
    }

    searchItemMouseenterHandler(item) {
        item.isHover = true;
    }

    gotoChatHandler(item) {
        ea.publish(nsCons.EVENT_CHAT_SEARCH_GOTO_CHAT_ITEM, { chatItem: item });
    }

    openSearchItemHandler(item) {
        item.isOpen = !item.isOpen;
    }

    searchMoreHandler() {

        if (this.forAction == nsCons.ACTION_TYPE_SEARCH) {
            this.searchMoreP = $.get('/admin/chat/direct/search', {
                search: this.search,
                size: this.page.size,
                page: this.page.number + 1
            }, (data) => {
                if (data.success) {
                    this.chats = _.concat(this.chats, data.data.content);

                    this.page = data.data;
                    this.last = data.data.last;
                    this.moreCnt = data.data.totalElements - (data.data.number + 1) * data.data.size;
                }
            });
        } else {
            this.searchMoreP = $.get('/admin/chat/channel/getAts', {
                size: this.page.size,
                page: this.page.number + 1
            }, (data) => {
                if (data.success) {
                    this.chats = _.concat(this.chats, _.map(data.data.content, (item) => {
                        let chatChannel = item.chatChannel;
                        chatChannel.chatAt = item;
                        return chatChannel;
                    }));

                    this.page = data.data;
                    this.last = data.data.last;
                    this.moreCnt = data.data.totalElements - (data.data.number + 1) * data.data.size;
                }
            });
        }
    }

    removeStowHandler(item) {
        $.post('/admin/chat/channel/removeStow', {
            id: item.chatStow.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.chats = _.reject(this.chats, {
                    id: item.id
                });
                toastr.success('移除收藏消息成功!');
            } else {
                toastr.error(data.data, '移除收藏消息失败!');
            }
        });
    }

    removeAtHandler(item) {
        $.post('/admin/chat/channel/markAsReaded', {
            chatAtId: item.chatAt.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.chats = _.reject(this.chats, {
                    id: item.id
                });
            } else {
                toastr.error(data.data, '移除@消息失败!');
            }
        });
    }
}
