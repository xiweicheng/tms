import { bindable } from 'aurelia-framework';

export class EmHotkeysModal {

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.md).modal();
    }

    show() {
        $(this.md).modal('show');
    }
}
