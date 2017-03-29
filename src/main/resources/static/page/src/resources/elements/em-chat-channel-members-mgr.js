import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelMembersMgr {

    @bindable channel;
    @bindable users;

    /**
     * 构造函数
     */
    constructor() {
        this.membersOpts = {
            onAdd: (addedValue, addedText, $addedChoice) => {
                this.emModal.showDimmer();
                $.post('/admin/channel/addMember', {
                    id: this.channel.id,
                    members: addedValue,
                    baseUrl: utils.getBaseUrl(),
                    path: wurl('path'),
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('添加成员成功!');
                        this.channel.members = data.data.members;
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_MEMBER_ADD_OR_REMOVE, {
                            type: 'add',
                            members: data.data.members
                        });
                    } else {
                        toastr.error(data.data, '添加成员失败!');
                    }
                }).always(() => {
                    this.emModal.hideDimmer();
                });
            },
            onLabelRemove: (removedValue) => {
                if (this.channel.owner.username == removedValue) {
                    return false;
                }

                this.emModal.showDimmer();
                $.post('/admin/channel/removeMember', {
                    id: this.channel.id,
                    members: removedValue,
                    baseUrl: utils.getBaseUrl(),
                    path: wurl('path'),
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('移除成员成功!');
                        this.channel.members = data.data.members;
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_MEMBER_ADD_OR_REMOVE, {
                            type: 'remove',
                            members: data.data.members
                        });
                    } else {
                        toastr.error(data.data, '移除成员失败!');
                    }
                }).always(() => {
                    this.emModal.hideDimmer();
                });
            }
        };
    }

    channelChanged() {
        if (this.channel) {
            let usernames = _.sortBy(_.map(this.channel.members, 'username'));
            // usernames = [this.channel.owner.username, ..._.without(usernames, this.channel.owner.username)];
            _.defer(() => {
                $(this.membersRef).dropdown().dropdown('clear').dropdown('set selected', usernames).dropdown(this.membersOpts);
            });

        }
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

    }

    initMembersUI(last) {

        if (last) {
            _.defer(() => {
                this.channelChanged();
            });
        }
    }

    showHandler() {
        $(this.membersRef).dropdown().dropdown('clear');
        this.channelChanged();
    }

    approveHandler(modal) {

    }

    show() {
        this.emModal.show({
            hideOnApprove: true,
            autoDimmer: false
        });
    }
}
