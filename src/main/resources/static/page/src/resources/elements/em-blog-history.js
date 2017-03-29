import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogHistory {

    isSuper = nsCtx.isSuper;
    loginUser = nsCtx.loginUser;

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_HISTORY_CHANGED, (payload) => {
            this.refreshHistory();
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    viewHistoryHandler(blogHistory, ver, isCurrentVer) {
        this.blogHistoryViewVm.show(blogHistory, ver, isCurrentVer);
    }

    refreshHistory() {
        $.get('/admin/blog/history/list', {
            id: this.blog.id
        }, (data) => {
            if (data.success) {
            	this.oldHistories = data.data;
                this.histories = _.reverse(_.clone(data.data));
            } else {
                toastr.error(data.data, '获取博文历史失败!');
            }
        });
    }

    showHandler() {
        this.refreshHistory();
    }

    approveHandler() {

    }

    show(blog) {
        this.blog = blog;
        this.emModal.show({ hideOnApprove: true, autoDimmer: false });
    }

    restoreHandler(item) {
        this.ajax1 = $.post('/admin/blog/history/restore', { hid: item.id }, (data, textStatus, xhr) => {
            if (data.success) {
                ea.publish(nsCons.EVENT_BLOG_CHANGED, { action: 'updated', blog: data.data });
                this.refreshHistory();
                toastr.success('博文历史记录还原成功!');
            } else {
                toastr.error(data.data, '博文历史记录还原失败!');
            }
        });
    }

    removeHandler(item) {
        this.ajax2 = $.post('/admin/blog/history/remove', { hid: item.id }, (data, textStatus, xhr) => {
            if (data.success) {
                this.refreshHistory();
                toastr.success('博文历史记录删除成功!');
            } else {
                toastr.error(data.data, '博文历史记录删除失败!');
            }
        });
    }

    diffHandler() {
        let list = [...this.oldHistories, this.blog];
        let chks = _.filter(list, 'checked');
        if (chks && chks.length > 1) {
            let f = chks[chks.length - 1];
            let s = chks[chks.length - 2];
            let fIndex = _.indexOf(list, f);
            let sIndex = _.indexOf(list, s);
            this.blogHistoryDiffVm.show(f, s, fIndex, sIndex);
        } else {
            toastr.error('请先选择要比较版本');
        }
    }
}
