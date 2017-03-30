import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelLinkMgr {

    @bindable channel;

    @bindable loginUser;

    links = [];

    channelChanged(news, old) {
        if (this.channel) {
            $.get('/admin/link/listBy', {
                channelId: this.channel.id
            }, (data) => {
                if (data.success) {
                    this.links = data.data;
                } else {
                    this.links = [];
                }
            });
        }
    }

    addHandler() {
        $.post('/admin/link/create', {
            title: this.title,
            href: this.href,
            channelId: this.channel.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.title = '';
                this.href = '';
                this.links.push(data.data);
                ea.publish(nsCons.EVENT_CHANNEL_LINKS_REFRESH, {});
            } else {
                toastr.error(data.data);
            }
        });
    }

    delHandler(item) {
        $.post('/admin/link/delete', {
            id: item.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.links = _.reject(this.links, { id: item.id });
                ea.publish(nsCons.EVENT_CHANNEL_LINKS_REFRESH, {});
                toastr.success('删除成功!');
            } else {
                toastr.error(data.data);
            }
        });
    }

    editHandler(item) {
        item.oldTitle = item.title;
        item.oldHref = item.href;
        item.isEditing = true;
    }

    updateHandler(item) {
        $.post('/admin/link/update', {
            id: item.id,
            title: item.title,
            href: item.href,
        }, (data, textStatus, xhr) => {
            if (data.success) {
                item.isEditing = false;
                ea.publish(nsCons.EVENT_CHANNEL_LINKS_REFRESH, {});
                toastr.success('更新成功!');
            } else {
                toastr.error(data.data);
            }
        });
    }

    showHandler() {
        this._reset();
    }

    _reset() {}

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {}

    show() {
        this.emModal.show({ autoDimmer: false });
    }

    approveHandler(modal) {

    }
}
