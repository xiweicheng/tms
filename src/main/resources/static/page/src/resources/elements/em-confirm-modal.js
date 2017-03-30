import { bindable } from 'aurelia-framework';

export class EmConfirmModal {

	config = {};

    constructor() { // 通过构造函数注入
    }

    /**
     * 当视图从DOM中分离时被调用
     */
    detached() {
        $(this.md).remove();
    }

    attached() {

        $(this.md).modal({
            closable: false,
            allowMultiple: true,
            onApprove: () => {
                this.onapprove && this.onapprove();
            },
            onDeny: () => {
                this.ondeny && this.ondeny();
            }
        });
    }

    reset() {
        this.config = {
            title: '操作确认',
            content: '确定要执行该操作吗?',
            warning: false
        };
    }

    /**
     * 显示确认窗口
     * @param onapprove: 确认回调函数
     * @param ondeny: 取消回调函数
     */
    show(config) {

        this.reset();

        if (config) {
            this.config = _.extend(this.config, config);
        }

        if (config && config.onapprove) {
            this.onapprove = config.onapprove;
        }

        if (config && config.ondeny) {
            this.ondeny = config.ondeny;
        }

        $(this.md).modal('show');
    }

    /* 隐藏确认窗口 */
    hide() {
        $(this.md).modal('hide');
    }
}
