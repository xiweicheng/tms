import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatSystemLinkMgr {

    links = [];

    addHandler() {
        $.post('/admin/link/create', {
            title: this.title,
            href: this.href,
            type: 'App'
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.title = '';
                this.href = '';
                this.links.push(data.data);
                ea.publish(nsCons.EVENT_SYSTEM_LINKS_REFRESH, {});
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
                ea.publish(nsCons.EVENT_SYSTEM_LINKS_REFRESH, {});
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
                ea.publish(nsCons.EVENT_SYSTEM_LINKS_REFRESH, {});
                toastr.success('更新成功!');
            } else {
                toastr.error(data.data);
            }
        });
    }

    showHandler() {
        $.get('/admin/link/listByApp', (data) => {
            if (data.success) {
                this.links = data.data;
            } else {
                this.links = [];
            }
        });
    }

    show() {
        this.emModal.show({ autoDimmer: false });
    }

    approveHandler(modal) {

    }
}
