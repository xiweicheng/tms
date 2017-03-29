import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatScheduleRemind {

    @bindable events;

    interval = 5000;

    headOffset = 10 * 60 * 1000;

    reminded = [];

    /**
     * 构造函数
     */
    constructor() {
        this._pollCheck();
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.timer && clearInterval(this.timer);
    }

    _pollCheck() {
        this.timer = setInterval(() => {

            if (!this.events) {
                return;
            }

            let now = new Date().getTime();
            _.each(this.events, (event) => {
                if (event.start && !_.includes(this.reminded, event.id)) {
                    let start = event.start;
                    if (start > now && start < (now + this.headOffset)) {
                        this.event = event;
                        this.reminded.push(event.id);
                        this._desktopPuh();
                        this.show();
                    }
                }
            });

        }, this.interval);
    }

    _desktopPuh() {
        push.create('TMS日程提醒通知', {
            body: `内容: ${this.event.title}`,
            icon: {
                x16: 'img/tms-x16.ico',
                x32: 'img/tms-x32.png'
            },
            timeout: 5000
        });
    }

    showHandler() {

    }

    approveHandler() {

    }

    show() {
        this.emModal.show({
            hideOnApprove: true,
            autoDimmer: false
        });
    }
}
