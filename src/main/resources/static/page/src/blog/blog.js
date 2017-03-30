import { bindable, inject } from 'aurelia-framework';
import chatService from 'chat/chat-service';

export class Blog {

    rightSidebarShow = false;
    isHide = true;

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_VIEW_CHANGED, (payload) => {
            this.routeConfig && this.routeConfig.navModel.setTitle(`${payload.title} | 博文 | TMS`);
        });
        this.subscribe1 = ea.subscribe(nsCons.EVENT_BLOG_RIGHT_SIDEBAR_TOGGLE, (payload) => {
            if (payload && !_.isUndefined(payload.toggle)) {
                this.rightSidebarShow = payload.toggle;
            } else {
                this.rightSidebarShow = !this.rightSidebarShow;
            }
        });
        this.subscribe2 = ea.subscribe(nsCons.EVENT_BLOG_TOGGLE_SIDEBAR, (payload) => {
            this.isHide = payload;
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe1.dispose();
        this.subscribe2.dispose();

        clearInterval(this.timeagoTimer);
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

        let tg = timeago();
        this.timeagoTimer = setInterval(() => {
            $(this.blogContainerRef).find('[data-timeago]').each((index, el) => {
                $(el).text(tg.format($(el).attr('data-timeago'), 'zh_CN'));
            });
        }, 5000);

        // 用户信息popup
        $('.tms-blog').on('mouseenter', 'span[data-value].at-user:not(.pp-not),a[data-value].author:not(.pp-not)', (event) => {
            event.preventDefault();
            var $a = $(event.currentTarget);
            ea.publish(nsCons.EVENT_CHAT_MEMBER_POPUP_SHOW, {
                username: $a.attr('data-value'),
                target: event.currentTarget
            });
        });

    }

    /**
     * 当视图从DOM中分离时被调用
     */
    detached() {

    }

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {

        this.routeConfig = routeConfig;
        nsCtx.blogId = params.id;

        ea.publish(nsCons.EVENT_BLOG_SWITCH, { id: params.id });

        return Promise.all([chatService.loginUser().then((user) => {
                nsCtx.loginUser = user;
                nsCtx.isSuper = utils.isSuperUser(user);
                nsCtx.isAdmin = utils.isAdminUser(user);
            }),
            chatService.listUsers().then((users) => {
                nsCtx.users = users;
                window.tmsUsers = users;
            })
        ]);
    }
}
