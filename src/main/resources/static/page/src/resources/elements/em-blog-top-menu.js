import { bindable, containerless } from 'aurelia-framework';
import 'timeago';
let tg = timeago();

@containerless
export class EmBlogTopMenu {

    isHide = true;

    loginUser = nsCtx.loginUser;

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_SWITCH, (payload) => {
            this.toggleHandler(true);
        });
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_LEFT_SIDEBAR_TOGGLE, (payload) => {
            this.toggleHandler(payload.isHide);
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.logoRef).on('mouseenter', (event) => {
            $(this.logoRef).animateCss('flip');
        });

        $(this.searchRef)
            .search({
                type: 'category',
                minCharacters: 2,
                onSelect: (result, response) => {
                    $(this.searchRef).search('hide results');
                    _.defer(() => $(this.searchRef).find('input').blur());
                    return false;
                },
                apiSettings: {
                    onResponse: function(resp) {
                        // var response = {
                        //     results: []
                        // };
                        // $.each(resp.data, (index, item) => {
                        //     response.results.push({
                        //         title: item.title,
                        //         // description: utils.abbreviate(item.content, 65),
                        //         description: `<i class="wait icon"></i>${item.creator.name} 创建于 ${tg.format(item.createDate, 'zh_CN')}`,
                        //         url: `#/blog/${item.id}`
                        //     });
                        // });
                        var response = {
                            results: {
                                blogs: {
                                    name: `博文 (${resp.data.blogs.length})`,
                                    results: []
                                },
                                comments: {
                                    name: `评论 (${resp.data.comments.length})`,
                                    results: []
                                }
                            }
                        };
                        $.each(resp.data.blogs, (index, item) => {
                            response.results.blogs.results.push({
                                title: item.title,
                                // description: utils.abbreviate(item.content, 65),
                                description: `<i class="wait icon"></i>${item.creator.name} 创建于 ${tg.format(item.createDate, 'zh_CN')}`,
                                url: `#/blog/${item.id}`
                            });
                        });
                        $.each(resp.data.comments, (index, item) => {
                            response.results.comments.results.push({
                                title: `#/blog/${item.targetId}?cid=${item.id}`,
                                // description: item.content,
                                description: `<i class="wait icon"></i>${item.creator.name} 创建于 ${tg.format(item.createDate, 'zh_CN')}<br/>${utils.encodeHtml(item.content)}`,
                                url: `#/blog/${item.targetId}?cid=${item.id}`
                            });
                        });
                        return response;
                    },
                    url: '/admin/blog/search?search={query}&comment=true&ellipsis=60'
                }
            });

        this._refreshSysLinks();

        if (nsCtx.blogId == 'create') {
            _.defer(() => { $('a[href="#modaal-blog-write"]').click(); });
        }
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

    searchBlurHandler() {
        this.isSearchFocus = false;
    }

    searchFocusHandler() {
        this.isSearchFocus = true;
    }

    toggleHandler(isHide) {
        if (this.isHide === isHide) {
            return;
        }
        this.isHide = isHide ? isHide : !this.isHide;
        ea.publish(nsCons.EVENT_BLOG_TOGGLE_SIDEBAR, this.isHide);
    }

    userEditHandler() {
        this.userEditMd.show();
    }

    logoutHandler() {
        $.post('/admin/logout').always(() => {
            utils.redirect2Login();
        });
    }

    searchKeyupHandler(event) {
        if (event.keyCode == 27) {
            $(this.searchRef).search('set value', '');
        }
    }
}
