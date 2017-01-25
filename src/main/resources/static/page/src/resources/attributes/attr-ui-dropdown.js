import { customAttribute, inject } from 'aurelia-framework';

@customAttribute('ui-dropdown')
@inject(Element)
export class AttrUiDropdownCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

    }

    _init(action) {
        _.defer(() => {
            $(this.element).dropdown({
                action: action
            });
        });
    }

    bind() {
        this._init(this.value ? this.value : 'hide');
    }
}
