import {
    bindable,
    containerless
}
from 'aurelia-framework';

@containerless
export class EmModal {

    @bindable confirmLabel = '确认';
    @bindable cancelLabel = '取消';
    @bindable onapprove;
    @bindable ondeny;
    @bindable onshow;
    @bindable onvisible;
    @bindable disabled = false;
    @bindable classes = 'small';
    @bindable showConfirm = true;

    options = {
        hideOnApprove: true,
        autoDimmer: true
    }

    /**
     * 当视图从DOM中分离时被调用
     */
    detached() {
        $(this.modal).remove();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

        $(this.modal).modal({
            closable: false,
            autofocus: false,
            observeChanges: true,
            allowMultiple: true,
            // detachable: false,
            onShow: () => {
                this.onshow && this.onshow(this);
            },
            onVisible: () => {
                this.onvisible && this.onvisible(this);
            },
            onApprove: () => {
                this.options.autoDimmer && this.showDimmer();
                this.onapprove && this.onapprove(this);
                return this.options.hideOnApprove;
            },
            onDeny: () => {
                this.ondeny && this.ondeny(this);
            }
        });
    }

    showDimmer() {
        this.loading = true;
        $(this.modal).find('.dimmer').dimmer('show');
    }

    hideDimmer() {
        this.loading = false;
        $(this.modal).find('.dimmer').dimmer('hide');
    }

    show(options) {
        _.extend(this.options, options);
        $(this.modal).modal('show');
    }

    hide() {
        this.hideDimmer();
        $(this.modal).modal('hide');
    }

    refresh() {
        // 延迟方法
        _.defer(() => {
            $(this.modal).modal('refresh');
        });
    }

}
