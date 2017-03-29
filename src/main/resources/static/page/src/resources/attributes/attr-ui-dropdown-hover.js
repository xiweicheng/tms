import { customAttribute, inject } from 'aurelia-framework';

@customAttribute('ui-dropdown-hover')
@inject(Element)
export class AttrUiDropdownHoverCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

    }

    _init(action) {
        _.defer(() => {
            $(this.element).dropdown({
                on: 'hover',
                action: action
            });
        });
    }

    bind() {
        this._init(this.value ? this.value : 'hide');
    }
}
