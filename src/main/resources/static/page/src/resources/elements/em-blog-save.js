import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogSave {

    @bindable trigger;

    loginUser = nsCtx.loginUser;
    isSuper = nsCtx.isSuper;

    triggerChanged() {

        $(this.trigger).click((event) => {
            this.show();
        });

    }

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_SAVE, (payload) => {
            this.blogInfo = payload;
            this.show();
        });
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox();
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    show() {
        this.emModal.show({ hideOnApprove: false, autoDimmer: true });
    }

    showHandler() {
        $(this.chk).checkbox('set unchecked');
        $.get('/admin/space/listMy', (data) => {
            if (data.success) {
                this.spaces = data.data;
            }
        });
    }

    approveHandler(modal) {

        var html = utils.md2html(this.blogInfo.content);
        let users = [nsCtx.memberAll, ...(window.tmsUsers ? tmsUsers : [])];

        $.post(`/admin/blog/create`, {
            url: utils.getBasePath(),
            usernames: utils.parseUsernames(this.blogInfo.content, users).join(','),
            title: this.blogInfo.title,
            content: this.blogInfo.content,
            spaceId: $(this.spacesRef).dropdown('get value'),
            privated: $(this.chk).checkbox('is checked'),
            contentHtml: html
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.blog = data.data;
                toastr.success('博文保存成功!');
                ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                    action: 'created',
                    blog: this.blog
                });
                modal.hide();
                $('a[href="#modaal-blog-write"]').modaal('close');
            } else {
                toastr.error(data.data, '博文保存失败!');
            }
        });
    }

    initSpacesHandler(last) {
        if (last) {
            _.defer(() => {
                $(this.spacesRef).dropdown('clear');
            });
        }
    }
}
