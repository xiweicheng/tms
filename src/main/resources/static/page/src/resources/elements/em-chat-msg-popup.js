import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatMsgPopup {

    chatMsg;

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_CHAT_MSG_POPUP_SHOW, (payload) => {
            this.id = payload.id;
            this.target = payload.target;

            $(this.target).popup({
                popup: this.popup,
                hoverable: true,
                inline: false,
                movePopup: false,
                // position: 'top left',
                // maxSearchDepth: 50,
                silent: true,
                position: 'bottom left',
                jitter: 300,
                prefer: 'opposite',
                delay: {
                    show: 300,
                    hide: 300
                },
                onShow: () => {
                    $.get('/admin/chat/channel/get', {
                        id: this.id
                    }, (data) => {
                        if (data.success) {
                            this.chatMsg = data.data;
                        } else {
                            toastr.error(data.data, "加载失败!");
                        }
                    });
                }
            }).popup('show');
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }
}
