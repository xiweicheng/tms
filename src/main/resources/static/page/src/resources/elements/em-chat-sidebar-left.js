import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatSidebarLeft {

    @bindable users;
    @bindable loginUser;
    @bindable channels;
    @bindable chatTo;
    @bindable isAt;
    filter = '';
    isSuper = nsCtx.isSuper;

    usersChanged() {
        this._filter();
    }

    channelsChanged() {
        this._filter();
    }

    loginUserChanged() {
        if (this.loginUser) {
            this.isSuper = utils.isSuperUser(this.loginUser);
        }
    }

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_CHANNEL_ACTIONS, (payload) => {
            this[payload.action](payload.item);
        });
        this.subscribe1 = ea.subscribe(nsCons.EVENT_SYSTEM_LINKS_REFRESH, (payload) => {
            this._refreshSysLinks();
        });
    }

    _refreshSysLinks() {
        $.get('/admin/link/listByApp', (data) => {
            if (data.success) {
                this.sysLinks = data.data;
            } else {
                this.sysLinks = [];
            }
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe1.dispose();
    }

    bind(bindingCtx, overrideCtx) {
        this._refreshSysLinks();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.logoRef).on('mouseenter', (event) => {
            $(this.logoRef).animateCss('flip');
        });
    }

    _filter() {

        _.each(this.users, (item) => {
            item.hidden = item.username.indexOf(this.filter) == -1;
        });

        _.each(this.channels, (item) => {
            item.hidden = item.name.indexOf(this.filter) == -1;
        });
    }

    chatToUserFilerKeyupHanlder(evt) {
        this._filter();

        if (evt.keyCode === 13) {
            let user = _.find(this.users, {
                hidden: false
            });

            if (user) {
                window.location = wurl('path') + `#/chat/@${user.username}`;
                return;
            }

            let channel = _.find(this.channels, {
                hidden: false
            });

            if (channel) {
                window.location = wurl('path') + `#/chat/${channel.name}`;
                return;
            }
        }
    }

    clearFilterHandler() {
        this.filter = '';
        _.each(this.users, (item) => {
            item.hidden = item.username.indexOf(this.filter) == -1;
        });
        _.each(this.channels, (item) => {
            item.hidden = item.name.indexOf(this.filter) == -1;
        });
    }

    editHandler(item) {
        this.selectedChannel = item;
        this.channelEditMd.show();
    }

    delHandler(item) {
        this.confirmMd.show({
            onapprove: () => {
                $.post('/admin/channel/delete', {
                    id: item.id
                }, (data) => {
                    if (data.success) {
                        toastr.success('删除频道成功!');
                        _.remove(this.channels, { id: item.id });
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_DELETED, { channel: item });
                    } else {
                        toastr.error(data.data, '删除频道失败!');
                    }
                });
            }
        });
    }

    membersMgrHandler(item) {
        this.selectedChannel = item;
        this.channelMembersMgrMd.show();
    }

    membersShowHandler(item) {
        this.selectedChannel = item;
        this.channelMembersShowMd.show();
    }

    leaveHandler(item) {
        this.confirmMd.show({
            content: `确定要离开频道<code class="nx">${item.title}</code>吗?`,
            onapprove: () => {
                $.post('/admin/channel/leave', {
                    id: item.id
                }, (data) => {
                    if (data.success) {
                        toastr.success('离开频道成功!');
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_LEAVED, { channel: data.data });
                    } else {
                        toastr.error(data.data, '离开频道失败!');
                    }
                });
            }
        });
    }

    switchHandler() {
        ea.publish(nsCons.EVENT_SWITCH_CHAT_TO, {});
    }

    addChannelLinkHandler(event) {
        this.sysLinkMgrVm.show();
    }

}
