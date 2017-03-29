import { bindable, containerless } from 'aurelia-framework';
import {
    default as clipboard
} from 'clipboard-js';
import {
    default as Clipboard
} from 'clipboard';

@containerless
export class EmBlogContent {

    blog;

    loginUser = nsCtx.loginUser;
    isSuper = nsCtx.isSuper;
    isAdmin = nsCtx.isAdmin;

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_SWITCH, (payload) => {
            this.getBlog();
            ea.publish(nsCons.EVENT_BLOG_RIGHT_SIDEBAR_TOGGLE, {
                toggle: false
            });
        });
        this.subscribe2 = ea.subscribe(nsCons.EVENT_BLOG_CHANGED, (payload) => {
            if (payload.action == 'updated') {
                // this.blog = payload.blog;
                _.extend(this.blog, payload.blog);
                _.defer(() => this._dir());
            }
        });

        this.throttleCreateHandler = _.throttle(() => { this.createHandler() }, 1000, { 'trailing': false });
        this.throttleEditHandler = _.throttle(() => { this.editHandler() }, 1000, { 'trailing': false });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe2.dispose();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        this.getBlog();

        new Clipboard('.em-blog-content .tms-clipboard')
            .on('success', function(e) {
                toastr.success('复制到剪贴板成功!');
            }).on('error', function(e) {
                toastr.error('复制到剪贴板失败!');
            });

        $('.em-blog-content').on('click', 'code[data-code]', function(event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).attr('data-code')).then(
                    () => { toastr.success('复制到剪贴板成功!'); },
                    (err) => { toastr.error('复制到剪贴板失败!'); }
                );
            }
        });

        $('.em-blog-content').on('click', '.pre-code-wrapper', function(event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).find('i[data-clipboard-text]').attr('data-clipboard-text')).then(
                    () => { toastr.success('复制到剪贴板成功!'); },
                    (err) => { toastr.error('复制到剪贴板失败!'); }
                );
            }
        });

        $('.em-blog-right-sidebar').on('click', '.panel-blog-dir .wiki-dir-item', (event) => {
            event.preventDefault();
            if ($(window).width() <= 768) {
                ea.publish(nsCons.EVENT_BLOG_RIGHT_SIDEBAR_TOGGLE, { toggle: false });
            }
            $('.em-blog-content').scrollTo(`#${$(event.currentTarget).attr('data-id')}`, 200, {
                offset: 0
            });
        });

        $(this.mkbodyRef).on('dblclick', (event) => {
            if (event.ctrlKey) {
                if (this.blog.openEdit || this.isSuper || this.blog.creator.username == this.loginUser.username) {
                    this.editHandler();
                }
            }
        });

        $('.em-blog-content').scroll(_.throttle((event) => {
            try {
                let sHeight = $('.em-blog-content')[0].scrollHeight;
                let sTop = $('.em-blog-content')[0].scrollTop;

                let scale = sTop * 1.0 / (sHeight - $('.em-blog-content').outerHeight());
                this.progressWidth = $('.em-blog-content').outerWidth() * scale;
            } catch (err) { this.progressWidth = 0; }

        }, 10));

        this.initHotkeys();
    }

    initHotkeys() {
        try {
            $(document).bind('keyup', 'e', (evt) => { // edit
                evt.preventDefault();
                if (this.blog.openEdit || this.isSuper || this.blog.creator.username == this.loginUser.username) {
                    this.throttleEditHandler();
                }
            }).bind('keyup', 'c', (evt) => { // create
                evt.preventDefault();
                this.throttleCreateHandler();
            }).bind('keydown', 'd', (evt) => { // dir
                evt.preventDefault();
                if (this.dir) {
                    this.catalogHandler();
                }
            }).bind('keydown', 's', (evt) => { // share
                evt.preventDefault();
                this.blogShareVm.show();
            }).bind('keydown', 'f', (evt) => { // follow
                evt.preventDefault();
                this.followerHandler();
            }).bind('keydown', 't', (event) => { // scroll top
                event.preventDefault();
                $('.em-blog-content').scrollTo(0, 200, {
                    offset: 0
                });
            }).bind('keydown', 'b', (event) => { // scroll bottom
                event.preventDefault();
                $('.em-blog-content').scrollTo(`max`, 200, {
                    offset: 0
                });
            });
        } catch (err) { console.log(err); }

    }

    _dir() {
        this.dir = utils.dir($(this.mkbodyRef), 'tms-blog-dir-item-');
        return this.dir;
    }

    getBlog() {
        this.progressWidth = 0;
        if (!nsCtx.blogId || isNaN(new Number(nsCtx.blogId))) {
            return;
        }

        this.getStow();
        this.getFollower();

        return $.get('/admin/blog/get', {
            id: nsCtx.blogId
        }, (data) => {
            if (data.success) {
                this.blog = data.data;
                ea.publish(nsCons.EVENT_BLOG_VIEW_CHANGED, this.blog);
                _.defer(() => this._dir());
            } else {
                toastr.error(data.data, "获取博文失败!");
            }
        });
    }

    getStow() {
        $.get('/admin/blog/stow/get', {
            id: nsCtx.blogId
        }, (data) => {
            if (data.success) {
                this.blogStow = data.data;
            } else {
                toastr.error(data.data);
            }
        });
    }

    getFollower() {
        $.get('/admin/blog/follower/get', {
            id: nsCtx.blogId
        }, (data) => {
            if (data.success) {
                this.blogFollower = data.data;
            } else {
                toastr.error(data.data);
            }
        });
    }

    editHandler() {
        if (!nsCtx.isModaalOpening) {
            ea.publish(nsCons.EVENT_BLOG_ACTION, { action: 'edit', id: this.blog.id });
        }
    }

    deleteHandler() {

        this.emConfirmModal.show({
            onapprove: () => {
                $.post("/admin/blog/delete", {
                    id: this.blog.id
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('删除博文成功!');
                        window.location.href = "#/blog";
                        window.location.reload();
                    } else {
                        toastr.error(data.data, '删除博文失败!');
                    }
                });
            }
        });

    }

    createHandler() {
        if (!nsCtx.isModaalOpening) {
            $('a[href="#modaal-blog-write"]').click();
        }
    }

    updateSpaceHandler() {
        this.blogSpaceUpdateVm.show(this.blog);
    }

    updatePrivatedHandler() {
        $.post('/admin/blog/privated/update', {
            id: this.blog.id,
            privated: !this.blog.privated
        }, (data, textStatus, xhr) => {
            if (data.success) {
                _.extend(this.blog, data.data);
                ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                    action: 'updated',
                    blog: this.blog
                });
                toastr.success('更新博文可见性成功!');
            } else {
                toastr.error(data.data, '更新博文可见性失败!');
            }
        });
    }

    isZanDone() {
        let voteZan = this.blog.voteZan;
        if (!voteZan) {
            return false;
        }

        return voteZan.split(',').includes(this.loginUser.username);
    }

    rateHandler() {
        $.post('/admin/blog/vote', {
            id: this.blog.id,
            url: utils.getBasePath(),
            contentHtml: utils.md2html(this.blog.content),
            type: this.isZanDone() ? 'Cai' : 'Zan'
        }, (data, textStatus, xhr) => {
            if (data.success) {
                _.extend(this.blog, data.data);
            } else {
                toastr.error(data.data, '博文投票失败!');
            }
        });
    }

    openEditHandler() {
        $.post('/admin/blog/openEdit', {
            id: this.blog.id,
            open: !this.blog.openEdit
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.blog.openEdit = !this.blog.openEdit;
                ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                    action: 'updated',
                    blog: this.blog
                });
                toastr.success(this.blog.openEdit ? '开放协作编辑成功!' : '关闭协作编辑成功!');
            } else {
                toastr.error(data.data, '协作编辑操作失败!');
            }
        });
    }

    refreshHandler() {
        let p = this.getBlog();
        p && p.done(() => { toastr.success('刷新操作成功!'); });
    }

    historyHandler() {
        this.blogHistoryVm.show(this.blog);
    }

    catalogHandler() {
        ea.publish(nsCons.EVENT_BLOG_RIGHT_SIDEBAR_TOGGLE, {
            action: 'dir',
            dir: this._dir()
        });
    }

    authHandler() {
        this.blogSpaceAuthVm.show('blog', this.blog);
    }

    copyHandler() {
        if (!nsCtx.isModaalOpening) {
            ea.publish(nsCons.EVENT_BLOG_ACTION, { action: 'copy', id: this.blog.id });
        }
    }

    stowHandler() {
        if (!this.blogStow) {
            $.post('/admin/blog/stow/add', {
                id: this.blog.id
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.blogStow = data.data;
                    ea.publish(nsCons.EVENT_BLOG_STOW_CHANGED, { action: 'add', data: this.blogStow });
                    toastr.success('博文收藏成功!');
                } else {
                    toastr.error(data.data);
                }
            });
        } else {
            $.post('/admin/blog/stow/remove', {
                sid: this.blogStow.id
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    ea.publish(nsCons.EVENT_BLOG_STOW_CHANGED, { action: 'remove', data: this.blogStow });
                    this.blogStow = null;
                    toastr.success('删除博文收藏成功!');
                } else {
                    toastr.error(data.data);
                }
            });
        }

    }

    followerHandler() {
        if (!this.blogFollower) {
            $.post('/admin/blog/follower/add', {
                id: this.blog.id
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.blogFollower = data.data;
                    toastr.success('博文关注成功!');
                } else {
                    toastr.error(data.data);
                }
            });
        } else {
            $.post('/admin/blog/follower/remove', {
                fid: this.blogFollower.id
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.blogFollower = null;
                    toastr.success('取消博文关注成功!');
                } else {
                    toastr.error(data.data);
                }
            });
        }

    }

    dimmerHandler() {
        ea.publish(nsCons.EVENT_BLOG_SWITCH, {});
    }
}
