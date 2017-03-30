import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogRightSidebar {

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_RIGHT_SIDEBAR_TOGGLE, (payload) => {
            if (payload.action == 'dir') {
                $(this.dirRef).empty().append(payload.dir);
            }
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }
}
