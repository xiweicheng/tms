import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogHistoryView {

    isSuper = nsCtx.isSuper;
    loginUser = nsCtx.loginUser;

    showHandler() {}

    approveHandler() {

    }

    show(blogHistory, ver, isCurrentVer) {
        this.blogHistory = blogHistory;
        this.blog = blogHistory.blog;
        this.ver = ver;
        this.isCurrentVer = isCurrentVer;
        this.emModal.show({ hideOnApprove: true, autoDimmer: false });
    }

    restoreHandler() {
        this.ajax1 = $.post('/admin/blog/history/restore', { hid: this.blogHistory.id }, (data, textStatus, xhr) => {
            if (data.success) {
                ea.publish(nsCons.EVENT_BLOG_CHANGED, { action: 'updated', blog: data.data });
                ea.publish(nsCons.EVENT_BLOG_HISTORY_CHANGED, {});
                toastr.success('博文历史记录还原成功!');
                this.emModal.hide();
            } else {
                toastr.error(data.data, '博文历史记录还原失败!');
            }
        });
    }
}
