import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelJoin {

    @bindable loginUser;

    _getChannels() {
        $.get('/admin/channel/list', (data) => {
            if (data.success) {
                this.channels = data.data;
                _.each(this.channels, (item) => {
                    item.joined = _.some(item.members, (m) => {
                        return m.username == this.loginUser.username;
                    });
                });
            } else {
                toastr.error(data.data, '获取频道列表失败!');
            }
        });
    }

    refresh() {
        this._getChannels();
    }

    joinHandler(item) {
        this.confirmMd.show({
            content: `确定要加入频道<code class="nx">${item.title}</code>吗?`,
            onapprove: () => {
                $.post('/admin/channel/join', {
                    id: item.id
                }, (data) => {
                    if (data.success) {
                        toastr.success('加入频道成功!');
                        item.joined = true;
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_JOINED, { channel: data.data });
                    } else {
                        toastr.error(data.data, '加入频道失败!');
                    }
                });
            }
        });
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
                        item.joined = false;
                        ea.publish(nsCons.EVENT_CHAT_CHANNEL_LEAVED, { channel: data.data });
                    } else {
                        toastr.error(data.data, '离开频道失败!');
                    }
                });
            }
        });
    }
}
