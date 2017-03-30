import { customAttribute, inject } from 'aurelia-framework';

@customAttribute('ui-dropdown-action')
@inject(Element)
export class AttrUiDropdownActionCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

    }

    _init(context) {
        _.defer(() => {
            $(this.element).dropdown({
                action: 'hide',
                context: context
            });
        });
    }

    bind() {
        this._init(this.value ? this.value : window);
    }
}
