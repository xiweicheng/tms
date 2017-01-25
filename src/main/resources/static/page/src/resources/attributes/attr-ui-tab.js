import { customAttribute, inject } from 'aurelia-framework';

@customAttribute('ui-tab')
@inject(Element)
export class AttrUiTabCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

    }

    _init() {
        _.defer(() => {
            $(this.element).find('.item').tab();
        });
    }

    bind() {
        this._init();
    }
}
